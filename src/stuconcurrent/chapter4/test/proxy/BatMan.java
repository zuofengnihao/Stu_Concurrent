package stuconcurrent.chapter4.test.proxy;

public class BatMan implements Hero {
    @Override
    public void fly() {
        System.out.println("只能打飞机了");
    }

    @Override
    public int attack() {
        System.out.println("勾拳");
        return 10;
    }
}
