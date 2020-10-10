# Java

---

## 代理模式

代理模式就是**我们使用代理对象代替真实对象（real object）的访问，这样就在不修改原目标对象的前提下，提供额外的功能操作，扩展目标对象的功能，比如说在目标对象的某个方法执行前后可以增加一些自定义操作**</br></br>
代理模式分为静态代理和动态代理两种实现方式

## 静态代理

**静态代理中，我们对目标对象的每个方法的增强都是手动完成的（*非常不灵活，比如接口一旦新增方法，目标对象和代理对象都需要进行修改）并且麻烦（需要对每个目标类都单独写个代理类）*）**。实际应用非常少，日常开发几乎看不到静态代理的场景</br></br>
上面我们是从实现角度和应用角度来说的静态代理，从JVM层面来说，**静态代理在编译时就将接口、实现类、代理类这些都变成了一个个实际的class文件。**</br></br>
静态代理的实现步骤：

1. 定义一个接口以及实现类
1. 创建一个代理类同样实现这个接口
1. 将目标对象注入进代理类，然后在代理类的对应方法中调用目标类的对应方法。这样的话，我们就可以通过代理类屏蔽对目标对象的访问，并且可以在目标方法执行前后做一些自己想做的事情

代码展示：

1.**定义短信发送接口**

```java
public interface SmsService {
    String send(String message);
}
```

2.**实现发送短信接口**

```java
public class SmsServiceImpl implements SmsService {
    @Override
    public String send(String message) {
        System.out.print("send messasge:" + message);
        return message;
    }
}
```

3.**创建代理类并同样实现发送短信的接口**

```java
public class SmsProxy implements SmsService {
    private final SmsService smsService;

    public SmsProxy(SmsService smsService){
        this.smsService = smsService;
    }

    @Override
    public String send(String message) {
        // 调用方法之前，我们可以添加自己的操作
        System.out.print("before method send()");
        // 调用方法
        smsService.send(message);
        // 调用方法之后，我们可以添加自己的操作
        System.out.print("after method send()");
        return null;
    }
}
```

4.**实际使用**

```java
public class TestMain {
    public static void main(String[] args) {
        SmsService smsService = new SmsServiceImpl();
        SmsProxy smsProxy = new SmsProxy(smsService);
        smsProxy.send("java");
    }
}
```

运行代码，控制台打印出：

```java
before method send()
send message java
after method send()
```

从输出结果看出，我们已经增加了```SmsServiceImpl#send()```方法。

## 动态代理

相当于静态代理来说，动态代理更为灵活。因为我们不需要针对每个目标类都单独创建一个代理类，并且也不需要我们必须实现接口，我们可以直接代理实现类（*CGLIB动态代理*）。</br>

**从JVM角度来说，动态代理是在运行是动态生成类字节码，并加载到JVM中的**</br>

说到动态代理，Spring AOP、RPC框架应该是两个不得不提的，他们的实现几乎都依赖了动态代理。</br></br>
**动态代理在我们实际开发中使用相对较小，但是对于框架几乎是必备的**

就Java而言，动态代理的实现方式有很多种，比如**JDK的动态代理、CGLIB动态代理**等等

### JDK动态代理机制

#### 介绍

**在Java动态代理机制中```InvocationHandler```接口和```Proxy```类是核心**。</br>
```Proxy```类使用频率最高的方法是```newProxyInstance()```，这个方法主要来生成一个代理对象。

```java
public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
        throws IllegalArgumentException
    {
        .......
    }
```

这个方法一共三个参数

1. **loader:** 类加载器
1. **interfaces:** 被代理类实现的一些接口
1. **h:** 实现```InvocationHandler```接口的对象

要实现动态代理的话，还必须实现```InvocationHandler```来处理自定义逻辑。当我们的动态代理对象调用一个方法的时候，这个方法的调用就会转发到实现```InvocationHandler#invoke()```方法来调用

```java
public interface InvocationHandler {
    /**
    * 当你使用代理对象调用方法的时候实际上会调用这个方法
    */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
```

```#invoke()```有下面三个参数：

1. **proxy:** 动态生成的代理类
1. **method:** 与代理类对象调用的方法对应
1. **args:** 当前method方法的参数

也就是说：**你通过```Proxy```类的```newProxyInstance()```方法创建的代理对象在调用方法的时候，实际上会调用到```InvocationHandler#invoke()```**，你可以在```invoke()```方法中处理自定义逻辑，比如在方法执行前后做什么事情。

#### JDK动态代理使用步骤

1. 自定义一个接口以及实现类
1. 自定义实现```InvocationHandler```，并重写```invoke()```方法，在```invoke```方法中我们会调用原生方法（被代理类的方法）并自定义一些处理逻辑；
1. 通过```Proxy.newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)```方法创建对象；

#### 代码示例

1.**自定义发送短信接口**

```java
public interface SmsService {
    String send(String message);
}
```

2.**实现发送短信接口**

```java
public class SmsServiceImpl implements SmsService {
    @Override
    public String send(String message) {
        System.out.print("send messasge:" + message);
        return message;
    }
}
```

3.**定义一个JDK动态代理类**

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author YanCh
 * @version v1.0
 * Create by 2020-10-09 15:33
 **/
public class SmsServiceInvocationHandler implements InvocationHandler {

    /**
     * 代理类中真实的对象
     */
    private final Object target;

    public SmsServiceInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 调用方法之前可以添加自己的操作
        System.out.println("before method" + method.getName());
        Object result = method.invoke(target, args);
        // 调用方法之后可以进行自己的操作
        System.out.println("after method" + method.getName());
        return result;
    }
}
```

```invoke()```方法：当我们动态代理对象调用原生方法的时候，最终实际上是调用到的是```invoke()```方法，然后由```invoke()```方法代替我们去调用了被代理对象的原生方法。

4.**获取代理对象的工厂类**

```java
public class JdkProxyFactory {
    public static Object getProxy(Object target) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(), // 目标类的类加载
                target.getClass().getInterfaces(), // 代理类需要实现的接口
                new SmsServiceInvocationHandler(target)
        );
    }
}
```

```getProxy()```：主要通过```Proxy.newProxyInstance()```方法获取某个对象

5.**实际使用**

```java
public static void main(String[] args) {
    SmsService smsService = (SmsService) JdkProxyFactory.getProxy(new SmsServiceImpl());
    smsService.send("java");
}
```

运行代码后输出：

```java
before methodsend
send message:java
after methodsend
```

### CGLIB动态代理

#### CGLIB介绍

**JDK动态代理有一个致命的问题是其只能代理实现接口的类。**

**为了解决这个问题，我们可以用CGLIB动态代理机制来避免。**

CGLIB(*Code Generation Library*)是一个基于ASM的字节码生成库，它允许我们在运行时对字节码进行修改和动态生成。CGLIB通过继承方式实现代理。很多知名的开源框架都是使用CGLIB，例如Spring中的AOP模块中；如果目标对象实现了接口，则默认采用JDK动态代理，否则使用CGLIB动态代理

**在CGLIB动态代理机制中```MethodInterceptor```接口和```Enhancer```类是核心。**

你需要自定义```MethodInterceptor```并重写```intercept```方法，```intercept```用于拦截增强被代理类的方法

```java
public interface MethodInterceptor
extends Callback
{
    // 拦截被代理类的方法
    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                               MethodProxy proxy) throws Throwable;
}
```

1. **obj**:被代理的对象
1. **method**：被拦截的方法
1. **args**：方法入参
1. **methodProxy**：用于调用原始方法

你可以通过```Enhancer```类来动态获取被代理类，当代理类调用方法的时候，实际上调用的是```MethodInterceptor```中的intercept方法。

#### CGLIB动态代理类使用过步骤

1. 定义一个类
1. 自定义```MethodInterceptor```并重写```intercept```方法，```intercept```用于拦截被代理类的方法，和JDK的```invoke```方法类似；
1. 通过```Enhancer```类的```create()```创建代理类

#### CGLIB代码示例

不同于JDK动态代理不需要依赖额外包，CGLIB(*Code Generation Library*)实际上是属于一个开源项目，如果你要使用它的话，需要手动添加相关依赖。

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.2.9</version>
</dependency>
```

1.**实现一个使用阿里云发送短信的类**

```java
public class AliSmsService {
    public String send(String message) {
        System.out.println("send message：" + message);
        return message;
    }
}
```

2.**自定义```MethodInterceptr```（方法拦截器）**

```java
public class AliServiceMethodInterceptor implements MethodInterceptor {
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // 调用方法之前，我们可以添加自己的操作
        System.out.println("before method : " + method.getName());
        // 执行方法
        Object o = proxy.invokeSuper(obj, args);
        // 调用方法之后，我们同样可以添加自己的操作
        System.out.println("after method : " + method.getName());
        return o;
    }
}
```

3.**获取代理类**

```java
public class CglibProxyFactory {
    public static Object getProxy(Class<?> clazz) {
        // 创建动态代理增强类
        Enhancer enhancer = new Enhancer();
        // 设置类加载器
        enhancer.setClassLoader(clazz.getClassLoader());
        // 设置被代理类
        enhancer.setSuperclass(clazz);
        // 设置方法拦截器
        enhancer.setCallback(new AliServiceMethodInterceptor());
        // 创建代理类
        return enhancer.create();
    }
}
```

4.**使用**

```java
AliSmsService aliSmsService = (AliSmsService) CglibProxyFactory.getProxy(AliSmsService.class);
aliSmsService.send("java");
```

运行代码之后输出：

```java
before method : send
send message：java
after method : send
```

### JDK动态代理和CGLIB动态代理对比

1. **JDK动态代理只能代理实现了接口的类，而CGLIB可以代理未实现任何接口的类。** 另外，CGLIB动态代理是通过生成一个被代理类的子类来拦截代理类的方法调用，因此不能代理声明未final类型的类和方法。
1. 就二者效率来说，大部分情况下都是JDK动态代理更优秀，随着JDK版本的升级，这个优势更加明显

## 静态代理和动态代理的对比

1. **灵活性**：动态代理更加灵活，不需要必须实现接口，可以直接代理实现类，并且针对每个目标类都创建一个代理类。另外，静态代理中，接口一旦新增方法，目标对象和代理对象都要进行修改，这是非常麻烦的
1. **JVM层面**：静态代理在编译时就将接口、实现类、代理类这些都变成了一个个实际的class文件。而动态代理是在运行时生成类字节码，并加载到JVM中的。
