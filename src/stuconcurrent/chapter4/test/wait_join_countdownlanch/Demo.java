package stuconcurrent.chapter4.test.wait_join_countdownlanch;

import java.util.concurrent.CountDownLatch;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        Entity entity = new Entity(countDownLatch);
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " 线程开始！");
            for (int i = 0; i < 10; i++) {
                if ((i & 1) == 0) {
                    new Thread(() -> {
                        try {
                            entity.plusMethod();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, "Thread-" + i).start();
                } else {
                    new Thread(() -> {
                        try {
                            entity.minusMethod();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, "Thread-" + i).start();
                }
            }
            System.out.println(Thread.currentThread().getName() + " 结束开始！");
        }, "start");
        entity.setMain(thread);
        thread.start();
        countDownLatch.await();
        System.out.println(entity.result);
    }
}
