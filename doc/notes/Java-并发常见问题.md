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

下面我以一个常见的面试题为例讲解一下```synchronized```关键字的具体使用
