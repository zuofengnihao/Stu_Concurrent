package stuconcurrent.chapter4;

import java.util.concurrent.TimeUnit;

/*
自己线程join自己线程 死锁 不会抛出异常 程序中应该判断是否为当前线程
 */
public class TestJoinSelf {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " start");
        Thread thread = Thread.currentThread();
        if (thread != Thread.currentThread()) {
            thread.join();
        }
        TimeUnit.SECONDS.sleep(3);
        System.out.println(Thread.currentThread().getName() + " terminate");
    }
}
