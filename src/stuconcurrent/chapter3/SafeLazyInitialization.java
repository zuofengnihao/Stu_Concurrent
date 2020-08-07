package stuconcurrent.chapter3;

/**
 * 安全的(加synchronized)单例懒加载，缺点是性能开销大
 */
public class SafeLazyInitialization {
    public static Object instance;
    public static synchronized Object getInstance() {
        if (instance == null) instance = new Object();
        return instance;
    }
}
