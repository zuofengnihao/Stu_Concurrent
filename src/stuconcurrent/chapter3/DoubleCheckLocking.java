package stuconcurrent.chapter3;

/**
 * 双重检查锁的单例懒加载模式
 * 在第一次判断null时不会加锁，可以提高效率
 * 但是在instance = new Object();会有问题
 *
 * 这是一个错误的优化！！！
 * 在第一次不为null时，instance可能并没有初始化完成！！！
 *
 * 分解 instance = new Object()
 *
 * 1. memory = allocate(); //分配对象的内存空间。
 * 2. ctorInstance(memory); //初始化对象
 * 3. instance = memory; //设置instance指向刚分配的内存地址
 * 上面3行伪代码中，2和3可能会被重排序变成下面的形式
 * 1. memory = allocate(); //分配对象的内存空间。
 * 3. instance = memory; //设置instance指向刚分配的内存地址，此时对象并没有被初始化！！！
 * 2. ctorInstance(memory); //初始化对象
 *
 * 上的重排序是有可能发生的，因为并没有影响程序执行结果！！！
 *
 * 解决方案：
 * 1. 不允许2和3重排序。volatile关键字可以不允许 写/读重排序
 * 2. 允许重排序，但对其他线程不可见。
 */
public class DoubleCheckLocking {
    private static Object instance;
    public static Object getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckLocking.class) {
                if (instance == null) instance = new Object();
            }
        }
        return instance;
    }
}

class SafeDoubleCheckLocking {
    private volatile static Object instance;
    public static Object getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckLocking.class) {
                if (instance == null) instance = new Object();
            }
        }
        return instance;
    }
}

/**
 * JVM在类的初始化阶段，会执行类的初始化。在执行类的初始化期间，JVM会去获取一个锁，这个锁可以同步多个线程对同一个类的初始化。
 * 基于这个特性，可以实现另一种线程安全的延迟初始化方案。
 */
class InstanceFactory {
    private static class InstanceHolder {
        public static Object instance = new Object();
    }
    public static Object getInstance() {
        return  InstanceHolder.instance; // 这里将导致InstanceHolder类被初始化。
    }
}