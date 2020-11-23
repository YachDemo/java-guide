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

```synchronized```是依赖于JVM实现的，前面我们也讲到了虚拟机团队在JDK1.6为```synchronized```关键字做了很多优化，但是这些优化都是在虚拟机层面实现的，并没有直接暴露给我们。ReentrantLock
