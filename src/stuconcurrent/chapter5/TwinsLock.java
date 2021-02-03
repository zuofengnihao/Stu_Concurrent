package stuconcurrent.chapter5;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TwinsLock implements Lock {

    private Sync sync = new Sync(2);

    @Override
    public void lock() {
        sync.acquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        int i = sync.tryAcquireShared(1);
        if (i < 0) return false;
        return true;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.releaseShared(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    static class Sync extends AbstractQueuedSynchronizer {

        Sync(int status) {
            setState(status);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            // 此处设计了10次自旋 不然无法实现tryLock()
            for (int i = 0; i < 10; i++) {
                int currentState = getState();
                int newState = currentState - arg;
                if (newState < 0 || compareAndSetState(currentState, newState)) {
                    return newState;
                }
            }
            return -1;
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            for (;;) {
                int currentState = getState();
                int newState = currentState + arg;
                if (compareAndSetState(currentState, newState)) return true;
            }
        }

        public Condition newCondition() {
            return new ConditionObject();
        }
    }
}
