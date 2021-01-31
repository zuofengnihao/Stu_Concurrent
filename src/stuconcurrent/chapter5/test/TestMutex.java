package stuconcurrent.chapter5.test;

import stuconcurrent.chapter4.SleepUtils;
import stuconcurrent.chapter5.Mutex;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TestMutex {

    public static void main(String[] args) {
        Mutex mutex = new Mutex();
        new Thread(() -> {
            mutex.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " : 任务开始！");
                SleepUtils.second(1);
            } finally {
                mutex.unlock();
            }
        }, "HAS-thread-0").start();
        SleepUtils.second(1);
        new Thread(() -> {
            mutex.lock();
            try {
                SleepUtils.second(1);
            } finally {
                mutex.unlock();
            }
        }, "NOT-thread-1").start();
        SleepUtils.second(1);
        new Thread(() -> {
            mutex.lock();
            try {
                SleepUtils.second(1);
            } finally {
                mutex.unlock();
            }
        }, "NOT-thread-2").start();
    }
}
