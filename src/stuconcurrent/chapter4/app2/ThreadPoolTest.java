package stuconcurrent.chapter4.app2;

public class ThreadPoolTest {

    public static void main(String[] args) throws InterruptedException {
        DefaultThreadPool<Job> threadPool = new DefaultThreadPool<>();
        for (int i = 0; i < 10000; i++) {
            Job job = new Job();
            threadPool.execute(job);
        }
    }

    static class Job implements Runnable {

        @Override
        public void run() {
            System.out.println();
            System.out.print(Thread.currentThread().getName() + ": ");
        }
    }
}
