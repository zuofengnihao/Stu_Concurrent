package stuconcurrent.chapter3;

/**
 * 测试 join() happens before
 */
public class TestJoin {

    public static int i = 1;

    public static void main(    String args[]) {
        Thread thread = new Thread(
                () -> {
                    i = 10;
                }
        );

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(i);
    }
}
