# Stu_Concurrent

## 第1章 并发编程的挑战

### 1.1 上下文切换
当前线程要切换到另外一个线程的时候，需要保存当先线程的状态（如：寄存器里面已经计算好了的值，当前运行到哪段代码了等信息），然后读取或叫加载另外那个线程的状态，这就是上下文的切换。

#### 1.1.1 多线程一定快吗
不一定，因为线程有创建和上下文切换的开销。

#### 1.1.2 测试上下文切换次数和时长
* 使用Lmbench可测量上下文切换带来的消耗
* 使用vmstat可测量上下文切换的次数

#### 1.1.3 如何减少上下文的切换
1. 使用无锁并发编程：多线程竞争锁，会引起上下文的切换。可以使用一些办法避免使用锁。如：将数据ID按照Hash算法取模来分段，不同线程处理不同段的数据。
2. CAS算法：Atomic包使用CAS算法，不需要加锁。
3. 使用最少的线程：避免创建不需要的线程。
4. 协程：我理解的协程就是在用户态使用代码实现的多线程，和内核态的线程是一对多的关系。协程不需要切换到内核态来完成任务的调度与切换。当然协程也是需要记录和切换上下文的，只是在用户态中，程序员是自己可以用代码来压榨切换带来的最小开销极限。

#### 1.1.4 减少上下文切换实战
* 第一步：用jstack命令dump线程信息。
* 第二步：统计线程状态。
* 第三步：打开dump文件查看处于WAITING的线程在做什么。
* 第四步：减少无效的WAITING状态的线程数量。

### 1.2 死锁
避免死锁的常见方法：
1. 避免一个线程同时获取多个锁。
2. 避免一个线程在锁内同时占用多个资源，尽量保证每个锁只占用一个资源。
3. 尝试使用定时锁，lock.tryLock(timeout)。
4. 对于数据库锁，加锁和解锁必须在一个数据库链接里。

### 1.3 资源限制的挑战

1. 什么是资源限制  
2. 资源限制引发的问题
3. 如何解决资源限制
4. 在资源限制情况下进行并发

### 1.4 本章小结

## 第2章 Java并发机制的底层实现原理
Java中所使用的并发机制依赖于JVM的实现和CPU的指令。

### 2.1 Volatile的应用
在多处理器开发中保证了共享变量的“可见性”。

#### 2.1.1 Volatile的定义与实现原理
定义：Java编程语言允许线程访问共享变量，为了确保共享变量能被准确和一直的更新，线程应该确保通过排它锁单独获得这个变量。  

CPU的相关术语：
* 内存屏障（Memory barriers）：是一组处理器指令，用于实现对内存操作的顺序限制。
* 缓冲行（cache line）：缓存中可分配的最小存储单位。处理器填写缓存线时会加载整个缓存线，需要使用多个主内存读周期。
* 原子操作（atomic operations）：不可中断的一个或一系列操作。
* 缓存行填充（cache line fill）：当处理器识别到从内存中读取操作数是可缓存的，处理器读取整个缓存行到适当的缓存（L1/L2/L3）。
* 缓存命中（cache hit）：如果进行高速缓存行填充操作的内存位置仍然是下次处理器访问的地址时，处理器从缓存中读取操作数，而不是从内存中。
* 写命中（write hit）：当处理器将操作数写回到一个内存缓存的区域时，它会首先检查这个缓存的内存地址是否在缓存行中，如果存在一个有效的缓存行，则处理器将这个操作数写回到缓存，而不是写回到内存，这个操作被称为写命中。
* 写缺失（write misses the cache）：一个有效的缓存行被写入到不存在的内存区域。  

原理：如果对声明了volatile的变量进行写操作，JVM就会向处理器发送一条Lock前缀的指令，将这个变量所在缓存行的数据写回到系统内存。通过实现缓存一致性协议（MESI）每个处理器通过嗅探在总线上传播的数据来检查自己缓存的值是不是过期了，当处理器发现自己缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置成无效状态，当处理器对这个数据进行修改操作的时候，会重新从系统内存中把数据读到处理器缓存里。

#### 2.1.2 Volatile的使用优化

避免伪共享（false sharing）：Doug Lea 使用追加字节的方式来优化。

### 2.2 Synchronized的实现原理与应用

Synchronized同步代码块是使用monitorenter和monitorexit指令实现的。同步方法是另一种方式实现。

锁对象的三种形式：
1. 普通同步方法，锁是当前的实例对象。
2. 静态同步方法，锁是当前类的Class对象。
3. 同步代码块，锁是Synchronized括号中里配置的对象。

#### 2.2.1 Java对象头

* Mark Word 32/64bit 存储对象的hashCode或锁信息等  
* Class Metadata Address 32/64bit 存储到对象类型数据的指针  
* Array Length 32/32bit 数组长度（如果当前对象是数组）

    





