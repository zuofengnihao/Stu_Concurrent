package stuconcurrent.chapter4;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestSynFair {

    public static void main(String[] args) throws InterruptedException {
        Runner runner = new Runner();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runner, "thread-" + i);
            t.start();
            Thread.sleep(10);
        }
        System.out.println(new Date().getTime() + " main方法结束");
    }

    private static class Runner implements Runnable {

        @Override
        public void run() {
            System.out.println(new Date().getTime() + " " + Thread.currentThread().getName() + " 进入方法");
            synchronized (this) {
                System.out.println(new Date().getTime() + " " + Thread.currentThread().getName() + " 进入同步代码，没进入同步代码的进入同步队列");
                try {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println(new Date().getTime() + " " + Thread.currentThread().getName() + " 退出同步代码并结束线程");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
