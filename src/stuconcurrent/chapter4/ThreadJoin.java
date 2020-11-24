package stuconcurrent.chapter4;

import java.util.concurrent.TimeUnit;

public class ThreadJoin {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = Thread.currentThread();
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Runner(thread), i + "");
            t.start();
            thread = t;
        }
        TimeUnit.SECONDS.sleep(5);
        System.out.println(Thread.currentThread().getName() + " terminate");
    }

    private static class Runner implements Runnable {

        private Thread thread;

        public Runner(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " terminate");
        }
    }
}
