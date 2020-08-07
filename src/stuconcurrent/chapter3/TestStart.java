package stuconcurrent.chapter3;

public class TestStart {

    public static int i = 1;

    public static void main(String[] args) {

        i = 10;

        Thread thread = new Thread(() -> {
            System.out.println(i);
        });

        thread.start();

    }
}
