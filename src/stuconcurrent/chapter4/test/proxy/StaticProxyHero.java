package stuconcurrent.chapter4.test.proxy;

public class StaticProxyHero implements Hero {

    Hero hero;

    public StaticProxyHero(Hero hero) {
        this.hero = hero;
    }

    @Override
    public void fly() {
        System.out.println("静态代理人：飞不起来啊");
        hero.fly();
    }

    @Override
    public int attack() {
        System.out.println("静态代理人攻击");
        int attack = hero.attack();
        return attack + 1;
    }
}
