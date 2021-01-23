package stuconcurrent.chapter4.test.proxy;

public class SuperMan implements Hero {
    @Override
    public void fly() {
        System.out.println("超人一飞冲天");
    }

    @Override
    public int attack() {
        System.out.println("超人光线");
        return 100;
    }
}
