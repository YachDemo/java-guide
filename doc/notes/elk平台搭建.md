# elk平台搭建

基于CentOS7，es版本为es7.5.2，使用rpm方式安装

三台机器 分别为

- 192.168.1.61
- 192.168.1.62
- 192.168.1.63

修改host文件 ```vim /etc/hosts``` 在最后添加

```yaml
192.168.1.61 node-124
192.168.1.62 node-125
192.168.1.63 node-126
```

## elasticsearch7安装

1.获取安装包 ```wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.5.2-x86_64.rpm```
2.安装 ```rpm -ivh elasticsearch-7.5.2-x86_64.rpm```
3.分别修改配置文件 ```vim /etc/elasticsearch/elasticsearch.yml```

```yaml
cluster.name: Es-Cluster #集群名称，同一个集群，名称必须相同
node.name: node-124 #节点名，与本地host保持一致即可
path.data: /EsData #数据存放位置
path.logs: /EsData/log #日志存放位置
network.host: 192.168.1.61 #ip绑定
transport.tcp.port: 9300 # tcp端口
http.port: 9200 #http端口
discovery.seed_hosts: ["node-124", "node-125", "node-126"] #es7.x 之后新增的配置，写入候选主节点的设备地址，在开启服务后可以被选为主节点
cluster.initial_master_nodes: ["node-124", "node-125", "node-126"] # es7.x 之后新增的配置，初始化一个新的集群时需要此配置来选举master
```

其他两台差不多，启动es ```service elasticsearch start```

> 不使用rpm安装的话，需要创建es专用用户来完成配置，不能使用root
>> 查看es集群状态 ```curl -XGET 'http://localhost:9200/_cluster/health?pretty'```

4.配置x-pack

```shell
cd /usr/share/elasticsearch/bin
# 生成秘钥
./elasticsearch-certutil ca
# 复制秘钥，秘钥分别复制到另外两台机器
cp ../elastic-certificates.p12 /etc/elasticsearch
cd /etc/elasticsearch
# 修改用户组
chown elasticsearch:elasticsearch elastic-certificates.p12
```

5.重启ES ```service elasticsearch restart```

6.es配置x-pack
修改配置文件 ```vim /etc/elasticsearch/elasticsearch.yml```

```yaml
xpack.security.enabled: true #启用x-pack
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate
xpack.security.transport.ssl.keystore.path: elastic-certificates.p12 #秘钥最好跟配置同级目录
xpack.security.transport.ssl.truststore.path: elastic-certificates.p12
```

设置密码```interactive```手动；```auto```自动

```shell
cd /usr/share/elasticsearch/bin
# 设置密码
./elasticsearch-setup-passwords interactive
```

## Kibana安装

1.获取安装包 ```wget https://artifacts.elastic.co/downloads/kibana/kibana-7.5.2-x86_64.rpm```

2.安装 ```rpm -ivh kibana-7.5.2-x86_64.rpm```

3.修改配置 ```vim /etc/kibana/kibana.yml```

```yaml
# 端口号
server.port: 5601
# 绑定端口号
server.host: "192.168.1.61"
# es服务
elasticsearch.hosts: ["http://node-124:9268","http://node-125:9268","http://node-126:9268"]
# es账号
elasticsearch.username: "elastic"
# es密码
elasticsearch.password: "168mNBwXpECcN2i0sHLB2XGgimZfGciXuYpcHgYwTKd8Km84hw6ij5LoSs2nOl4SJpK"
# 设置中文面板
i18n.locale: "zh-CN"
```

4.启动 ```service kibana start```，访问```http://192.168.1.61:5601```，输入账号密码登录

## Logstash安装

1.获取安装包 ```wget https://artifacts.elastic.co/downloads/logstash/logstash-7.5.2.rpm```

2.安装 ```rpm -ivh logstash-7.5.2.rpm```

3.修改配置文件 ```vim /etc/logstash/logstash.yml``

```yaml
xpack.monitoring.elasticsearch.username: elastic
xpack.monitoring.elasticsearch.password: 168mNBwXpECcN2i0sHLB2XGgimZfGciXuYpcHgYwTKd8Km84hw6ij5LoSs2nOl4SJpK
xpack.monitoring.elasticsearch.hosts: ["http://192.168.1.61:9268", "http://192.168.1.62:9268", "http://192.168.1.63:9268"]
```

4.添加配置 ```vim /etc/logstash/conf.d/logstash-springboot.conf```

```yaml
input {
  tcp {
    mode => "server"
    host => "0.0.0.0" #绑定ip
    port => 4560 #端口
    codec => json_lines #格式
  }
}
output {
  elasticsearch {
    hosts => "192.168.1.62:9268" #es主节点
    index => "springboot-tikbee-%{+YYYY.MM.dd}" #安装日期生成
    user => "elastic" #es账号
    password => "168mNBwXpECcN2i0sHLB2XGgimZfGciXuYpcHgYwTKd8Km84hw6ij5LoSs2nOl4SJpK" #es密码
  }
}
```

## SpringBoot集成

1.maven包

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>5.3</version>
</dependency>
```

2.logback-spring.xml配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
    <!--应用名称-->
    <property name="APP_NAME" value="tikbee"/>
    <!--日志文件保存路径-->
    <property name="LOG_FILE_PATH" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}}/logs}"/>
    <contextName>${APP_NAME}</contextName>
    <!--每天记录日志到文件appender-->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE_PATH}/${APP_NAME}-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    <!--输出到logstash的appender-->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <!--可以访问的logstash日志收集端口-->
        <destination>192.168.1.61:4560</destination>
        <encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="WARN">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```
