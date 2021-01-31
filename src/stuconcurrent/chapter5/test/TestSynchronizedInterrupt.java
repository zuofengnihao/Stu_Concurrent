package stuconcurrent.chapter5.test;

public class TestSynchronizedInterrupt {

    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                for(;;) {
                    System.out.println(Thread.currentThread().getName() + " 正在运行");
                }
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + " 获取到锁了");
            }
        });
        t1.start();
        Thread.sleep(300);
        t2.start();
        Thread.sleep(300);
        t2.interrupt();
    }
}
