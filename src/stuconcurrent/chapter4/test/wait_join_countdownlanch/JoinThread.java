package stuconcurrent.chapter4.test.wait_join_countdownlanch;

public class JoinThread {

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "thread-1");
        thread.start();
        //thread.join();
        System.out.println("main end");
    }
}
