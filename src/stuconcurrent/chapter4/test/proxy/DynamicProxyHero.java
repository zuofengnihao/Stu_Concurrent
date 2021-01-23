package stuconcurrent.chapter4.test.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxyHero implements InvocationHandler {

    Hero hero;

    public DynamicProxyHero(Hero hero) {
        this.hero = hero;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("fly")) {
            System.out.println("动态代理人飞行");
            hero.fly();
            return null;
        } else if (method.getName().equals("attack")) {
            System.out.println("动态代理人攻击");
            return hero.attack() + 50;
        }
        return null;
    }
}
