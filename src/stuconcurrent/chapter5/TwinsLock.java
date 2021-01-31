package stuconcurrent.chapter5;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TwinsLock implements Lock {

    private Sync sync = new Sync(2);

    @Override
    public void lock() {
        sync.tryAcquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        int i = sync.tryAcquireShared(1);
        return i >= 0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        sync.tryAcquireSharedNanos(1, unit.toMillis(time));
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

        Sync (int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("count must large than zero");
            }
            setState(count);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            for(;;) {
                int current = getState();
                int newStatus = current - arg;
                if (newStatus < 0 || compareAndSetState(current, newStatus)) {
                    return newStatus;
                }
            }
        }
        
        @Override
        protected boolean tryReleaseShared(int arg) {
            return super.tryReleaseShared(arg);
        }

        Condition newCondition() {
            return new ConditionObject();
        }
    }
}
