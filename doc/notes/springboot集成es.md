# SpringBoot集成es

本文档是为了解决低版本spring-data-elasticsearch对es7的支持不友好问题

## yml配置

```yaml
es.hosts=125.88.36.99:9268,125.88.36.100:9268,125.88.36.101:9268
es.username=elastic
es.password=168mNBwXpECcN2i0sHLB2XGgimZfGciXuYpcHgYwTKd8Km84hw6ij5LoSs2nOl4SJpK
```

## Config类

```java
package com.sunsco.entity.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.sunsco.entity.model.elastic.BaseElasticEntity.*;

/**
 * Elasticsearch 配置
 * 由spring管理
 *
 * @author YanCh
 * Create by 2020-03-05 9:09
 **/
@Configuration
@Slf4j
public class ElasticsearchConfig implements InitializingBean, DisposableBean, FactoryBean<RestHighLevelClient> {
    @Value("${es.hosts}")
    private String[] hosts;
    @Value("${es.username}")
    private String username;
    @Value("${es.password}")
    private String password;


    private RestHighLevelClient restHighLevelClient;

    @Override
    public RestHighLevelClient getObject() throws Exception {
        return this.restHighLevelClient;
    }

    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (restHighLevelClient != null) {
            log.info("==========销毁restHighLevelClient连接=============");
            restHighLevelClient.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        restHighLevelClient = buildClient();
        createIndex(restHighLevelClient);
    }

    private RestHighLevelClient buildClient() {
        log.info("=========创建Elasticsearch连接开始========");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hosts.length; i++) {
            log.info("===========连接{}开始========", hosts[i]);
            httpHosts[i] = HttpHost.create(hosts[i]);
            stringBuilder.append(hosts[i]).append(",");
        }
        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (StringUtils.isNotBlank(username)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        restHighLevelClient = new RestHighLevelClient(builder);
        log.info("============连接{}成功==========", stringBuilder.toString());
        return restHighLevelClient;
    }

    /**
     * 创建es索引
     *
     * @param restHighLevelClient
     */
    private void createIndex(RestHighLevelClient restHighLevelClient) {
        try {
            // 遍历需要创建的索引
            for (int i = 0; i < INDEX_NAMES.length; i++) {
                if (this.indexExist(INDEX_NAMES[i], restHighLevelClient)) {
                    log.warn(" idxName={} 已经存在,idxSql={}", INDEX_NAMES[i], INDEX_MAPPING_PATHS[i]);
                    continue;
                }
                String idxSQL = returnXmlStr(INDEX_MAPPING_PATHS[i]);
                String setting = returnXmlStr(INDEX_SETTING_PATHS[i]);
                CreateIndexRequest request = new CreateIndexRequest(INDEX_NAMES[i]);
                request.mapping(idxSQL, XContentType.JSON);
                request.settings(setting, XContentType.JSON); // 手工指定Setting
                CreateIndexResponse res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
                if (!res.isAcknowledged()) {
                    throw new RuntimeException("初始化失败");
                } else {
                    log.info("创建index={}，创建成功", INDEX_NAMES[i]);
                }
            }
        } catch (Exception e) {
            log.error("================初始化es index 异常========================", e);
            System.exit(0);
        }
    }

    /**
     * 断某个index是否存在
     *
     * @param idxName index名
     * @return boolean
     */
    public boolean indexExist(String idxName, RestHighLevelClient restHighLevelClient) throws Exception {
        return restHighLevelClient.indices().exists(new GetIndexRequest(idxName), RequestOptions.DEFAULT);
    }

    private String returnXmlStr(String path) throws IOException {
        //此处如果用File file = Resource.getFile(filePath)会报异常：找不到文件
        Resource resource = new ClassPathResource(path);
        InputStream is = resource.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
}
```

## BaseElasticEntity实体基类

```java
package com.sunsco.entity.model.elastic;

/**
 * Elastic基类
 *
 * @author YanCh
 * Create by 2020-03-05 10:13
 **/
public class BaseElasticEntity {
    /**
     * id
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * es索引名称（项目启动时初始化创建索引）
     */
    public final static String[] INDEX_NAMES = {
            "el_store_goods",
            "el_store_goods_map"
    };

    /**
     * es索引设置相对路径
     */
    public final static String[] INDEX_SETTING_PATHS = {
            "/json/base-setting.json",
            "/json/base-setting.json"
    };

    /**
     *
     */
    public final static String[] INDEX_MAPPING_PATHS = {
            "/json/el_store_goods-mapping.json",
            "/json/el_store_goods_map-mapping.json"
    };
}
```

> 所有es的实体类全部继承该类

## 自定义一个es索引名称注解

```java
package com.sunsco.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解es的index名称
 *
 * @author YanCh
 * @version v1.0
 * Create by 2020-07-16 12:09
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsIndexName {

    /**
     * es 索引名称
     *
     * @return
     */
    String value();

}

```

## 基类Service

```java
package com.sunsco.service.elastic;

import com.sunsco.entity.model.elastic.BaseElasticEntity;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Elastic接口基类
 *
 * @author YanCh
 * Create by 2020-03-05 10:05
 **/
public interface BaseElasticService<T extends BaseElasticEntity> {

    default Class<T> getClazz() {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 创建索引
     *
     * @param idxSQL  索引描述
     * @param setting setting
     */
    void createIndex(String idxSQL, String setting);

    /**
     * 断某个index是否存在
     *
     * @return boolean
     */
    boolean indexExist() throws Exception;

    /**
     * 断某个index是否存在
     *
     * @return boolean
     * @throws
     */
    boolean isExistsIndex() throws Exception;

    /**
     * 设置分片
     *
     * @param request
     * @param shards   分片数
     * @param replicas 副本数
     * @throws
     */
    void buildSetting(CreateIndexRequest request, Integer shards, Integer replicas);

    /**
     * 插入数据
     *
     * @param t 对象
     */
    void insertOrUpdateOne(T t);

    /**
     * 批量插入数据
     *
     * @param list 带插入列表
     */
    void insertBatch(List<T> list);

    /**
     * 批量删除
     *
     * @param idList 待删除列表
     */
    void deleteBatch(Collection<T> idList);

    /**
     * 查询
     *
     * @param builder 查询参数
     * @return
     */
    List<T> search(SearchSourceBuilder builder);

    /**
     * set打乱顺序保证唯一
     *
     * @param builder
     * @return
     */
    Set<T> searchBySet(SearchSourceBuilder builder);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    T searchById(String id);

    int searchCount(QueryBuilder builder);

    /**
     * 删除index
     */
    void deleteIndex();

    /**
     * 批量删除
     *
     * @param builder
     */
    void deleteByQuery(QueryBuilder builder);
}
```

## 实现

```java
package com.sunsco.service.elastic.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sunsco.entity.annotation.EsIndexName;
import com.sunsco.entity.model.elastic.BaseElasticEntity;
import com.sunsco.service.elastic.BaseElasticService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Elastic接口基类
 *
 * @author YanCh
 * Create by 2020-03-05 10:05
 **/
@Slf4j
@Component
public class BaseElasticServiceImpl<T extends BaseElasticEntity> implements BaseElasticService<T> {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    private String getIndexName() {
        Class clazz = getClazz();
        if (clazz.isAnnotationPresent(EsIndexName.class)) {
            EsIndexName esIndexName = (EsIndexName) clazz.getAnnotation(EsIndexName.class);
            return esIndexName.value();
        } else {
            return clazz.getSimpleName();
        }
    }

    public void createIndex(String idxSQL, String setting) {
        String indexName = getIndexName();
        try {
            if (this.isExistsIndex()) {
                log.error(" idxName={} 已经存在,idxSql={}", indexName, idxSQL);
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.mapping(idxSQL, XContentType.JSON);
            request.settings(setting, XContentType.JSON); // 手工指定Setting
            CreateIndexResponse res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean indexExist() throws Exception {
        GetIndexRequest request = new GetIndexRequest(getIndexName());
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }


    @Override
    public boolean isExistsIndex() throws Exception {
        return restHighLevelClient.indices().exists(new GetIndexRequest(getIndexName()), RequestOptions.DEFAULT);
    }

    @Override
    public void buildSetting(CreateIndexRequest request, Integer shards, Integer replicas) {
        request.settings(Settings.builder().put("index.number_of_shards", shards)
                .put("index.number_of_replicas", replicas));
    }

    @Override
    public void insertOrUpdateOne(T t) {
        IndexRequest request = new IndexRequest(getIndexName());
        Gson gson = new Gson();
        log.info("Data : id={},entity={}", t.getId(), gson.toJson(t));
        request.id(t.getId());
        request.source(gson.toJson(t), XContentType.JSON);
//        request.source(JSON.toJSONString(entity.getData()), XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertBatch(List<T> list) {
        BulkRequest request = new BulkRequest();
        String idxName = getIndexName();
        Gson gson = new GsonBuilder().serializeNulls().create();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId()).source(gson.toJson(item), XContentType.JSON)));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteBatch(Collection<T> idList) {
        BulkRequest request = new BulkRequest();
        String indexName = getIndexName();
        idList.forEach(item -> request.add(new DeleteRequest(indexName, item.toString())));
        try {
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> search(SearchSourceBuilder builder) {
        String idxName = getIndexName();
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                // 距离 如果定义距离
//                BigDecimal geoDis = BigDecimal.valueOf((double) hit.getSortValues()[0]);
//                System.out.println(">>>>>>>>>>>>>>>>>>>>" + geoDis);
                res.add(new GsonBuilder().create().fromJson(hit.getSourceAsString(), getClazz()));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Set<T> searchBySet(SearchSourceBuilder builder) {
        String idxName = getIndexName();
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            Set<T> res = new HashSet<>(hits.length);
            for (SearchHit hit : hits) {
                // 距离 如果定义距离
//                BigDecimal geoDis = BigDecimal.valueOf((double) hit.getSortValues()[0]);
//                System.out.println(">>>>>>>>>>>>>>>>>>>>" + geoDis);
                res.add(new GsonBuilder().create().fromJson(hit.getSourceAsString(), getClazz()));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    @Override
    public T searchById(String id) {
        SearchRequest request = new SearchRequest(getIndexName());
        SearchSourceBuilder builder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("id", id);
        builder.query(termQueryBuilder);
        request.source(builder);
        try {
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            return new GsonBuilder().create().fromJson(response.getHits().getAt(0).getSourceAsString(), getClazz());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("searchById error:", e);
        }
        return null;
    }

    @Override
    public int searchCount(QueryBuilder builder) {
        CountRequest countRequest = new CountRequest(getIndexName());
        countRequest.query(builder);
        try {
            CountResponse response = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
            return (int) response.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void deleteIndex() {
        String idxName = getIndexName();
        try {
            if (!this.indexExist()) {
                log.error(" idxName={} 已经存在", idxName);
                return;
            }
            restHighLevelClient.indices().delete(new DeleteIndexRequest(idxName), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void deleteByQuery(QueryBuilder builder) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(getIndexName());
        request.setQuery(builder);
        //设置批量操作数量,最大为10000
        request.setBatchSize(10000);
        request.setConflicts("proceed");
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

```
