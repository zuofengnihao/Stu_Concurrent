package stuconcurrent.chapter5.test;

import stuconcurrent.chapter5.TwinsLock;

public class TestTwinsLock {

    public static void main(String[] args) {
        TwinsLock lock = new TwinsLock();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + " begin");
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }, "thread-" + (i + 1));
            thread.start();
        }
    }
}
