# Java

## 集合概述

### Java集合概述

从下图看出，在Java中除了以Map结尾的类之外，其他类都实现了```Collection```接口。

并且，以Map结尾的都实现了```Map```接口。

![集合](https://snailclimb.gitee.io/javaguide/docs/java/collection/images/Java-Collections.jpeg "集合")

### 说说List，Set，Map三者的区别

- ```List```：存储元素是有序可重复的
- ```Set```：无序且不重复
- ```Map```：使用键值对存储（key-value）存储，类似于数学上的函数y=f(x)，其中x表示key，“y”代表value，Key是无序的，value是无序的，可重复的，每个键最多映射一个值。

## 集合框架底层数据结构总结

先来看一下```Collection```接口下面的集合

### List

- ```ArrayList```：```Object[]```数组
- ```Vector```：```Object[]```数组
- ```LinkedList```：双向链表（JDK1.6之前为循环链表，JDK1.7取消了循环）

### Set

- ```HashSet```（无序，唯一）：底层基于```HashMap```实现的，底层采用```HashMap```来保存元素
- ```LinkedHashSet```：```LinkedHashSet```是```HashSet```的子类，并且内部是通过```LinkedHashMap```来实现的。有点类似于我们之前说的```LinkedHashMap```是基于```HashMap```实现的一样，不过还是有点区别的
- ```TreeSet```（有序，唯一）：红黑树（自平衡的排序二叉树）

再来看看```Map```接口下的集合。

### Map

- ```HashMap```：JDK1.8之前```HashMap```由数组+链表组成的，数组是HashMap的主体。链表则是主要为了解决哈希冲突而存在的（”拉链法“解决冲突）。JDK1.8以后在解决哈希冲突时有了较大变化，当链表长度大于阈值（默认为8）（将链表转为红黑树前会判断，如果当前数组长度小于64，那么先会选择数组扩容，而不是转换为红黑树）时，将链表转化为红黑树，以减少搜索时间
- ```LinkedHashMap```：LinkedHashMap继承自HashMap，所以他的底层仍然是基于拉链式散链结构即数组和链表或红黑树组成，另外，LinkedHashMap在上面结构的基础上，增加了一条双向链表，使得上面的结构可以保持键值对插入的顺序。同时通过对链表进行相应的操作，实现了访问顺序相关逻辑。
- ```Hashtable```：数组+链表组成的，数组是Hashtable的主体，链表则是为了解决哈希冲突而存在的
- ```TreeMap```：红黑树（自平衡的排序二叉树）

## 为什么要使用集合

当我们需要保存一组类型相同的数据的时候，我们应该是用一个容器来保存，这个容器就是数组，但是，使用数组存储对象有一定的弊端，因为我们在实际的开发中，存储的数据类型是多种多样的，于是，就出现了“集合”，集合同样也是用来存储多个数据的。

数组的缺点是一旦声明了之后，长度就不可变了；同时，声明数组的数据类型也决定了该数组存储数据的数据类型；而且，数组存储的数据是有序的，可重复的，特点单一。集合提高了数据存储的灵活性，Java集合不仅可以用来存储不同类型不同数量的对象，还可以保存具有映射关系的数据

## Iterator迭代器

### Iterator是什么

```java
public interface Iterator<E> {
    // 集合中是否还有元素
    boolean hasNext();
    // 或得集合中下一个元素
    E next();
    ...
}
```

```Iterator```对象称为迭代器（设计模式的一种），迭代器可以对集合进行遍历，但每一个集合内部的数据结构可能是不尽相同的，所以每一个集合存和取很可能是不一样的，虽然我们可以人为的在每一个类中定义```hashNext()```和```next()```方法，但这样会让整个集合体系过于臃肿。于是就有了迭代器。

迭代器是将这样的方法抽取出接口，然后在每个类的内部定义，定义自己的迭代方式，这样做规定了整个集合体系的遍历方式都是```hasNext()```和```next()```方法，使用者不管怎么实现的，会用即可。迭代器的定义为：提供一种方法访问一个容器对象中的各个元素，而又不需要暴露该对象的内部细节。

### 迭代器Iterator有啥用

```Iterator```主要用来遍历集合用的，它的特点是更加安全，因为他可以确保，在当前遍历的集合元素被更改的时候，就会抛出```ConcurrentModificationException```异常

### 如何使用

我们通过使用迭代器来遍历```HashMap```，演示一下迭代器Iterator的使用。

```java
Map<String, String> map = new HashMap<>();
map.put("1", "Java");
map.put("2", "C++");
map.put("3", "PHP");
Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
while (iterator.hasNext()) {
    Map.Entry<String, String> next = iterator.next();
    System.out.println(next.getKey() + ":" + next.getValue());
}
```

## 有哪些集合是线程不安全的？怎么解决呢

我们常用的```ArrayList```，```LinkedList```，```HashMap```，```HashSet```，```TreeSet```，```TreeMap```，```PriorityQueue```都不是线程安全的。解决办法很简单，可以使用线程安全的集合来代替。

如果你要使用线程安全的集合的话，```java.util.concurrent```包中提供了很多并发容器使用

1. ```ConcurrentHashMap```：可以看作是线程安全的```HashMap```
1. ```CopyOnWriterArrayList```：可以看作线程安全的```ArrayList```，在读多写少的的场合性能非常好，远远好用```Vector```
1. ```ConcurrentLinkedQueue```：高效的并发队列，使用链表实现。可以看作一个线程安全的```LinkendList```，这是一个非阻塞队列。
1. ```BlockingQueue```：这是一个接口，JDK内部通过链表，数组等方式实现了这个接口。表示阻塞队列，非常适合用于数据共享的通道。
1. ```ConcurrentSkipListMap```：跳表的实现。这是一个Map，使用跳表的数据结构实现快速查找。

## Collection子接口值List

### ArrayList和Vector的区别

1. ArrayList是List的主要实现类，底层使用Object[]存储，适合频繁的查找工作，线程不安全
1. Vector是List的古老实现类，底层使用Object[]存储，线程安全的。

### ArrayList与LinkedList的区别

1. **是否保证线程安全**：```ArrayList```和```LinkedList```都是不同步的，也就是不保证线程安全；
1. **底层数据结构**：```ArrayList```使用的是```Object```**数组**；```LinkedList```底层使用**双向链表**（JDK1.6之前为循环链表，JDK1.7取消了循环，注意双向链表和双向循环链表的区别，下面有介绍到！）
1. **插入和删除是否受元素位置的影响**： ① **```ArrayList```采用数组存储，所以插入和删除元素的复杂度受元素位置的影响**。比如：执行```add(E e)```方法的时候，```ArrayList```会默认将指定的元素追加到此列表的末尾，这种情况的时间复杂度就为O(1)。但是如果要在指定的位置i插入和删除元素的话（```add(int index, E element)```）时间复杂度就为O(n-1)。因为在进行上述操作的时候集合中i和第i和i个元素之后的（n-i）个元素都需要往后（前）移一位的操作。② **```LinkedList```采用链表存储，所以对于add(E e)的方法插入，删除元素的时间复杂度不受元素位置的影响，近似于O(1)，如果是在指定位置i插入和删除元素(add(int index, E element)的话，时间复杂度近似为O((n))因为需要先移动到指定位置再插入。**
1. **是否支持快速随机访问**：```LinkedList```不支持高效的随机元素访问，而ArrayList支持。快速随机访问就是通过元素的序号快速获取元素对象（对应于(```get(int index)```)方法）。
1. 内存空间是否占用：```ArrayList```的空间浪费主要体现在list列表的结尾会预留一定的容量空间，而```LinkedList```的空间花费主要则体现在它的每一个元素都需要消耗比ArrayList更多的空间（因为要存放直接后继和直接前驱以及数据）。

### 补充内容：双向链表和双向循环链表

**双向链表：** 包含两个指针一个prev指向前一个节点，一个next指向后一个节点

![双向链表](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/%E5%8F%8C%E5%90%91%E9%93%BE%E8%A1%A8.png "双向链表")

**双向循环链表：** 最后一个节点的next指向head，而head的prev指向最后一个节点，构成一个环。

![双向循环链表](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/%E5%8F%8C%E5%90%91%E5%BE%AA%E7%8E%AF%E9%93%BE%E8%A1%A8.png "双向循环链表")

#### 补充内容：RandomAccess接口

```java
public interface RandomAccess {

}
```

查看源码我们发现实际上```RandomAccess```接口什么都没有定义。所以，在我看来```RandomAccess```接口只是一个标识罢了。标识什么？标识实现这个接口的类具有随机访问功能。

在```binarySearch()```方法中，他判断传入的list是否```RandomAccess```的实例，如果是，调用```indexedBinarySearch()```方法，如果不是,调用```iteratorBinarySearch()```方法

```java
public static <T>
int binarySearch(List<? extends Comparable<? super T>> list, T key) {
    if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
        return Collections.indexedBinarySearch(list, key);
    else
        return Collections.iteratorBinarySearch(list, key);
}
```

### ArrayList扩容机制

详见笔主的这篇文章:[通过源码一步一步分析 ArrayList 扩容机制](https://github.com/Snailclimb/JavaGuide/blob/master/docs/java/collection/ArrayList-Grow.md)

## Collection子接口之Set

### comparable 和 Comparator 的区别

- ```comparable```接口实际上是出自```java.lang```包含他有一个```compareTo(Object o)```方法来排序
- ```comparator```接口实际上出自```java.util```包它有```compare(Object obj1, Object obj2)```方法排序

一般我们需要对一个集合使用自定义排序是吗，我们需要重写```compareTo()```方法或```compare()```方法，当我们需要对某一个集合实现两种排序方式，比如一个Song对象歌名和歌手名分别采用一种排序方法的话，我们可以重写compareTo()方法和自制的```Comparator```方法或者两个Comparator来实现歌手和歌名的排序，第二种代表我们只能使用参数版 ```Collections.sort()```

#### Comparator定制排序

```java
ArrayList<Integer> arrayList = new ArrayList<>();
arrayList.add(-1);
arrayList.add(3);
arrayList.add(3);
arrayList.add(-5);
arrayList.add(6);
arrayList.add(7);
arrayList.add(-9);
arrayList.add(-7);
System.out.println("原始数组：");
System.out.println(arrayList);
// void reverse(List list)：反转
Collections.reverse(arrayList);
System.out.println("Collections.reverse(arrayList):");
System.out.println(arrayList);

// void sort(List list),按自然排序的升序排序
Collections.sort(arrayList);
System.out.println("Collections.sort(arrayList):");
System.out.println(arrayList);

// 定制排序
Collections.sort(arrayList, new Comparator<Integer>() {
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }
});
System.out.println("定制排序后：");
System.out.println(arrayList);
```

Output:

```java
原始数组：
[-1, 3, 3, -5, 6, 7, -9, -7]
Collections.reverse(arrayList):
[-7, -9, 7, 6, -5, 3, 3, -1]
Collections.sort(arrayList):
[-9, -7, -5, -1, 3, 3, 6, 7]
定制排序后：
[-9, -7, -5, -1, 3, 3, 6, 7]
```

#### 重写compareTo方法实现按年龄来排序

```java
package com.example.entity;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-13 16:08
 **/
// person对象没有实现Comparable接口，所以必须实现，这样才不会出错，才可以使treemap中的数据按顺序排列
// 前面一个例子的String类已经默认实现了Comparable接口，详细可以查看String类的API文档，另外其他
// 像Integer类等都已经实现了Comparable接口，所以不需要另外实现了
public class Person implements Comparable<Person> {
    private String name;

    private int age;

    public Person(String name, int age) {
        super();
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    /**
     * 重写compareTo方法实现按年龄来排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Person o) {
        if (this.age > o.age) {
            return 1;
        }
        if (this.age < o.age) {
            return -1;
        }
        return 0;
    }
}
```

```java
TreeMap<Person, String> pdata = new TreeMap<>();
pdata.put(new Person("张三", 30), "zhangsan");
pdata.put(new Person("李四", 20), "lisi");
pdata.put(new Person("王五", 10), "wangwu");
pdata.put(new Person("小红", 5), "xiaohong");
// 得到key的值的同事获取key所对应的值
Set<Person> keys = pdata.keySet();
for (Person key : keys) {
    System.out.println(key.getAge() + "-" + key.getName());
}
```

Output:

```java
5-小红
10-王五
20-李四
30-张三
```

### 无序性和不可重复性的含义是什么

1. 什么是无序性？无序性不代表随机性，无序性是指存储的数据在底层数组中并非按照数组索引的顺序添加，而是根据数据的哈希值决定的。
1. 什么是不可重复性？不可重复性是指添加的元素按照equals()判断时，返回false，同时需要重写equals()方法和HashCode()方法。

### 比较HashSet，LinkedHashSet和TreeSet三者的异同

HashSet是Set的主要实现类，底层实现主要是HashMap，线程不安全的，可以存储null值

LinkedHashSet是HashSet的子类，能够按照添加的顺序遍历

TreeSet底层使用红黑树，能够按照添加的顺序进行遍历，排序有自然排序和定制排序

## Map接口

### HashMap和Hashtable的区别

1. **线程是否安全：** HashMap是线程不安全的，而Hashtable是线程安全的，因为Hashtable内部所有方法都基本经过```synchronize```修饰（如果要保证线程安全就使用ConcurrentHashMap吧）
1. **效率：** 因为线程安全的问题，HashMap要比Hashtable要快一点，另外Hashtable基本被淘汰，不要在代码中使用它
1. **对于null key和null value的支持：** HashMap可以存储为null的key或者value，但null作为键只能有一个，null作为值可以有多个；Hashtable不允许null键和null值，否则会抛出NPE异常
1. **初始容量大小和每次扩容大小的不同**：① 创建时如果不指定容量初始值，Hashtable默认的初始大小为11，之后每次扩容，容量变为原来的2n+1。HashMap的默认初始化大小为16。之后每次扩容，容量变为原来的两倍。② 如果创建时给了容量初始值，那么Hashtable会直接使用你给定的大小，而HashMap会将其扩充为2的幂次方大小（HashMap中的```tableSizeFor()```保证，下面给出了代码）。也就是说HashMap总是使用2的幂作为哈希表的大小，后面会介绍为什么是2的幂次方。
1. **底层数据结构：** JDK1.8以后的HashMap在解决Hash冲突时有了较大的变化，当链表长度大于阈值（默认为8）（转换为红黑树时会判断，如果当前数组长度小于64，那么会选择先进行数组扩容，而不是转换为红黑树）时，将链表转换为红黑树，以减少搜索时间。Hashtable没有这样的机制。

**HashMap中带有初始容量的构造函数：**

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                            initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                            loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}
```

下面这个方法保证了HashMap总是使用2的幂次方的大小

```java
/**
 * Returns a power of two size for the given target capacity.
 */
static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

### HashMap和HashSet的区别

如果你看过```HashSet```的源码的话应该知道：HashSet的底层实现就是基于HashMap实现的。（HashSet的源码非常少，因为除了```clone(),writeObject(), readObject()```是HashSet自己不得不实现之外吗，其他方法都是直接调用HashMap中的方法）

HashMap | HashSet
---     |   ---
实现了Map接口|实现Set接口
存储键值对 | 仅存储兑现
调用```put()```方法向map中添加元素|调用```add()```方法向set中添加元素
HashMap使用键(Key)计算HashCode|HashSet使用每个成员对象来计算HashCode值，对于两个对象来说HashCode可能相同，所以用equals()方法来判断对象相等性

### HashMap和TreeMap区别

```TreeMap```和```HashMap```都继承自```AbstractMap```，单需要注意的是```TreeMap```它还实现了```NavigableMap```接口和```SortedMap```接口。

![TreeeMap](https://snailclimb.gitee.io/javaguide/docs/java/collection/images/TreeMap%E7%BB%A7%E6%89%BF%E7%BB%93%E6%9E%84.png)

实现```NavigableMap```接口让TreeMap有了对集合内元素的搜索能力

实现```SortedMap```接口让TreeMap有了对集合元素根据键排序的功能。默认搜索按照Key的升序排序，不过我们也可以指定Key的排序器。示例代码如下

```java
import java.util.TreeMap;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-14 9:48
 **/
public class Person {
    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Person(Integer age) {
        this.age = age;
    }

    public static void main(String[] args) {
        TreeMap<Person, String> treeMap = new TreeMap<>((o1, o2) -> {
            int num = o1.getAge() - o2.getAge();
            return Integer.compare(num, 0);
        });
        treeMap.put(new Person(3), "person1");
        treeMap.put(new Person(18), "person2");
        treeMap.put(new Person(35), "person3");
        treeMap.put(new Person(16), "person4");
        treeMap.forEach((key, value) -> System.out.println(value));
    }
}
```

输出

```java
person1
person4
person2
person3
```

可以看出，```TreeMap```已经是按照```Person```的age字段的升序来排列了。

**综上，相比于```HashMap```来说```TreeMap```主要是多了对集合中的元素根据键排序的能力以及对集合内元素的搜索的能力。**

### HashSet如何检查重复

当你把对象加入HashSet时，HashSet会先计算对象的HashCode值来判断对象加入的位置，同时也会与其他加入的对象的HashCode值作比较，如果没有相符的HashCode，HashSet会假设对象没有重复的出现。但是如果发现有相同的HashCode值的对象，这时会调用```equals()```方法来检查HashCode相等的对象是否相同。如果两者相同。HashSet就不会让其加入成功

**hashCode()与equals()的相关规定**:

1. 如果两个对象相等，则hashCode一定也是相同的
1. 如果两个对象相等，对两个equals方法返回true
1. 两个对象具有相同的hashCode值，他们两个不一定相等
1. 综上，equals方法被覆盖过，则hashCode方法也必须被覆盖
1. hashCode()的默认行为是在堆上的对象产生独特值。如果没有重写hashCode()，则该两个对象无论如何都不会相等（即使这两个对象指向相同的数据）

**==和equals的区别**:

对于基本类型来说，==比较的是值是否相等；

对于引用类型来说，==比较的是两个引用是否指向同一个对象地址（两者在内存中存放的地址（堆内存地址）是否指向同一个地方）；

对于引用类型（包装类型）来说，equals如果没有被重写，对比他们的内存地址是否相同；如果equals被重写（例如String），则比较的是地址中的内容

### HashMap的底层实现

#### JDK1.8之前

jdk1.8之前```HashMap```是**数组和链表**结合在一起使用也就是**链表散列**。**HashMap通过key的hashCode经过扰动函数处理过后得到hash值，然后通过(n-1) & hash 判断当前元素存放的位置(这里n指的是数组的长度)，如果当前存在元素的话，就判断该元素与要存入的元素的hash值以及key是否相同，如果相同的话，则直接覆盖，不同的话通过拉链法解决冲突。**

所谓扰动函数

```json
C:\Java\jdk1.8.0_191\bin\java.exe -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:57783,suspend=y,server=n -javaagent:C:\Users\Administrator\AppData\Local\JetBrains\IntelliJIdea2020.2\captureAgent\debugger-agent.jar -Dfile.encoding=UTF-8 -classpath C:\Users\Administrator\AppData\Local\Temp\classpath706231261.jar com.sunsco.tool.utils.BaiduMapServiceSDK
Connected to the target VM, address: '127.0.0.1:57783', transport: 'socket'
16:52:39.013 [main] DEBUG org.springframework.web.client.RestTemplate - HTTP GET http://api.map.baidu.com/place_abroad/v1/suggestion?query=%E6%9B%BC%E5%85%8B%E9%A0%93&region=%E6%BE%B3%E9%97%A8&output=json&ak=Zfn6nMSk5Ca18AAcmFvqvxF91ZgSIswO
16:52:39.033 [main] DEBUG org.springframework.web.client.RestTemplate - Accept=[text/plain, application/json, application/cbor, application/*+json, */*]
16:52:39.042 [main] DEBUG org.springframework.web.client.RestTemplate - Writing [{}] as "application/x-www-form-urlencoded"
16:52:39.171 [main] DEBUG org.springframework.web.client.RestTemplate - Response 200 OK
16:52:39.172 [main] DEBUG org.springframework.web.client.RestTemplate - Reading to [java.lang.String] as "text/javascript;charset=utf-8"
{
    "status":0,
    "message":"ok",
    "result":[
        {
            "name":"曼克顿(第一座)",
            "location":{
                "lat":22.160131,
                "lng":113.571005
            },
            "uid":"5dfffd00290410696e3e1b7c",
            "city":"澳门特别行政区",
            "district":"氹仔岛",
            "business":"",
            "cityid":"2911",
            "tag":"西餐厅",
            "address":"澳门特别行政区嘉模堂区孙逸仙博士大马路西50米"
        },
        {
            "name":"曼克顿山",
            "location":{
                "lat":22.338182,
                "lng":114.153847
            },
            "uid":"45a662c38714679d54bfffa4",
            "city":"香港特别行政区",
            "district":"深水埗区",
            "business":"",
            "cityid":"2912",
            "tag":"住宅区",
            "address":"香港特别行政区深水埗区宝轮街1号"
        },
        {
            "name":"曼克顿广场",
            "location":{
                "lat":22.323182,
                "lng":114.221323
            },
            "uid":"acb68f99efe223dd0a1cc76e",
            "city":"香港特别行政区",
            "district":"观塘区",
            "business":"",
            "cityid":"2912",
            "tag":"写字楼",
            "address":"香港特别行政区九龙观塘区湾宏泰道23号"
        },
        {
            "name":"曼克顿",
            "location":{
                "lat":22.316189,
                "lng":114.235902
            },
            "uid":"6ec7452194fb88054acc4a6b",
            "city":"香港特别行政区",
            "district":"观塘区",
            "business":"",
            "cityid":"2912",
            "tag":"西餐厅",
            "address":"香港观塘"
        },
        {
            "name":"曼克顿花园",
            "location":{
                "lat":22.232084,
                "lng":114.228519
            },
            "uid":"81f5a38ab6f2d35bad31e5e3",
            "city":"香港特别行政区",
            "district":"南区",
            "business":"",
            "cityid":"2912",
            "tag":"住宅区",
            "address":"香港南区赤柱大潭道33号"
        },
        {
            "name":"曼克顿大厦",
            "location":{
                "lat":22.242861,
                "lng":114.200678
            },
            "uid":"5aee51367f6bf160f4b91685",
            "city":"香港特别行政区",
            "district":"南区",
            "business":"",
            "cityid":"2912",
            "tag":"住宅区",
            "address":"香港特别行政区南区浅水湾道63号"
        },
        {
            "name":"曼克顿 裁缝",
            "location":{
                "lat":22.306162,
                "lng":114.183458
            },
            "uid":"429b02aa0c0014f8b7147f4c",
            "city":"香港特别行政区",
            "district":"油尖旺区",
            "business":"",
            "cityid":"2912",
            "tag":"裁缝店",
            "address":"曼克顿 裁缝 Mercantile House, Unit E, Ground Opposite Tsim Sha "
        },
        {
            "name":"曼克顿山5座",
            "location":{
                "lat":22.338436,
                "lng":114.153972
            },
            "uid":"6ef28d331bdd32f0f6e73b36",
            "city":"香港特别行政区",
            "district":"深水埗区",
            "business":"",
            "cityid":"2912",
            "tag":"写字楼",
            "address":"香港特别行政区深水埗区荔枝角宝轮街1号"
        },
        {
            "name":"Jag - 曼克顿山会所餐厅",
            "location":{
                "lat":22.337466,
                "lng":114.153939
            },
            "uid":"0b555e14d240c06420026255",
            "city":"香港特别行政区",
            "district":"深水埗区",
            "business":"",
            "cityid":"2912",
            "tag":"西餐厅",
            "address":"油尖旺区荔枝角宝轮街1号曼克顿山会所5楼"
        },
        {
            "name":"曼克顿照明",
            "location":{
                "lat":22.280259,
                "lng":114.191438
            },
            "uid":"15055a9cb51b7a184107c19b",
            "city":"香港特别行政区",
            "district":"湾仔区",
            "business":"",
            "cityid":"2912",
            "tag":"家居建材",
            "address":"湾仔"
        }
    ]
}
```
