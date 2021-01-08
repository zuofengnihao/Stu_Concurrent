package stuconcurrent.chapter4;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestSynFair {

    public static void main(String[] args) throws InterruptedException {
        MyList list = new MyList();
        for (int i = 0; i < 20; i++) {
            final int value = i + 1;
            Thread t = new Thread(() -> {
                try {
                    list.addItem(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "thread-" + value);
            t.start();
            Thread.sleep(1);
        }
        System.out.println(new Date().getTime() + " main方法结束");
    }

    private static class MyList {

        private int index = 0;

        private int[] list = new int[10];

        public void addItem(int value) throws InterruptedException {
            String name = Thread.currentThread().getName();
            System.out.println(new Date().getTime() + " " + name + " 进入addItem()方法...");
            synchronized (this) {
                System.out.println(new Date().getTime() + " " + name + " 进入同步代码块...");
                if (index >= list.length) index = 0;
                list[index++] = value;
                if (value == 1) {
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("=============================================");
                }
            }
            System.out.println(new Date().getTime() + " " + name + " 添加完成");
            System.out.println(Arrays.toString(list));
        }

    }
}
