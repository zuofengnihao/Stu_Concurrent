package stuconcurrent.chapter4.test.wait_join_countdownlanch;

import java.util.concurrent.CountDownLatch;

public class Entity {

    int result = 0;

    Thread main;

    CountDownLatch countDownLatch;

    public Entity(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public void setMain(Thread main) {
        this.main = main;
    }

    public void plusMethod() throws InterruptedException {
        main.join();
        System.out.println(Thread.currentThread().getName() + " : plus run");
        synchronized (this) {
            while (result != 0) {
                this.wait();
            }
            this.result++;
            this.notifyAll();
        }
        countDownLatch.countDown();
        System.out.println(Thread.currentThread().getName() + " : plus end");
    }

    public void minusMethod() throws InterruptedException {
        main.join();
        System.out.println(Thread.currentThread().getName() + " : minus run");
        synchronized (this) {
            while (result == 0) {
                this.wait();
            }
            this.result--;
            this.notifyAll();
        }
        countDownLatch.countDown();
        System.out.println(Thread.currentThread().getName() + " : minus end");
    }

}
