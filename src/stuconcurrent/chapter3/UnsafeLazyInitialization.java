package stuconcurrent.chapter3;

/**
 * 不安全的单例懒加载
 */
public class UnsafeLazyInitialization {
    private static Object instance;
    public static Object getInstance() {
        if (instance == null) {
            instance = new Object();
        }
        return instance;
    }
}
