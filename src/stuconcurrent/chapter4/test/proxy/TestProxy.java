package stuconcurrent.chapter4.test.proxy;

import java.lang.reflect.Proxy;

public class TestProxy {

    public static void main(String[] args) {
        BatMan batMan = new BatMan();
        SuperMan superMan = new SuperMan();

        System.out.println("静态代理batman");
        StaticProxyHero staticProxyHero = new StaticProxyHero(batMan);
        staticProxyHero.fly();
        int i = staticProxyHero.attack();
        System.out.println("静态代理总伤害: " + i);

        System.out.println("动态代理superman");
        Hero dynamicProxyHero = (Hero) Proxy.newProxyInstance(Hero.class.getClassLoader(), new Class[]{Hero.class}, new DynamicProxyHero(superMan));
        dynamicProxyHero.fly();
        int j = dynamicProxyHero.attack();
        System.out.println("动态代理总伤害: " + j);
    }
}
