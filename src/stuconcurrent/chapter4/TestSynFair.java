package stuconcurrent.chapter4;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 线程状态及状态转换
 *
 *  当多个线程同时请求某个对象监视器时，对象监视器会设置几种状态用来区分请求的线程：
 *  ◆ Contention List：所有请求锁的线程将被首先放置到该竞争队列。
 *  ◆ Entry List：Contention List中那些有资格成为候选人的线程被移到Entry List。
 *  ◆ Wait Set：那些调用wait方法被阻塞的线程被放置到Wait Set。
 *  ◆ OnDeck：任何时刻最多只能有一个线程正在竞争锁，该线程称为OnDeck。
 *  ◆ Owner：获得锁的线程称为Owner。
 *  ◆ !Owner：释放锁的线程。
 *
 *  新请求锁的线程将首先被加入到ConetentionList中，当某个拥有锁的线程（Owner状态）调用unlock之后，如果发现EntryList为空则从ContentionList中移动线程到EntryList，下面说明下ContentionList和EntryList的实现方式。
 *
 */
public class TestSynFair {

    public static void main(String[] args) throws InterruptedException {
        Runner runner = new Runner();
        Thread t1 = new Thread(runner, "Thread-1");
        Thread t2 = new Thread(runner, "Thread-2");
        Thread t3 = new Thread(runner, "Thread-3");
        Thread t4 = new Thread(runner, "Thread-4");
        Thread t5 = new Thread(runner, "Thread-5");
        Thread t6 = new Thread(runner, "Thread-6");

        t1.start();
        Thread.sleep(1);
        t2.start();
        Thread.sleep(1);
        t3.start();
        Thread.sleep(1);
        t4.start();
        Thread.sleep(1);
        t5.start();
        Thread.sleep(1);
        t6.start();
    }

    private static class Runner implements Runnable {

        @Override
        public void run() {
            System.out.println(new Date().getTime() + " " + Thread.currentThread().getName() + " 进入方法");
            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " 进入同步代码，没进入同步代码的进入同步队列");
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println(Thread.currentThread().getName() + " 退出同步代码并结束线程");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
