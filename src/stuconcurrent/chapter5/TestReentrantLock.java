package stuconcurrent.chapter5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public class TestReentrantLock {

    public static void main(String[] args) {
        ReentrantLock2 fairLock = new ReentrantLock2(true);
        ReentrantLock2 unfairLock = new ReentrantLock2(false);

    }

    private static class ReentrantLock2 extends ReentrantLock {

        ReentrantLock2(boolean fair) {
            super(fair);
        }

        public Collection<Thread> getQueueThreads() {
            ArrayList<Thread> threads = new ArrayList<>(super.getQueuedThreads());
            Collections.reverse(threads);
            return threads;
        }
    }
}
