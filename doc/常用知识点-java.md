# 常用

## Java

---

### HashMap底层实现

- java 1.8 之前使用数组和链表结合一起使用，也就是**链表散列**。**HashMap通过key的hashCode经过扰动函数处理过后得到hash值，然后通过(n-1) & hash 判断当前元素存放的位置(n指数组的长度)，如果当前位置存在元素的话，就判断该元素与要存入元素hash值以及key是否相同，如果相同的话直接覆盖，不相同使用拉链法解决冲突**  </br>
**所谓扰动函数就是hashMap的hash方法。使用hash方法也就是扰动函数是为了防止一些实现比较差的hashCode()方法 换句话说使用扰动函数之后可以减少碰撞**
java1.8的hash源码：

```java
static final int hash(Object key) {
    int h;
    // key.hashCode(): 返回散列值也就是hashCode
    // ^: 按位异或
    // >>>: 无符号右移，忽略符号位，空位都用0补齐
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

对比java1.7的#hash源码:

```java
static int hash(int h) {
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).

    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
```

相对于JDK1.8的hash方法，JDK1.7的方法性能会稍差一点，因为扰动了4次</br>

所谓“**拉链法**“就是：将链表和数组相结合。也就是说创建一个链表数组，数组中每一格就是一个链表。如果遇到哈希冲突，将冲突的值加入链表中即可

#### JDK1.8之后

相对于之前的版本，jdk1.8在解决哈希冲突时有了较大的变化，当链表长度大于阈值（默认为8）（将链表转换为红黑树前会判断，如果当前数组长度小于64，那么先进行数组扩容，而不是转换为红黑树）时，将链表转换为红黑树，以减少搜索时间。

> TreeMap, TreeSet以及JDK1.8之后的HashMap的底层都用到了红黑树，以解决二叉查找树的缺陷，因为二叉查找树在某些情况下会退化成一个线性结构

#### HashMap为什么是2的幂次方

为了让HashMap存取高效，尽量减少碰撞，也就是要尽量把数据分配均匀。Hash值的取值范围为-2147483648~2147483647之间，前后加起来大概40亿的映射空间，只要哈希函数映射的比较均匀松散，一般应用是很难出现碰撞的。但一个40亿长度的数组，内存是无法放下的。所以这个散列值不能直接拿来用的。用之前还要先要做对数组长度取模运算，得到余数才能用来要存放位置也就是数组下标。这个数组下标的计算方法是```(n - 1) & hash```。（n代表数组长度）这也解释了HashMap的长度为什么是2的幂次方

##### 这个算法如何设计呢

我们首先可能会想到采用%取余的操作来实现。但是，重点来了：**取余（%）操作中是如果除数是2的幂次则等价于与除数减一的与（&）操作（也就是说 hash%length==hash&(length-1)的前提是 length 是 2 的 n 次方；）**，并且**采用二进制位操作&，相对于%能提高运算效率，这也就解释了HashMap为什么是2的幂次方**

#### HashMap多线程死循环问题

主要原因是在于并发的rehash会造成元素之间形成一个循环链表，不过在jdk1.8解决了这个问题，但是还是不建议在多线程下使用HashMap,因为多线程下使用HashMap还是会造成问题必比如数据丢失。并发环境下推荐使用ConcurrentHashMap

#### ConcurrentHashMap和Hashtable的区别

ConcurrentHashMap和Hashtable的区别主要体现在线程安全的方式上不同。

- **底层数据结构**：JDK1.7的ConcurrentHashMap采用**分段的数组+链表**实现JDK1.8采用的数据结构跟HashMap一样，数组+链表/红黑树二叉树。Hashtable和jdk1.8之前的HashMap的底层数据结构类似都是采用数组+链表的形式，数组是HashMap的主体，链表主要是为了解决哈希冲突而存在的
- **实现线程安全的方式（重要）：①在jdk1.7的时候，ConcurrentHashMap(分段锁)**，对整个桶数组进行分段分割（Segment），每一把锁只锁容器中一部分数据，多线程访问容器里不同的数据段的数据，就不会存在锁竞争，提高并发访问率、**到了jdk1.8已经摒弃了Segment的概念，而是直接用Node数组+链表+红黑树的数据结构来实现，并发控制使用synchronize和CAS来操作。（jdk1.6对synchronize做了很多优化）**，整个看起来就像是优化过且线程安全的HashMap,虽然在jdk1.8中还能看到Segment的数据结构，但是已经简化了属性，为了兼容旧版本；②**HashTable（同一把锁）**:使用synchronize保证线程安全，效率非常低下。当一个线程访问同步方法时，其他线程也访问同步方法，可能会进入阻塞或轮询状态，如果使用put添加元素，另一个线程不能使用put添加元素，也不能使用get，竞争会越来越激烈效率越低

#### ConcurrentHashMap线程安全的具体实现方式/底层实现原理

##### JDK1.7

首先将数据分为一段段存储，然后给没每一段数据配一把锁，当一个线程占用锁访问其中一个段数据时，其他段的数据也能被其他线程访问</br></br>
**ConcurrentHashMap是由Segment数组结构和HashEntry数组结构构成.**</br></br>
Segment实现了ReentrantLock，所以Segment是一种可重入锁，扮演锁的角色。HashEntry用于存储键值对数据

```java
static class Segment<K, V> extends ReentrantLock implements Serializable {
}
```

一个ConcurrentHashMap里包含一个Segment数组。Segment的结构和HashMap类似，是一个数组和链表结构，一个Segment包含一个HashEntry数组，每个HashEntry是一个链表结构的元素，每个Segment守护着一个HashEntry数组里面的元素，当对HashEntry的数据进行修改时，必须首先获得对应的Segment的锁

##### JDK1.8

ConcurrentHashMap取消了Segment分段锁，采用CAS和synchronize来保证并发安全。数据结构跟HashMap1.8的结构类似，数组+链表/红黑二叉树。Java8在链表长度超过一个阈值（8）时将链表（寻址时间复杂度为O(N)）转换为红黑树（寻址时间复杂度为（O(log(N))））</br></br>
synchronize只锁定当前链表或红黑二叉树的首节点，这样只要hash不冲突，就不会产生并发，效率提升

### Collections工具类

Collections常用方法

1. 排序
1. 查找，替换操作
1. 同步控制（不推荐，需要线程安全的集合类型是请考虑使用JUC包下的并发集合）

#### 排序操作

```java
void reverse(List<?> list) //反转
void shuffle(List<?> list) // 随机排序
void sort(List list) //按自然排序的升序排序
void sort(List list, Comparator c) //定制排序，由Comparator控制排序逻辑
void swap(List list, int i , int j) //交换两个索引位置的元素
void rotate(List list, int distance) //旋转。当distance为正数时，将list后distance个元素整体移到前面。当distance为负数时，将 list的前distance个元素整体移到后面
```

#### 查找，替换操作

```java
int binarySearch(List list, Object key) //对List进行二分查找，返回索引，注意List必须是有序的
int max(Collection coll) //根据元素的自然顺序，返回最大的元素。 类比int min(Collection coll)
int max(Collection coll, Comparator c) //根据定制排序，返回最大元素，排序规则由Comparatator类控制。类比int min(Collection coll, Comparator c)
void fill(List list, Object obj) // 用指定的元素代替指定list中的所有元素。
int frequency(Collection c, Object o) //统计元素出现次数
int indexOfSubList(List list, List target) //统计target在list中第一次出现的索引，找不到则返回-1，类比int lastIndexOfSubList(List source, list target).
boolean replaceAll(List list, Object oldVal, Object newVal) // 用新元素替换旧元素
```

### 其他重要问题

#### 什么是快速失败（fail-fast）

**快速失败（fail-fast）**是Java集合的一种错误检测机制。**在使用迭代器遍历的时候，我们在多线程下操作非安全失败（fail-false）的集合类可能会触发fail-fast机制，导致抛出```ConcurrentModificationException```异常。另外，在单线程下，如果遍历集合对象的内容进行了修改的话也会触发fail-fast机制**

> 注：增强for循环也是借助迭代器进行遍历

举个例子：多线程下，如果线程1正在对集合进行遍历，此时线程2对集合进行了修改（增加，修改，删除），或者线程1在遍历过程对集合进行修改，都会导致线程1抛出```ConcurrentModificationException```异常</br></br>
**为什么呢**</br></br>
每当迭代器使用```hasNext()/next()```遍历下一个元素之前，都会检测```modCount```变量是否为```expectedModCount```值，是的话就返回遍历；否则抛出异常，终止遍历。</br>
如果我们集合被遍历期间对其进行修改的话都会改变```modCount```值，进而导致```modCount != expectedModCount```,进而抛出```ConcurrentModificationException```异常

> 注：通过```Iterator```的方法修改集合的话会修a改到```expectedModCount```的值，所以不会抛出异常。

```java
final void checkForComodification() {
    if(modCount != expectedModCount){
        throw new ConcurrentModificationException();
    }
}
```
