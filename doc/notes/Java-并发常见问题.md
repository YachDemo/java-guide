# Java并发

## synchronize关键字

### 说说你对synchronized关键字的了解

**```synchronized```关键字解决的是多个线程访问资源的同步性，synchronize关键字可以保证被他修饰的方法或代码块在任意时刻只能有一个线程执行。**

另外，在Java早期版本中，synchronize属于**重量级锁**，效率低下。

**为什么呢？**

因为监视器（monitor）是依赖于底层操作系统```Mutex Lock```来实现的，Java的线程是映射到操作系统的原生线程之上的。如果挂起或者唤醒一个线程，都需要操作系统帮忙完成，而操作系统实现线程之间的切换需要从用户态转换成内核态，这个状态之间的转换需要较长的时间，时间成本相对较高。

庆幸的是Java 6之后Java官方对从JVM层面对synchronized较大优化，所以现在的synchronized锁优化的效率也不错了。JDK1.6对锁的引入做了较优化，比如自旋锁，适应性自旋锁，锁消除，锁粗化，偏向锁，轻量级锁等技术来减少锁操作的开销。

所以，你会发现目前的话，不论是各种开源框架还是JDK源码都大量使用了synchronized关键字

**```synchronized```关键字最主要解决的三种使用方式：**

1. 修饰访问的实例方法：作用于当前对象实例加锁，进入同步方法时加锁

### 说说自己怎么使用synchronized关键字

**synchronized关键字最主要的三种使用方式：**

1.**修饰实例方法：** 作用于当前对象示例加锁，进入同步代码之前要获得**当前对象示例的锁**

```java
synchronized void method() {
    // 业务代码
}
```

2.**修饰静态方法：** 也就是给当前类加锁，会作用类的所有对象实例，进入同步代码前要获得**当前class的锁**。因为静态成员不属于任何一个实例对象，是类成员（static表名该类是个静态资源，不管new多少个对象，只有一份）。所以，如果一个线程A调用一个实例对象非静态```synchronized```方法，而线程B需要调用这个实例对象所属类的静态```synchronized```是允许的，不会发生互斥现象，**因为访问静态```synchronized```方法占用的锁是当前类的锁，而非访问非静态```synchronized```方法占用的是当前实例对象的锁**

```java
synchronized void static method(){
    // 业务代码
}
```

3.**修饰代码块：** 指定加锁对象，给指定的类/对象加锁。```synchronized(this|object)```表示进入同步代码库前要获得**给定对象的锁**。```synchronized(类.class)```表示进入同步代码前要获得**当前class的锁**

```java
synchronized(this){
    // 业务代码
}
```

**总结：**

- ```synchronized```关键字加到```static```静态方法和```synchronized(class)```代码块上都是给Class类上锁。
- ```synchronized```关键字加到实例方法上就是给对象实例上锁。
- 尽量不要用```synchronized(String a)```因为JVM中，字符串常量具有缓存功能

下面我以一个常见的面试题为例讲解一下```synchronized```。

面试中面试官经常会说：单例模式了解过吗？给我手写一下！给我解释一下双重检验锁实现单例模式的原理

**双重检验锁实现对象单例（线程安全）。**

```java
public class Singleton{
    private volatile static Singleton uniqueInstance;

    public Singleton(){
    }

    public static Singleton getUniqueInstance() {
        // 判断对象是否已经实例化过，没有实例化过的才进入加锁代码
        if(uniqueInstance == null) {
            // 对象加锁
            synchronized(Singleton.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new Singleton();
                }
            }
        }
        return uniqueInstance;
    }

}
```

另外，需要注意```uniqueInstance```采用```volatile```关键字修饰也是很有必要

```uniqueInstance = new Singleton()``` 这段代码其实是分为三步执行：

1. 为```uniqueInstance```分配内存空间
1. 初始化```uniqueInstance```
1. 将```uniqueInstance```指向分配的内存地址

但是由于JVM具有指令重排的特性，执行顺序可能变成1->3->2。指令重排在单线程环境下不会出现问题，但是在多线程环境下会导致一个线程获得还没初始化的实例。例如，线程T1执行了1和3，此时T2调用```getUniqueInstance()```后发现```uniqueInstance```不为空，因此返回```uniqueInstance```，但此时```uniqueInstance```还未被初始化。

使用```volatile```可以禁止JVM的指令重排，保证在多线程环境下也能正常运行。

### 构造方法也能用synchronized关键字修饰吗

先说结论：**构造方法不能用synchronized关键字修饰**

构造方法本省就是线程安全的，不存在同步构造方法这一说法

### 讲一下synchronized关键字的底层原理

**synchronized关键字底层原理属于JVM层面。**

#### synchronized同步语句块情况

```java
public class SynchronizedDemo {
    public void method() {
        synchronized(this) {
           System.out.print("synchronized 代码块");
        }
    }
}
```

使用JDK自带的```javap```命令查看```SynchronizedDemo```类的相关字节码信息：首先切换到类的对应目录执行```javac SynchronizedDemo.java```命令生成.class文件，然后执行```javap -c -s -v -l SynchronizedDemo.class```

![执行信息](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/synchronized%E5%85%B3%E9%94%AE%E5%AD%97%E5%8E%9F%E7%90%86.png)

从上面我们可以看出：

**```synchronized```同步语句块实现使用的是```monitorenter```和```monitorexit```指令，其中```monitorenter```指令指向同步代码块的开始位置，```monitorexit```指令则指明同步代码块的结束位置**

当执行```monitorenter```指令时，线程试图获取对象**监视器monitor**的持有权。

> 在Java虚拟机（HotSpot）中，Monitor是基于C++实现的，由ObjectMonitor实现的，没个对象中都内置了一个ObjectMonitor对象。
> 另外，**wait/notify**等方法也依赖于monitor对象，这就是为什么只有在同步的块或方法中才能调用wait/notify等方法，否则就会抛出java.lang.IllegalMonitorStateException的原因

#### synchronized修饰方法的情况

```java
public class SynchronizedDemo {
    public synchronized void method() {
        System.out.print("synchronized 代码块");
    }
}
```

![执行信息](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/synchronized%E5%85%B3%E9%94%AE%E5%AD%97%E5%8E%9F%E7%90%862.png)

```synchronized```修饰的方法并没有```monitorenter```指令和```monitorexit```指令，取而代之的是却是```ACC_SYNCHRONIZED```标识，该标识指明了方法是一个同步方法，JVM通过该```ACC_SYNCHRONIZED```访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。

#### 总结

```synchronized```同步代码块使用的是```monitorenter```指令和```monitorexit```指令，其中```monitorenter```指令指向同步代码块的开始位置，```monitorexit```指令则指向同步代码块的结束位置

```synchronized```修饰的方法并没有```monitorenter```指令和```monitorexit```指令，取而代之的是```ACC_SYNCHRONIZED```标识，该标识指明了该方法是一个同步方法

**不过两者的本质都是对对象监视器monitor的获取。**

### 说说JDK1.6之后的synchronized关键字对底层做了什么优化，可以详细介绍一下这些优化吗

JDK1.6对锁的实现引入了大量的优化，如偏量锁，轻量级锁，自旋锁，适应性自旋锁，锁消除，锁粗化等技术来减少锁操作的开销。

锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态。他们会随着竞争的激烈而逐渐升级。注意锁可以升级不可以降级，这种策略是为了提高获得锁和释放锁的效率。

### 谈谈synchronized和ReentrantLock的区别

#### 两者都是可重入锁

**可重入锁**指的是自己可以再次获得自己的内部锁。比如一个线程获得了某个对象的锁，此时这个对象锁还没释放，当其再次想要获取这个对象的锁的时候还是可以获取的，如果不可重入锁的话，就会造成死锁。同一个线程每次获取锁，锁的计数器都会自增1，所以要等到锁的计数器下降为0时才能释放锁。

#### synchronized依赖于JVM而ReentrantLock依赖于API

```synchronized```是依赖于JVM实现的，前面我们也讲到了虚拟机团队在JDK1.6为```synchronized```关键字做了很多优化，但是这些优化都是在虚拟机层面实现的，并没有直接暴露给我们。ReentrantLock是JDK层面实现的（也就是api层面，需要lock()和unlock()方法配合try/finally语句块来实现），所以我们可以通过查看它的源码，来看它如何实现的。

#### ReetrantLock比synchronized增加了一些高级功能

相比于```synchronized```，```ReetrantLock```增加了一些高级功能。主要来说主要有三点

- **等待可中断：** ```ReetrantLock```提供了一种能够中断锁等待的线程机制。通过```lock.lockInterruptibly();```来实现这个机制。也就是说正在等待的线程可以选择放弃等待，改为处理其他事情。
- **可实现公平锁：** ```ReetrantLock```可以指定是公平锁还是非公平锁。而```synchronized```只能是非公平锁。所谓公平锁就是先等待线程先获取锁。```ReetrantLock```默认是非公平的，可以通过```ReetrantLock```类的```ReentrantLock(boolean fair)```构造方法来指定实现是否公平
- **可实现选择性通知（锁可以绑定多个条件）：** ```synchronized```关健字与```wait()```和```notify()/notifyAll()```方法结合可以实现等待/通知机制。```ReetrantLock```类当然也可以实现，但是需要借助```Condition```接口与```newCondition()```方法

> ```Condition```是JDK1.5才有的，它具有很好的灵活性，比如实现多路通知功能也就是在一个Lock对象中可以创建多个```Condition```实例（即对象监视器）。**线程对象可以注册在指定的```Condition```中，从而可以有选择性的进行线程通知，在调度线程上更加灵活。在使用```notify()/notifyAll()```方法进行通知时，被通知的线程是由JVM选择的，用```ReetrantLock```类结合```Condition```实例可以实现选择性通知**，这个功能非常重要，而且是```Condition```接口默认提供的。而```synchronized```关键字就相当于整个Lock对象中只有一个```Condition```实例，所有线程都注册在它一个身上。如果执行```notifyAll()```方法的话就会通知所有处于等待状态的线程，这样会造成很大的效率问题，而```Condition```实例的```signalAll()```方法则只会唤醒注册在该```Condition```实例中的所有等待线程。

**如果你想使用上述功能，那么选择ReetrantLock是一个不错的选择。性能已不是选择标准。**

## volatile关键字

我们先从**CPU缓存模型**说起

### CPU缓存模型

**为什么要弄一个CPU高速缓存呢？**

类似于我们开发网站后台用的缓存（比如Redis）是为了解决程序处理速度和访问常规关系型数据库速度不对等的问题。**CPU缓存则是为了解决CPU处理速度和缓存处理速度不对等的问题**

我们甚至可以把**内存看作外存的高速缓存**，程序运行的时候我们把外存的数据复制带内存，由于内存的处理速度远远高于外存，这样就提高了处理速度。

总结：**CPU Cache缓存是内存数据用于解决CPU处理速度和内存不匹配的问题，内存缓存的是硬盘数据用于解决硬盘访问速度过慢的问题。**

为了更好的理解，我画了一个简单的CPU Cache示意图如下:(实际上，现代CPU Cache通常分为三层，分别叫L1，L2，L3 Cache)

![CPU](https://guide-blog-images.oss-cn-shenzhen.aliyuncs.com/2020-8/303a300f-70dd-4ee1-9974-3f33affc6574.png)

**CPU Cache的工作方式：**

先复制一份数据到CPU Cache中，当CPU需要用到的时候直接从CPU Cache中读取数据，当运算完成后，再将运算得到的数据写回Main Memory中。但是，这样同样存在**内存缓存不一致的问题**比如我执行一个i++操作的话，如果两个线程同时执行的的话，假设两个线程从CPU Cache中读取i=1，两个线程做了i++运算完之后在写回Main Memory之后i=2，而正确结果为i=3。

**CPU为了解决内存缓存不一致问题可以通过制定缓存协议或者其他手段来解决。**

### 讲一下JMM（Java内存模型）

在JDK1.2之前，Java内存模型实现总是从**主存**（即共享内存）读取变量，是不需要特别注意的。而在当前的Java内存模型下，线程可以把变量保存在**本地内存**（比如机器的寄存器中），而不是直接从主存中进行读写。这就造成了一个线程在主存中修改了一个变量的值，而另外一个线程还在继续使用它在寄存器中的变量值的拷贝，造成**数据的不一致**

![模型](https://guide-blog-images.oss-cn-shenzhen.aliyuncs.com/2020-8/0ac7e663-7db8-4b95-8d8e-7d2b179f67e8.png)

要解决这个问题，就需要把变量声明为```volatile```，这就指示JVM，这个变量是共享且不稳定的，每次使用它都是到主存中进行读取。

**所以，```volatile```关健字除了防止JVM的指令重排，还有一个重要作用就是保证变量可见性。**

![volatile模型](https://guide-blog-images.oss-cn-shenzhen.aliyuncs.com/2020-8/d49c5557-140b-4abf-adad-8aac3c9036cf.png)

### 并发的编程的三个重要特性

1. **原子性：**一个操作或者多次操作，要么所有的操作全部得到执行并且不会收到任何因素的干扰而中断，要么所有操作都执行，要么都不执行。```synchronized```可以保证代码片段的原子性
1. **可见性：**当一个变量对共享变量进行了修改，那么了另外的线程都是立即可以看到修改后的最新值。```volatile```关键字可以保证共享变量的可见性
1. **有序性：**代码在执行过程中的先后顺序，java在编译器以及运行期间的优化，代码的执行顺序未必就是编写代码时候的顺序。```volatile```关键字可以禁止指令进行重排序优化。

### 说说synchronized关键字和volatile关键字的区别

```synchronized```和```volatile```是两个互补的存在，而不是对立的存在

- **```volatile```关键字**是线程同步的**轻量级实现**，所以**volatile性能肯定是比synchronized关键字要好**。但是**volatile关键字只能用于变量而```synchronized```关键字可以修饰方法以及代码块**
- **volatile关键字能保证数据的可见性，但不能保证数据的原子性。synchronized两者都能保证**
- **volatile关键字主要用于解决变量在多个线程之间的可见性，而synchronized关键字解决的是多个线程之前访问资源的同步性**

## ThreadLocal

### ThreadLocal简介

通常情况下，我们创建的变量是可以被任何一个线程访问并修改的。如果想实现每一个线程都有自己的本地专属变量该任何解决呢？JDK中提供的ThreadLocal类正是为了解决这样的问题。**```ThreadLocal```类主要解决的就是让每个线程绑定自己的值，可以将ThreadLocal类形象的比喻成存放数据的盒子，盒子中可以存储每个线程的私有数据。**

**如果你创建了一个```ThreadLocal```变量，那么访问这个变量的每个线程都会有这个变量的本地副本，这也是```ThreadLocal```变量名的由来。他们可以使用```get()```和```set()```方法来获取默认值或将其值改为当前线程所存的副本的值。从而避免了线程安全的问题。**

### ThreadLocal原理

从```Thread```类源代码入手

```java
public class Thread implements Runnable {
    ......
    //与此线程有关的ThreadLocal值。由ThreadLocal类维护
    ThreadLocal.ThreadLocalMap threadLocals = null;



    //与此线程有关的InheritableThreadLocal值。由InheritableThreadLocal类维护
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
    ......
}
```

从上面```Thread```类源代码可以看出```Thread```类中有一个```threadLocals```和一个```inheritableThreadLocals```变量，他们都是```ThreadLocalMap```类型的变量，我们可以把```ThreadLocalMap```理解成```ThreadLocal```类实现的定制化的HashMap。默认情况下这两个变量都是null，只有当前线程调用ThreadLocal类的set或get方法时才创建他们，实际上调用这两个方法的时候，我们调用的是```ThreadLocalMap```的```get()```、```set()```方法。

```ThreadLocal```类的```set()```方法

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if(map != null) {
        map.set(this, value);
    } else {
        createMap(t, value);
    }
}
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```

通过上面这些内容，我们足可以通过猜测得出结论：**最终的变量是放在了当前线程的```ThreadLocalMap```中，而不是存在```ThreadLocal```上，```ThreadLocal```可以理解为只是```ThreadLocalMap```的封装，传递了变量值。**```ThreadLocal```类中可以通过```Thread.currentThread()```获取到当前线程对象后，直接通过```getMap(Thread t)```可以访问到该线程的```ThreadLocalMap```对象。

**每个Thread中都具备一个ThreadLocalMap，而ThreadLocalMap可以存储以ThreadLocal为key，Object对象为value的键值对。**

```java
ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue){
    .....
}
```

比如我们在同一个线程中声明了两个```ThreadLocal```对象的话，会使用```Thread```内部都是使用那个仅有那个```ThreadLocalMap```存放数据的，```ThreadLocalMap```的key就是```ThreadLocal```对象，value就是调用```ThreadLocal```对象调用set方法设置的值

### ThreadLocal内存泄漏问题

```ThreadLocalMap```中使用的key为```ThreadLocal```的弱引用。所以，如果```ThreadLocal```没有被外部强引用的的情况下，在垃圾回收的时候，key会被清理掉，而value不会被清理掉，这样一来，```ThreadLocalMap```就会出现key为null的Entry。假如我们不做任何措施的话，value永远无法被GC回收，这个时候可能会产生内存泄漏。```ThreadLocalMap```实现中已经考虑了这种情况，在调用```set()、get()、remove()```的时候，会清理掉key为null的记录。使用完```ThreadLocal```后最好手动调用```remove()```方法

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
    }
}
```

**弱引用介绍：**

> 如果一个对象具有弱引用，那就类似于**可有可无的生活用品**。弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域过程中，一旦发现了只局域弱引用的对象，不管当前内存空间是否足够与否，都会回收它的内存，不过，由于垃圾回收器是一个优先级很低的线程，因此不一定很快发现那些只具有弱引用的对象</br></br>弱引用可以和一个引用队列(ReferenceQueue)联合使用，如果弱引用所引用的对象被垃圾回收，Java虚拟机就会把这个弱引用加入到与之关联的引用队列中去

## 线程池

### 为什么要使用线程池

> **池化技术大家已经屡见不鲜了，线程池，数据库连接池，Http连接池等等都是对这个思想的应用。池化技术的思想主要是为了减少每次获取资源的开销，提高对资源的利用率。**

**线程池**提供看一种限制和管理资源（包括执行了一个任务）。每个**线程池**还维护了一些基本统计信息，例如已经完成任务数量。

这里借用《java并发编程艺术》提到的来说一下**使用线程池的好处：**

- **降低资源消耗**。通过重复利用已创建的的线程降低线程创建和销毁造成的损耗。
- **提高响应速度**。当任务到达时，任务可以不需要等到线程创建就能立即执行。
- **提高线程的可管理性**。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控

### 实现Runnable接口和Callable接口的区别

```Runnable```自Java1.0来一直存在，但```Callable```仅在Java1.5中引用，目的就是为了处理```Runnable```不支持的用例。**```Runnable```接口不会返回结果活或抛出检查异常，但```Callable```接口可以。所以，如果任务不需要返回结果或者抛出异常推荐使用```Runnable```接口**，这样代码看起来更简洁

工具类```Executors```可以实现```Runnable```对象和```Callable```对象之间的相互转换（```Executors.callable(Runnable task)```或```Executors.callable(Runnable task，Object resule)```）。

```Runnable.java```

```java
@FunctionalInterface
public interface Runnable {
    /**
     * 线程被执行时，没有返回值时也无法返回异常
     */
    public abstract void run();
}
```

```Callable.java```

```java
@FunctionalInterface
public interface Callable<V> {
    /**
     * 计算结果，或在无法这样做时抛出异常。
     * @return 计算得出的结果
     * @throws 如果无法计算结果，则抛出异常
     */
    V call() throws Exception;
}
```

### 执行execute()方法和submit()方法的区别是什么

1. **```execute()```方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功与否；**
1. **```submit()```方法用于提交需要返回值的任务。线程池会返回一个```Future```类型的对象，通过这个```Future```对象可以判断任务是否执行成功**，并且可以通过```Future```的```get()```方法来获取返回值，```get()```方法会阻塞当前线程直到任务完成，而使用```get(long timeout, TimeUnit unit)```方法会阻塞当前线程一段时间后立即执行，这时候有可能没有执行完。

我们以```AbstractExecutorService```接口中一个submit方法为例子来看看源码：

```java
public Future<?> submit(Runnable task) {
    if (task == null) throw new NullPointException();
    RunnableFuture<Void> ftask = newTaskFor(task, null);
    execute(ftask);
    return ftask;
}
```

上面方法调用```newTaskFor```方法返回一个```FutureTask```对象。

```java
protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    return new FutureTask<T>(runnable, value);
}
```

我们再来看看```execute```方法

```java
public void execute(Runnabel command) {
    ...
}
```

### 如何创建线程池

《阿里巴巴Java开发手册》中强制不能使用Executors去创建，而是通过ThreadPoolExector的方式，这样处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险

> Executors返回线程池的弊端如下：
>
> - FixedThreadPool和SingleThreadExecutor：允许请求队列长度为Integer.MAX_VALUE，可能堆积大量的请求，从而导致OOM
> - CachedThreadPool和ScheduledThreadPool；允许创建的线程数量为Integer.MAX_VALUE，可能创建大量线程，从而导致OOM

### ThreadPoolExecutor类分析

```ThreadPoolExecutor```类中提供的四个构造方法。我们来看最长的那个，其余三个都是在这个构造方法的基础上产生的（其他几个构造方法说白点都是给某些默认参数的的构造方法比如默认制定拒绝策略是什么）

```java
/**
 * 用给定的初始参数创建一个新的ThreadPoolExecutor.
 */
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> wordQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if(corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0)
           throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
           throw new NullPointerException();
     this.corePoolSize = corePoolSize;
     this.maximumPoolSize = maximumPoolSize;
     this.workQueue = workQueue;
     this.keepAliveTime = unit.toNanos(keepAliveTime);
     this.threadFactory = threadFactory;
     this.handler = handler;
}
```

**下面这些对创建非常重要，在后面使用线程池的过程中你一定会用到！所以，务必拿着小本本记好。**

#### ```ThreadPoolExecutor```构造函数重要参数分析

**```ThreadPoolExecutor```3个最重要的参数**

- ```corePoolSize```：核心线程数，线程定义了最小可以同时运行的线程数量。
- ```maximumPoolSize```：当队列中存放的任务达到队列容量的时候，当前可以同时运行的线程数量变为最大线程数。
- ```workQueue```：当新任务来的时候会判断当前运行的线程数量是否达到核心线程数，如果达到的话，新任务就会存放到队列中

```ThreadPoolExecutor```其他常见参数：

1. ```keepAliveTime```:当线程池中线程数量大于corePoolSize的时候，如果这时没有新的任务提交，核心线程外的线程不会立即销毁，而是会等待，直到时间超过```keepAliveTime```才会被回收销毁；
1. ```unit```：keepAliveTime的时间单位。
1. ```threadFactory```：executor创建新的线程时会用到
1. ```handler```：饱和策略。关于饱和策略下面单独介绍一下。

#### ```ThreadPoolExecutor```饱和策略

**```ThreadPoolExecutor```饱和策略的定义：**

如果当时同时运行的线程数量达到最大线程数并且队列也被放满时，```ThreadPoolExecutor```定义了一些策略：

- **```ThreadPoolExecutor.AbortPolicy```**:抛出```RejectedExecutionException```来拒绝新任务的处理
- **```ThreadPoolExecutor.CallerRunsPolicy```**:调用执行自己的线程运行任务，也就是直接在调用```executor```方法的线程中运行（```run```）被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这个策略会降低对于新任务的提交速度，影响程序的整体性能。如果您的应用程序可以承受此延迟并且你要要求任何一个任务请求都要被执行的话，你可以选择这个策略。
- **```ThreadPoolExecutor.DiscardPolicy```**：不处理新任务，直接丢弃。
- **```ThreadPoolExecutor.DiscardOldestPolicy```**：此策略将丢弃最早未处理的任务请求。

### 一个简单的线程池demo

首先创建一个```Runnable```接口的实现类（当然也可以是```Callable```接口，上面我们也说了两者的区别）

```MyRunnable.java```

```java
package com.example.main;

import java.util.Date;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-12-01 9:21
 **/
public class MyRunnable implements Runnable {

    private String command;

    public MyRunnable(String s) {
        this.command = s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End. Time = " + new Date());
    }

    private void processCommand() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MyRunnable{" +
                "command='" + command + '\'' +
                '}';
    }
}
```

编写测试程序，我们这里以阿里巴巴推荐的```ThreadPoolExecutor```构造自定义参数的方式来创建线程池

```ThreadPoolExecutorDemo.java```

```java
package com.example.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-12-01 9:26
 **/
public class ThreadPoolExecutorDemo {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;

    public static void main(String[] args) {
        // 使用阿里巴巴推荐的创建线程池的方式
        // 通过ThreadPoolExecutor构造函数自定义参数创建
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 10; i++) {
            // 创建WorkerThread对象（WorkerThread类实现了Runnable接口）
            Runnable worker = new MyRunnable("" + i);
            // 执行
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()){ }
        System.out.println("Finished all threads");
    }

}
```

可以看到我们上面指定了：

1. ```corePoolSize```:核心线程数为5
1. ```maxmumPoolSize```：最大线程数10
1. ```keepAliveTime```:等待时间为1s
1. ```unit```：等待时间单位为```TimeUnit.SECONDS```
1. ```workQueue```：任务队列为```ArrayBlockingQueue```，并且容量为100
1. ```handler```：饱和策略为```ThreadPoolExecutor.CallerRunsPolicy```

output:

```text
pool-1-thread-1 Start. Time = Tue Dec 01 09:38:54 CST 2020
pool-1-thread-2 Start. Time = Tue Dec 01 09:38:54 CST 2020
pool-1-thread-3 Start. Time = Tue Dec 01 09:38:54 CST 2020
pool-1-thread-4 Start. Time = Tue Dec 01 09:38:54 CST 2020
pool-1-thread-5 Start. Time = Tue Dec 01 09:38:54 CST 2020
pool-1-thread-5 End. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-3 End. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-4 End. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-1 End. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-2 End. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-1 Start. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-4 Start. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-5 Start. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-3 Start. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-2 Start. Time = Tue Dec 01 09:38:59 CST 2020
pool-1-thread-3 End. Time = Tue Dec 01 09:39:04 CST 2020
pool-1-thread-1 End. Time = Tue Dec 01 09:39:04 CST 2020
pool-1-thread-4 End. Time = Tue Dec 01 09:39:04 CST 2020
pool-1-thread-2 End. Time = Tue Dec 01 09:39:04 CST 2020
pool-1-thread-5 End. Time = Tue Dec 01 09:39:04 CST 2020
Finished all threads
```

### 线程池原理分析

通过上面代码我们可以看出：**线程池每次会同时执行5个任务，这5个任务执行完之后，剩余的5个任务才会被执行**。

为了搞懂线程池的原理，我们需要首先分析一下```execute```方法

```java
// 存放线程池的运行状态（runState）和线程池内有效线程的数量（workerCount）
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

private static int workerCountOf(int c) {
    return c & CAPACITY;
}

private final BlockingQueue<Runnable> workQueue;

public void execute(Runnable command) {
    // 如果任务为null，则抛出异常
    if(command == null) {
        throw new NullPointerException();
    }
    // ctl 中保存的线程池当前的一些状态信息
    int c = ctl.get();

    // 下面会涉及到3步操作
    // 1. 首先判断当前线程池中执行的任务是否小于corePoolSize
    // 如果小于的话，通过addWorker(command, true)新建一个线程，并且将任务(command)添加到该线程中；然后，启动该线程从而执行任务
    if(workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true)) {
            return;
        }
        c = ctl.get();
    }
    // 2. 如果当前执行的任务数量大于等于corePoolSize的时候就会走到这里
    // 通过当前isRunning方法判断线程池状态，线程池处于RUNNING状态才会认为可以加入队列，该任务才会被加入进去
    if(isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        // 再次获取线程池状态，如果线程池状态不是RUNNING状态就需要从任务队列中移除任务，并尝试判断线程是否全部执行完。同时执行拒绝策略
        if(!isRunning(recheck) && remove(command)) {
            reject(command);
        }else if(workerCountOf(recheck) == 0) {
            // 如果当前线程空闲的话就创建一个线程并执行
            addWorker(null, falase);
        }
    }else if(!addWorker(command, false)) {
        // 3. 通过addWorker(command, false) 新建一个线程，并将任务(command) 添加到该线程中；然后，启动该线程从而执行任务
        // 如果addWorker(command, false)执行失败，则通过reject()执行相应的拒绝策略的内容。
        reject(command);
    }
}
```

通过下图可以更好的对上面这3步做一个展示

![执行流程](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-7/%E5%9B%BE%E8%A7%A3%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86.png)

> 我们在代码中模拟了10个任务，我们配置的核心线程数为5，等待队列容量为100，所以只可能存在5个任务同时执行，剩下的5个任务会被放到等待队列中去。当前的5个任务执行完成之后才会执行剩下的5个任务

## Atomic原子类

### 介绍一下Atomic原子类

Atomic翻译成中文是原子的意思。在化学上，我们知道原子是构成物质的最小单位，在化学反应中也是不可分割的。在我们这里Atomic是指一个操作是不可中断的。即使是在多个线程一起执行的时候，一个操作一旦开始，就不会被其他线程所干扰。

所以，所谓原子类说简单点就是具有原子/原子操作特征的类；

并发包```java.util.concurrent```的原子类都存放在```java.util.concurrent.atomic```下，如下图所示：

![juc](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/JUC%E5%8E%9F%E5%AD%90%E7%B1%BB%E6%A6%82%E8%A7%88.png)

### JUC包中的原子类是哪4类

**基本类型**

使用原子的方式更新基本类型

- ```AtomicInteger```：整形原子类
- ```AtomicLong```：长整型原子类
- ```AtomicBoolean```：布尔类型原子类

**数组类型**

- ```AtomicIntegerArray```：整形数组原子类
- ```AtomicLongArray```：长整形数组原子类
- ```AtomicReferenceArray```：引用类型数组原子类

**引用类型**

- ```AtomicReference```：引用类型原子类
- ```AtomicStampedReference```：原子更新引用类型里的字段原子类
- ```AtomicMarkableReference```：原子带有标记位的引用类型

**对象的属性修改类型**

- ```AtomicIntegerFieldUpdater```：原子更新整形字段的更新器
- ```AtomicLongFieldUpdater```：原子更新长整形字段的更新器
- ```AtomicStampedReference```：原子更新带有版本号引用类型。该类将整形与引用关联起来，可用于解决原子的更新数据和数据的版本号，可以解决使用CAS进行的原子更新的ABA问题

### 讲讲AtomicInteger的使用

**AtomicInteger类常用方法：**

```java
public final int get() // 获取当前的值
public final int getAndSet(int newValue)  // 获取当前的值，并设置新的值
public final int getAndIncrement() // 获取当前的值，并自增
public final int getAndDecrement() // 获取当前的值，并自减
public final int getAndAdd(int delta) // 获取当前的值，并且加上预期的值
boolean compareAndSet(int expect, int update) // 如果输入的数值等于预期值，则以原子的方式将改值设置为输入值（update）
public final void lazySet(int newValue) // 最终设置为newValue，使用lazySet设置之后可能导致其他线程在之后的一个小时内还可以读到旧的值
```

**AtomicInteger类的使用示例：**

使用AtomicInteger之后，不用对increment()方法加锁也可以保证线程安全

```java
class AtomicIntegerTest {
    private AtomicInteger count = new AtomicInteger();
    // 使用AtomicInteger之后，不需要对该方法加锁，也可以实现线程安全
    public void increment() {
        count.incrementAndGet();
    }
    public int getCount() {
        return count.get();
    }
}
```

### 能不能介绍一下AtomicInteger类的原理

```AtomicInteger```线程安全原理简单分析
s
```AtomicInteger```类的部分源码：

```java
// setup to use Unsafe.compareAndSwapInt for updates （更新操作时提供“比较并替换”的作用）
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long valueOffset;
static {
    try{
        valueOffset = unsafe.objectFiledOffset
            (AtomicInteger.class.getDeclaredField("value"));
    } catch(Exception ex) { throw new Error(ex); }
}
private volatile int value;
```

```AtomicInteger```类主要利用**CAS(compare and swap) + volatile 和 native方法**来保证操作的原子性，从而避免synchronized的高开销，执行效率大大提升

CAS的原理是拿期望的值和原本的的一个值比较，如果相同则更新成新的值。Unsafe类的objectFieldOffset()方法是一个本地方法，这个方法是用来拿到原来的值内存地址，返回值是valueOffset。另外value是一个volatile变量，在内存中可见，因此JVM可以保证任何时刻线程总能拿到该变量的最新值。

## AQS

### AQS介绍

AQS的全称为（AbstractQueuedSynchronizer），这个类在java.util.concurrent.locks包下面。

![AQS](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/AQS%E7%B1%BB.png)

AQS是一个用来构建锁和同步器的框架，使用AQS能简单且高效的构建出应用广泛的大量的同步器，比如我们提到的ReentrantLock，Semaphore，其他诸如ReentrantReadWriteLock，SynchronousQueue，FutureTask等等皆是基于AQS的。当然，我们自己也能利用AQS非常轻松的构建出符号我们自己需求的同步器

### AQS原理分析

AQS原理这部分参考了部分博客

> 在买面试中被问到并发知识 的时候，大多都会被问到“请你说一下自己对AQS的理解”。下面给大家一个示例供大家参考，面试不是背题，大家一定要加入自己的思想，即使加入不了自己的思想也要保证自己能够通俗的讲出来而不是背出来

下面大部分内容其实在AQS类注释上已经给出了，不过是英语的看着有点吃力一点，感兴趣的可以看源码

#### AQS原理概览

**AQS核心思想是，如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。如果被请求的资源被占用，那么就需要一套线程阻塞等待以及唤醒时锁分配的机制，这个机制AQS是用CLH队列实现的，即将暂时获取不到锁的线程加入到队列中去。**

> CLH(Craig,Landin,and Hagersten)队列是一个虚拟的双向队列（虚拟的双向队列即不存在队列实例，仅存在节点之前的关联关系）。AQS是将每条请求共享资源的线程封装成一个CLH锁队列的一个节点（Node）来实现锁的分配

看看AQS（AbstractQueueSynchronizer）原理图

![原理图](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/AQS%E5%8E%9F%E7%90%86%E5%9B%BE.png)

AQS使用一个int成员变量来表示同步状态，通过内置的FIFO队列来完成获取资源队列的排队工作。AQS使用CAS对该同步状态进行原子操作对其值的修改。

```java
private volatile int state; // 共享变量， 使用volatile修饰保证线程可见性
```

状态信息通过protected类型的getState,setState,compareAndSetState进行操作

```java
// 获取同步状态的当前值
protected final int getState() {
    return state;
}

// 设置同步状态的值
protected final void setState(int newState) {
    state = newState;
}

// 原子地（CAS操作）将同步状态值设置为给定值update如果当前同步状态的值等于expect（期望值）
protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

#### AQS 对资源的共享方式

**AQS定义两种资源共享方式。**

- **Exclusive**(独占)：只有一个线程能够执行，如```ReentrantLock```。又可分为公平锁和非公平锁：
  - 公平锁：按照线程在队列中的排队顺序，先拿到者先拿到锁
  - 非公平锁：当线程需要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的
- **Share**(共享)：多个线程可同时执行，如```Samaphore/CountDownLatch、CyclicBarriter、ReadWriterLock```我们都会在后面讲到

```ReentrantReadWirteLock```可以看成是组合式，因为```ReentrantReadWirteLock```也就是读写锁允许多个线程同时对某一资源进行读。

不同的自定义同步器争用共享资源的方式也不同，自定义同步器在实现时只需要实现对共享资源state的获取以及释放而已，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队列等），AQS已经在顶层实现好了
