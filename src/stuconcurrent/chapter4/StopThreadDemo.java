package stuconcurrent.chapter4;

import java.util.concurrent.TimeUnit;

public class StopThreadDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread one = new Thread(new Runner(), "one");
        one.start();
        TimeUnit.SECONDS.sleep(1);
        one.interrupt();

        Runner runner = new Runner();
        Thread two = new Thread(runner, "two");
        two.start();
        TimeUnit.SECONDS.sleep(1);
        runner.cancel();
    }

    private static class Runner implements Runnable {

        private volatile boolean on = true;

        @Override
        public void run() {
            int i  = 0;
            while (on && !Thread.currentThread().isInterrupted()) {
                i++;
            }
            System.out.println("Count i = " + i);
        }

        public void cancel() {
            on = false;
        }
    }
}
