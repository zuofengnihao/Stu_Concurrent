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

64位虚拟机 Mark Word：
* 1bit 是否偏向锁 
* 2bit 锁标志位
* 4bit GC分代年龄
* 31bit hashcode    
* 25bit unused

#### 2.2.2 锁定升级与对比
1. 偏向锁：  
   进入同步代码块，检查锁对象头markword是否有锁（锁标志位或是否偏向锁）。  
   * 如果是无锁状态  
     * 检查是否开启偏向锁  
       * 开启，cas修改markword偏向当前线程
          * 修改成功，执行同步代码块
          * 修改失败，先暂停偏向的线程，检查线程是否退出同步代码块
            * 退出，撤销偏向锁，唤醒原持有锁的线程
            * 未退出，升级为轻量级锁
       * 未开启，轻量级锁流程...
   * 有锁状态，是否为偏向锁
     * 是偏向锁，检查偏向线程是否为当前线程
       * 是，执行同步代码块
       * 否，cas修改markword偏向当前线程
         * 修改成功，执行同步代码块
         * 修改失败，先暂停偏向的线程，检查线程是否退出同步代码块 
           * 退出，撤销偏向锁，唤醒原持有锁的线程
           * 未退出，升级为轻量级锁
      * 否，按照当前锁的方法来执行 

2. 轻量级锁
   进入同步代码块，检查是否为偏向锁升级
   * 是偏向是升级，为偏向线程的栈中分配锁记录
   * 不是偏向锁升级，为当前线程的栈中分配所记录  
   
   CAS操作，将对象头中的markword复制到锁记录中，displaced mark word
   * 操作成功，markword指向当前线程的锁记录中，执行同步代码块，执行完毕后cas修改markword（改回）
     * 成功，解锁完成
     * 失败，升级重量级锁
   * 操作失败，自旋等待
     * 多次自选失败，升级重量级锁
       
3. 锁的优缺点  
   
   偏向锁：适用于只有一个线程访问的场景。优点是加锁一次cas操作，缺点是如果线程参在额外竞争会带来额外的撤销锁的消耗。  
   
   轻量级锁：适用于同步块执行速度快的场景。优点是竞争的线程不会阻塞，缺点是cup自旋消耗。  
   
   重量级锁：适用于追求吞吐量，同步代码块执行长。优点不会消耗cpu，缺点就是阻塞线程，响应慢。
   
### 2.3 原子操作的实现原理
1. 处理器如何实现原子操作
   * 使用总线加锁
   * 使用缓存加锁

2. Java如何实现原子操作
   * 使用循环CAS实现（自旋锁）  
     Cas操作的三大问题
     1. ABA问题，解决：在参数中加入版本号
     2. 循环时间长开销大
     3. 只能保证一个共享变量的原子操作，解决：同步块 
   * 使用锁机制实现原子操作：偏向锁，轻量级锁，重量级锁。JVM实现锁的方式都用到了CAS操作，即获取锁和释放锁时。

### 2.4 本章小结
## 第3章 Java内存模型
### 3.1 Java内存模型的基础
#### 3.1.1 并发编程模型的两个关键
线程之间如何通信及线程间如何同步？

线程间的通讯有两种机制：
1. 共享内存：共享内存的线程间通讯是隐式的，同步则是显式的。
2. 消息传递：消息传递的线程间通讯是显示的，同步则是隐式的。  

Java的并发采用的是共享内存的方式。

#### 3.1.2 Java内存模型的抽象结构
线程之间的共享变量存储在主存中，每个线程都有一个私有的本地内存，本地内存中存储了该线程以读/写的共享变量的副本。本地内存是JMM的一个抽象概念，它涵盖了缓存、写缓冲区、寄存器以及其他的硬件和编译优化。  

如果线程A与线程B之间要通讯的话，必须要经历2个步骤：
1. 线程A把本地内存A中更新过的共享变量刷新到主存中。
2. 线程B到主存中读取线程A之前更新变量。  

从整体上看，这两个步骤实质上是线程A向线程B发送消息，而这个通信过程必须要经过主存。JMM通过控制主存与每个线程的本地内存之间的交互来为JAVA程序员提供内存可见性保证。

#### 3.1.3 从源代码到指令序列的重排序
为了提高性能，编译器和处理器常常会对指令做重排序。分为3重类型：
1. 编译器优化的重排序。
2. 指令级并行重排序。
3. 内存系统的重排序。

1属于编译器重排序，2和3属于处理器重排序。  
JMM的编译器重排序规则会禁止特定类型的编译器重排序。而对于处理器重排序，会在生成指令序列时，插入特定类型的内存屏障来禁止。

#### 3.1.4 并发编程模型的分类
现代处理器使用写缓冲区临时存放向内存写入的数据。每个写缓冲区只对该核心可见，这个特性会对内存操作顺序产生重要的影响！  
例：int x = 0 在主存中，线程A执行 x = 1，然后线程B执行 print(x),打印结果可能会为“0”，因为线程A写入数据时写入的是缓存区，而线程B读取的是主存中的数据。在我们看来，像是线程B先于线程A执行了，即写读变成了读写。因此，大部分处理器都支持写读操作的重排序。

为了保证可见性，Java编译器在生成指令序列的适当位置会插入内存屏障指令来禁止特定类型的处理器的重排序。JMM把内存屏障分为4类。
1. LoadLoad Barriers `Load1;LoadLoad;Load2`：确保Load1数据的装载先于load2及后续装在指令的装在
2. StoreStore Barriers `Store1;StoreStore;Store2`：确保Store1数据对其他处理器可见（刷新回主存）先于Store2
3. LoadStore Barriers `Load1;LoadStore;Store2`：确保Load1数据装载先于Store2
4. StoreLoad Barriers `Store1;StoreLoad;Load2`：确保Store1数据对其他线程可见，先于Load2。该屏障之前的所有内存访问指令完成后，才执行该屏障之后的内存访问指令。StoreLoad Barriers是一个全能型的屏障，它同时具有以上3个屏障的效果。执行该屏障开销会很昂贵，因为当前处理器通常要把写缓冲区中的数据全部刷新到内存中。

#### 3.1.5 happens-before简介
从JDK5开始，Java使用新的JSR-133内存模型。JSR-133使用happens-before的概念来阐述操作之间的内存可见性。如果一个操作执行的结果需要另一个操作可见，那么这两个操作之间必须存在happens-before关系。
* 程序顺序规则：一个线程中的每个操作，happens-before于该线程中的任意后续操作。
* 监视器锁规则：对一个锁的解锁，happens-before于随后对这个锁的加锁。
* volatile变量规则：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。
* 传递性：如果A happens-before B，B happens-before C，则A happens-before C。

注意：两个操作之间具有happens-before原则不代表前一个操作必须在后一个操作之前执行！happens-before仅仅要求前前一个操作执行结果对后一个操作可见，且前一个操作按顺序排在第二个操作之前。

对于Java程序员来说，happens-before规则简单，它避免了我们为了理解JMM提供的内存可见性保证而去学习复杂的重排序规则以及这些规则的具体实现方法。

### 3.2 重排序
重排序是指编译器和处理器为了优化程序性能而对指令序列重新排序的一种手段。

#### 3.2.1 数据依赖性
如果两个操作访问同一个变量，且这两个操作中有一个为写操作，此时两个操作之间就存在数据依赖性。数据依赖性分为一下3种
1. 写后读 `a=1;b=a;` 写一个变量之后，再读这个变量。
2. 写后写 `a=1;a=2;` 写一个变量之后，再写这个变量。
3. 读后写 `a=b;a=1;` 读一个变量之后，再写这个变量。

以上3种操作，只要重排序两个操作的顺序，程序的执行结果就会改变。  
这里所说的数据依赖性仅针对单个处理器中执行的指令序列和单个线程中执行的操作。不同处理器线程之间的数据依赖性不被编译器和处理器考虑。

#### 3.2.2 as-if-serial语义
意思：不管怎么重排序，单线程程序的执行结果不能改变。编译器、runtime和处理器都必须遵守as-if-serial。  
as-if-serial语义把单线程程序保护了起来，使程序员产生了幻觉：单线程程序是按程序的顺序来执行的。

#### 3.2.3 程序顺序规则
在不改变程序执行结果的前提下，尽可能提高并行度。编译器和处理器遵从这一目标，从happens- before的定义我们可以看出，JMM同样遵从这一目标。  

#### 3.2.4 重排序对多线程的影响
1. 重排序会破坏多线程的语义。
2. 在单线程中，对存在控制依赖的操作重排序不会改变执行结果。但在多线程中，存在控制依赖的操作重排序，可能会改变程序的执行结果。

### 3.3 顺序一致性
顺序一致性内存模型是一个理论参考模型，处理器的内存模型和编程语言的内存模型都会以顺序一致性内存模型作为参照。

#### 3.3.1 数据竞争与数据一致性
如果程序是正确同步的，程序的执行将具有顺序一致性。

#### 3.3.2 顺序一致性模型
两大特性：
1. 一个线程中所有操作必须按照程序的顺序执行。
2. （不管程序是否同步）所有线程都只能看到一个单一的操作执行顺序，每个操作都必须原子执行且立刻对所有线程可见。

#### 3.3.3 同步程序的顺序一致性效果
这是一个正确同步的多线程程序。根据JMM规范，该程序的执行结果将与该程序在顺序一致性模型中的执行结果相同。  
JMM在具体实现上的基本方针为：在不改变（正确同步的）程序执行结果的前提下，尽可能地为编译器和处理器的优化打开方便之门。

#### 3.3.4 未同步程序的执行特性
JMM只提供最小安全性：线程执行时读取到的值，要么就是之前某个线程写入的，要么是默认值。  

JMM与顺序一致性的差异：
1. 顺序一致性保证单线程内的操作会按程序的顺序执行，JMM不能保证。
2. 顺序一致性模型保证所有线程只能看到一致的操作执行顺序，JMM不能保证。
3. JMM不保证对64位数据的写操作具有原子性，顺序一致性保证所有的内存读/写都是原子的。

### 3.4 volatile的内存语义
#### 3.4.1 volatile的特性
* 可见性：对一个volatile变量的读，总是能看到对这个volatile变量最后的写入。
* 原子性：对任意单个volatile变量的读/写具有原子性， 但类似++这种复合操作不具备原子性。

#### 3.4.2 volatile写-读建立的happens-before关系
从JSR-133开始，volatile变量的读写可以实现线程之间的通信。  
volatile的写-读，与锁的释放-获取有相同的内存效果。

#### 3.4.3 volatile写-读的内存语义
volatile写语义：当写一个volatile变量时，JMM会把该线程对应的本地内存中的共享变量值刷新到主存中。  
volatile读语义：当读一个volatile变量时，JMM会把该线程对应的本地内存置为无效，线程接下来将从主存中读取共享变量。

* 线程A写一个volatile变量，实质上是线程A向接下来将要读这个volatile变量的某个线程发出了消息。
* 线程B读一个volatile变量，实质上是线程B接收了之前某个线程发出的消息。
* 线程A写一个volatile变量，随后线程B读这个变量，实质上是线程A通过主存向B发送消息。

#### 3.4.4 volatile内存语义的实现
* 当第二个操作是volatile写时，不管第一个操作是什么，都不能重排序。这个规则保证volatile写之前的操作不会被编译器重排序到volatile写之后。
* 当第一个操作是volatile读时，不管第二个操作是什么，都不能重排序到volatile读之前。
* 当第一个操作是volatile写，第二个操作是volatile读时，不能重排序。

内存屏障：
* 在每个volatile写操作之前插入一个StoreStore屏障。
* 在每个volatile写操作之后插入一个StoreLoad。
* 在每个volatile读操作之后插入一个LoadLoad屏障。
* 在每个volatile读操作之前插入一个LoadStore。

在X86处理器中，仅会对写-读操作做重排序。因此JMM仅需在volatile写后面插入一个StoreLoad屏障。

#### 3.4.5 JSR-133为什么增强volatile内存语义
因为volatile的写-读没有锁的释放-获取具有的内存语义。

### 3.5 锁的内存语义
#### 3.5.1 锁的释放-获取建立的happens-before关系
与volatile写-读相似

#### 3.5.2 锁的释放和获取的内存语义
与volatile写-读相似 实际上是线程A向线程B发送消息。

#### 3.5.3 锁内存语义的实现
以ReentrantLock的源码为例
公平锁的加锁（获取锁）是读取一个volatile变量（state），公平锁的释放锁是写volatile变量state。
非公平锁的加锁（获取锁）是CAS操修改state变量，非公平锁的释放也是volatile变量的写操作。

CAS如何同时具有volatile读和写的内存语义的：
CAS源码指令cmpxchg，如果是多核处理器程序会给cmpxchg指令加上lock前缀：lock cmpxchag

lock指令前缀：
1. 确保对内存的读-改-写操作原子执行。
2. 禁止该指令与之前和之后的指令重排序。
3. 把写缓冲区的所有数据刷新到内存中。  

上了面2.3点所具有的内存屏障效果，足以实现volatile读和写。

#### 3.5.4 concurrent包的实现
Java线程间通信有4中方式：
1. A线程写volatile变量，B线程读volatile变量
2. A线程写volatile变量，B线程CAS操作volatile变量
3. A线程CAS操作volatile变量，B线程读volatile变量
4. A线程CAS操作volatile变量，B线程CAS操作volatile变量

volatile变量的读/写和CAS可以实现线程间的通讯，把这些整合在一起就形成了concurrent包的基石。

### 3.6 final域的内存语义
#### 3.6.1 final域的重排序
对于final域，编译器处理器要遵循两个重排序规则：
1. 在构造函数内对一个final域的写入，与随后把这个对象的引用赋值给一个引用变量，这两个操作之间不能重排序
2. 初次读一个包含final域的对象的引用时，与随后初次读这个final域，这两个操作之间不能重排序。

例子：见chapter3.FinalExample

#### 3.6.2 写final域的重排序规则
规则的实现包含2个方面：
1. JMM禁止编译器把final域的写重排序到构造函数之外。
2. 编译器会在final域的写之后，构造函数return之前，插入一个StoreStore屏障。这个屏障禁止处理器把final域的写重排序到构造函数之外。

#### 3.6.3 读final域的重排序规则
在一个线程中，初次读对象引用与初次读该对象包含的final域，JMM禁止处理器重排序这两个操作。编译器会在读final域操作之前插入一个LoadLoad屏障。（只针对处理器，我们自己编码的时候都是用对象.属性的方式，在程序员看来是遵循happens-before原则的，这是说的底层实现）

#### 3.6.4 final域为引用类型
例子：见chapter3.FinalReferenceExample

对于引用的对象，写final域的重排序规则对编译器和处理器增加了如下约束：  
在构造函数内对一个final引用的对象的成员域的写入，与随后在构造函数外把这个构造对象的引用赋值给一个引用变量，这两个操作之间不能重排序。

#### 3.6.5 为什么final引用不能从构造函数内“溢出”
在构造函数内部，不能让这个被构造的对象的引用为其他线程所见，也就是对象引用不能在构造函数中“溢出”。

#### 3.6.6 final语义在处理器中的实现
由于X86处理器不会对写-写做重排序，所以会省略掉写fianl域的StoreStore屏障。同样，由于X86处理器不会对存在间接依赖关系的操作做重排序，所以读final域的LoadLoad操作也会被省略。也就是说，在X86处理器中，final域的读写都不会插入任何内存屏障。

#### 3.6.7 JSR-133为什么要增强final语义
因为旧的内存模型中，线程可能看到final域的值会改变。（默认值0 -> 构造复制1）  
为了修复漏洞：只要对象正确的构造，被构造对象的引用在构造函数中没有“逸出“。

### 3.7 happens-before
#### 3.7.1 JMM的设计
设计JMM需要考虑两个因素：
1. 程序员对内存模型的使用。易于理解，易于编程，强内存模型
2. 编译器处理器对内存模型的实现。束缚越少越好，弱内存模型

策略：
1. 对于会改变程序执行结果的重排序，JMM禁止。
2. 对于不会改变执行结果的重排序，JMM允许。

#### 3.7.2 happens-before的定义
1. 一个操作happens-before另一个操作，那么第一个操作的执行结果将对第一个操作可见，而且第一个操作在第二个操作之前。
2. 两个操作存在happens-before关系，并不意味着必须按照制定顺序进行，只要重排序后执行结果相同。

#### 3.7.3 happens-before规则
1. 程序顺序规则：一个线程中的每个操作，happens-before于该线程的任意后续操作。
2. 监视器锁规则：对一个锁的解锁，happens-before于随后对这个锁的加锁。
3. volatile变量规则：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。
4. 传递性：A hb B -> B hb C -> A hb C
5. start()规则：线程的start()操作happens-before于线程中的任意操作。
6. join()规则：线程的任意操作happens-before线程的join()操作。

### 3.8 双重检查锁定与延迟初始化
#### 3.8.1 双重检查锁定的由来
典型例子：懒加载的单例模式

例子：chapter3.UnsafeLazyInitialization/SafeLazyInitialization/DoubleCheckLocking

#### 3.8.2 问题的根源
见例子：chapter3.DoubleCheckLocking

#### 3.8.3 volatile解决方案
例子：chapter3.SafeDoubleCheckedLocking

#### 3.8.4 给予初始化的解决方案
例子：chapter3.InstanceFactory

### 3.9 Java内存模型综述
#### 3.9.1 处理的内存模型
| 内存模型 | 对应的处理器 | Store-Load重排序 | Store-Store重排序 | Load-Load/Load-Store重排序 | 可以更早读取到其他处理器的写 | 可以更早读取到当前处理器的写 |
| TSO | sparc-TSO x64 | Y | ~ | ~ | ~ | Y |
| PSO | sparc-PSO | Y | Y | ~ | ~ | Y |
| RMO | ia64 | Y | Y | Y | ~ | Y |
| PowerPC | PowerPC | Y | Y | Y | Y | Y |  

从上表中看出所有处理器都允许 写-读 重排序。它们都使用了写缓冲区。  
同时都允许更早读到当前处理器的写，原因也是写缓冲区。由于写缓冲区只对当前处理器可见，这个特性可以导致当前处理器比其他处理器先看到临时保存在自己缓冲区中的写。  

JMM在不同处理器平台插入的内存屏障：
* TSO内存模型`sparc-TSO X86` `StoreLoad`  
* PSO内存模型`sparc-PSO` `StoreLoad StoreStore`
* RMO/PowerPC内存模型`ia64 PowerPC` `StoreLoad StoreStore LoadStore LoadLoad`

#### 3.9.2 各种内存模型之间的关系
* JMM是一个语言级的内存模型
* 处理器内存模型是硬件级的内存模型
* 顺序一致性内存模型是一个参考模型

处理器内存模型比语言内存模型弱，语言内存模型比顺序一致性内存模型弱。越弱越易于编程，但性能越差。

#### 3.9.3 JMM的内存可见性保证
1. 单线程不会出现内存可见性问题。
2. 正确同步的多线程将具有顺序一致性。
3. 未同步/未正确同步的多线程，JMM提供最小安全性保障：要么是前一个线程的写入，要么是默认值(0,null,false)

#### 3.9.4 JSR-133对旧的内充模型的修补
1. 增强volatile的内存语义
2. 增强final的内存语义

### 3.10 本章小结

## 第4章 Java并发编程基础
### 4.1 线程简介
#### 4.1.1 什么是线程
操作系统调度的最小单元是线程，也叫轻量级进程。

#### 4.1.2 为什么要使用多线程
1. 更多的处理器核心
2. 更快的响应时间
3. 更好的编程模型

#### 4.1.3 线程优先级
操作系统采用时分的形式调度运行线程，分出一个个的时间片，线程会分配到若干个时间片，当时间片用完了就会发生线程的调度，并等待下次分配。
线程能分配多少时间片决定了使用处理器资源的多少，而线程优先级就是决定线程分配处理器资源的属性。  

Java线程中，通过变量`priority`来控制优先级，范围1~10。在创建线程的时候可以通过`setPriority(int)`方法来修改优先级，默认是5，优先级高的线程分配时间片数量比低的多。

`!注意*` 程序的正确性不能依赖优先级！！！！

`例: chapter4.Priority.java`

#### 4.1.4 线程的状态
* NEW:初始状态，线程被构建，但还没有调用start()
* RUNNABLE:运行状态，Java线程将操作系统中的就绪和运行两种状态笼统的称作“运行中”
* BLOCKED:阻塞状态，表示线程阻塞于锁
* WAITING:等待状态，表示线程进入等待状态，进入该状态表示当前线程需要等待其他线程做出一些特定动作（通知或中断）
* TIME_WAITING:超时等待，该状态与WAITING不同，它是可以在指定时间自行返回
* TERMINATED:终止状态，表示当前线程已经执行完毕

线程创建之后，调用start()方法开始运行。当线程执行wait()方法之后，线程进入等待状态。进入等待状态的线程需要依靠其他线程的通知才能够返回到运行状态，而超时等待状态相当于在等待状态的基础上增加了超时限制，也就是超时时间到达时将会返回到运行状态。当线程调用同步方法时，在没有获取到锁的情况下，线程将会进入到阻塞状态。线程在执行Runnable的run()方法之后将会进入到终止状态。  
`例: chapter4.ThreadState.java`

#### 4.1.6 Daemon线程
Daemon线程是一种支持型线程，因为它主要被用作程序中后台调度以及支持性工作。这意味着，当一个Java虚拟机中不存在非Daemon线程的时候，Java虚拟机将会退出。可以通过调用Thread.setDaemon(true)将线程设置为Daemon线程。  
Daemon属性需要在启动线程之前设置，不能在启动线程之后设置。Daemon线程被用作完成支持性工作，但是在Java虚拟机退出时Daemon线程中的finally块并不一定会执行。  

### 4.2 启动和终止线程
start()方法进行启动，随着run()方法的执行完毕，线程也随之终止。

#### 4.2.1 构造线程
一个新构造的线程对象是由其parent线程来进行空间分配的，而child线程继承了parent是否为Daemon、优先级和加载资源的contextClassLoader以及可继承的ThreadLocal，同时还会分配一个唯一的ID来标识这个child线程。

#### 4.2.2 启动线程
调用start()方法就可以启动这个线程。线程start()方法的含义是：当前线程（即parent线程）同步告知Java虚拟机，只要线程规划器空闲，应立即启动调用start()方法的线程。

#### 4.2.3 理解中断
其他线程调用了当前线程的`interrupt()方法`。检查是否被中断调用当前线程的方法`tread.isInterrupted()`。检查当前线程是否被中断，并清楚标志位调用静态方法`Tread.interrupted()`。  
* interrupt() 设置中断标志。
* isInterrupted() 检查中断标志。
* interrupted() 检查标志并清除标志。  

`例子 c4.TestInterrupt`

#### 4.2.4 过期的suspend() resume() stop()
`suspend()` 占用锁 容易死锁  
`stop()` 不释放资源

#### 4.2.5 安全的终止线程
1. 使用中断操作
2. boolean信号量

`例子 c4.StopThreadDemo.java`  

### 4.3 线程间的通信
#### 4.3.1 volatile和synchronized
关键字volatile可以用来修饰字段（成员变量），保证所有线程对变量访问的可见性。
 
关键字synchronized可以修饰方法或者以同步块的形式来进行使用，保证了线程对变量访问的可见性和排他性。  
同步块的实现使用了monitorenter和monitorexit指令，而同步方法则是依靠方法修饰符上的ACC_SYNCHRONIZED来完成的。其本质是对一个对象的监视器（monitor）进行获取，而这个获取过程是排他的，也就是同一时刻只能有一个线程获取到由synchronized所保护对象的监视器。

### 重要补充：阻塞线程
#### 1 线程状态及转换  
当多个线程同时请求某个监视器时，对象监视器会设置几种状态用来区分请求的线程：
* Contention List：所有请求锁的线程将被首先放置到该竞争队列。
* Entry List：Contention List中那些有资格成为候选人的线程被移到Entry List。
* Wait Set：那些调用wait方法被阻塞的线程被放置到Wait Set。
* OnDeck：任何时刻最多只能有一个线程正在竞争锁，该线程称为OnDeck。
* Owner：获得锁的线程称为Owner。
* !Owner：释放锁的线程。

新请求锁的线程将首先被加入到ConetentionList中，当某个拥有锁的线程（Owner状态）调用unlock之后，如果发现EntryList为空则从ContentionList中移动线程到EntryList，下面说明下ContentionList和EntryList的实现方式。

#### 2 ContentionList虚拟队列
ContentionList并不是一个真正的Queue，而只是一个虚拟队列，原因在于ContentionList是由Node及其next指针逻辑构成，并不存在一个Queue的数据结构。ContentionList是一个后进先出（LIFO）的队列，每次新加Node时都会在队头进行，通过CAS改变第一个节点的指针为新增节点，同时设置新增节点的next指向后续节点，而取得操作则发生在队尾。显然，该结构其实是个Lock-Free的队列。  
因为只有Owner线程才能从队尾取元素，也即线程出列操作无争用，当然也就避免了CAS的ABA问题。

#### 3 EntryList
EntryList与ContentionList逻辑上同属等待队列，ContentionList会被线程并发访问，为了降低对ContentionList队尾的争用，而建立EntryList。Owner线程在unlock时会从ContentionList中迁移线程到EntryList，并会指定EntryList中的某个线程（一般为Head）为Ready（OnDeck）线程。Owner线程并不是把锁传递给OnDeck线程，只是把竞争锁的权利交给OnDeck，OnDeck线程需要重新竞争锁。这样做虽然牺牲了一定的公平性，但极大的提高了整体吞吐量，在Hotspot中把OnDeck的选择行为称之为“竞争切换”。  
OnDeck线程获得锁后即变为owner线程，无法获得锁则会依然留在EntryList中，考虑到公平性，在EntryList中的位置不发生变化（依然在队头）。如果Owner线程被wait方法阻塞，则转移到WaitSet队列；如果在某个时刻被notify/notifyAll唤醒，则再次转移到EntryList。   

`例子：c4.TestSynFair` 在测试中，我们发现线程是按逆序执行的及 FILO先进后出 原因在于 ContentionList虚拟队列

#### 4.3.2 等待/通知机制
1. 使用wait()、notify()和notifyAll()时需要先对调用对象加锁。
2. 调用wait()方法后，线程状态由RUNNING变为WAITING，并将当前线程放置到对象的等待队列。
3. notify()或notifyAll()方法调用后，等待线程依旧不会从wait()返回，需要调用notify()或notifAll()的线程释放锁之后，等待线程才有机会从wait()返回。
4. notify()方法将等待队列中的一个等待线程从等待队列中移到同步队列中，而notifyAll()方法则是将等待队列中所有的线程全部移到同步队列，被移动的线程状态由WAITING变为BLOCKED。
5. 从wait()方法返回的前提是获得了调用对象的锁。

#### 4.3.3 等待/通知的经典范式
等待方遵循如下原则：
1. 获取对象的锁。
2. 如果条件不满足，那么调用对象的wait()方法，被通知后仍要检查条件。
3. 条件满足则执行对应的逻辑。  
```java
synchronized(对象) {
    while(条件不满足) {
        对象.wait();
    }
    处理逻辑;
}
```
通知方遵循如下原则：
1. 获得对象的锁。
2. 改变条件。
3. 通知所有等待在对象上的线程。
```java
synchronized(对象) {
    改变条件;
    对象.notifyAll();
}
```

#### 4.3.4 管道输入/输出流
管道输入/输出流主要包括了如下4种具体实现：PipedOutputStream、PipedInputStream、PipedReader和PipedWriter，前两种面向字节，而后两种面向字符。  
`例子c4.Piped`  
对于Piped类型的流，必须先要进行绑定，也就是调用connect()方法，如果没有将输入/输出流绑定起来，对于该流的访问将会抛出异常。

#### 4.3.5 Thread.join()的使用
如果一个线程A执行了thread.join()语句，其含义是：当前线程A等待thread线程终止之后才从thread.join()返回。   
join()方法的逻辑结构与4.3.3节中描述的等待/通知经典范式一致，即加锁、循环和处理逻辑3个步骤。  
`例子：c4.ThreadJoin `  
!*自己线程join自己线程 死锁 不会抛出异常 程序中应该判断是否为当前线程

#### 4.3.6 ThreadLocal的使用
`例子：c4.Profiler`

### 4.4 线程应用实例
#### 4.4.1 等待超时模式
调用一个方法时等待一段时间（一般来说是给定一个时间段），如果该方法能够在给定的时间段之内得到结果，那么将结果立刻返回，反之，超时返回默认结果。  
```java
public synchronized Object get(long mills) throws InterruptedException {
    long futrue = System.currentTimeMillis() + mills;
    long remaining = mills;
    while((result == null) && remaning > 0) {
        wait();
        remaining = futrue - System.currentTimeMillis();
    }
    return result;
}
```

#### 4.4.2 一个简单的数据库连接池
`例子：c4.a1.*`

#### 4.4.3 线程池技术及其示例
`例子：c4.a2。*`

#### 4.4.4 一个基于线程池技术的简单web服务器
`例子：c4.a3.*`

## 第五章 Java中的锁
### 5.1 Lock接口
Lock的使用方式:
```java
Lock lock = new ReentrantLock();
lock.lock();
try{
} finally {
    lock.unlock();
}
```
在finally块中释放锁，目的是保证在获取到锁之后，最终锁能被释放。不要将获取锁的过程写在try块中，因为如果在获取锁时发生了异常，会导致锁无故释放。  

相对Lock，Synchronized关键字不具备的特性：
- 尝试非阻塞地获取锁：当线程尝试获取锁，如果这一刻锁没有被其他线程获取，则成功获取并持有锁
- 能被中断的获取锁：与synchronized不同，获取到锁的线程能够响应中断，当获取到锁的线程被中断时，中断异常将会被抛出，同时锁会被释放
- 超时获取锁：在指定的截止时间之间获取锁，如果截止时间到了扔无法获取锁，则返回

Lock的API
- `void lock()`：获取锁，调用该方法当前线程将会获取锁，当锁获得后，从该方法返回
- `void lockInterruptibly() throws InterruptedException`：可中断的获取锁，和`lock()`方法不同之处在于该方法会响应中断，即在锁的获取中可以中断当前线程
- `boolean tryLock()`：尝试非阻塞的获取锁，调用该方法后立刻返回，如果能获取则返回true，否则返回false
- `boolen tryLock(long time,TimeUnit unit) throws InterruptedException`：超时的获取锁，当前线程在以下三种情况会返回：
   1. 当前线程在超时时间内获得了锁
   2. 当前线程在超时时间内被中断
   3. 超时时间结束，返回false
- `void unlock()`：释放锁
- `Condition newCondition()`：获取等待通知组件，该组件和当前的锁绑定，当前线程只有获得了锁，才能被调用该组件的`await()`方法，而调用后，当前线程将释放锁

### 5.2 队列同步器(AQS)
#### 5.2.1 队列同步器的接口与示例
重写同步器指定方法时，需要使用同步器提供的如下3个方法来访问修改同步状态：
* `getState()`：获取当前同步状态
* `setState()`：设置当前同步状态
* `compareAndSetState(int expect,int update)`：使用CAS设置当前状态，改方法能保证状态设置的原子性

同步器可重写的方法
* `protected boolean tryAcquire(int arg)`：独占式获取同步状态，实现该方法需要查询当前状态并判断同步状态是否符合预期，然后再进行CAS设置同步状态
* `protected boolean tryRelease(int arg)`：独占式释放同步状态，等待获取同步状态的线程将会有机会获取同步状态
* `protected int tryAcquireShared(int arg)`：共享式获取同步状态，返回大于等于0的值，表示获取成功，反之，获取失败
* `protected boolean tryReleaseShared(int arg)`：共享式释放同步状态
* `protected boolean isHeldExclusively()`：当前同步器是否在独占模式下被线程占用，一般该方法表示是否被当前线程所独占

同步器的模板方法
* `void acquire(int arg)`：独占式获取同步状态，如果当前线程获取同步状态成功，则由该方法返回，否则，将会进入同步队列等待，该方法将会调用重写的`tryAcquire(int arg)`方法
* `void acquireInterruptibly(int arg)`：与`acquire(int arg)`相同，但是该方法响应中断，当前线程未获取到同步状态而进入同步队列中，如果当前线程被中断，则改方法会抛出InterruptedException并返回
* `boolean tyrAcquireNanos(int arg,long nanos)`：在`acquireInterruptibly(int arg)`基础上增加了超时限制，如果当线程在超时时间内没有获取到同步状态，那么将会返回false，如果获取到了返回true
* `void acquireShared(int arg)`：共享式的获取同步状态，如果当前线程未获取到同步状态，将会进入同步队列等待，与独占式获取的主要区别是同一刻可以有多少个线程获取到同步状态
* `void acquireSharedInterruptibly(int arg)`：与`void acquireShared(int arg)`相同，该方法响应中断
* `boolean tryAcquireSharedNanos(int arg,long nanos)`：在`void acquireShared(int arg)`基础上增加了超时限制
* `boolean release(int arg)`：独占式的释放同步状态，该方法会在释放同步状态之后，将同步状态中第一个节点包含的线程唤醒
* `boolean releaseShared(int arg)`：共享式释放同步状态
* `Collection<Thread> getQueuedThreads()`：获取等待在同步队列上的线程集合

`例子：c5.Mutex`

#### 5.2.2 队列同步器的实现分析
1. 同步队列：同步器依赖内部的同步队列（一个FIFO的双向队列），获取同步状态失败的线程，会构造队列节点node以尾部添加的方式进入同步队列并阻塞线程。为保证队列的线程安全，会使用CAS的设置尾节点的方法：`compareAndSetTail(Node expect,Node update)`。节点属性如下
   * int waitStatus：等待状态，包含如下状态
     1. CANCELLED，值为1，由于在同步队列中等待超时或者被中断，需要从同步队列中取消等待，节点进入该状态将不会变化。
     2. SIGNAL，值为-1，后继节点的线程处于等待状态，而当前节点的线程如果释放了同步状态或者被取消了，将会通知后继节点，使后继节点的线程得以运行
     3. CONDITION，值为-2，节点在等待队列中，节点线程等待在Condition上，当其他线程对Condition调用了signal()方法后，该节点将会从等待队列中转移到同步队列中，加入到对同步状态的获取中。
     4. PROPAGET，值为-3，表示下一次共享式同步状态获取会无条件地被传播下去
     5. INITIAL，值为0，初始状态
   * Node prev：前驱节点，当节点加入同步队列时被设置（尾部添加）
   * Node next：后继节点
   * Node nextWaiter：等待队列中的后继节点。如果当前节点时共享的，那么这个字段是一个SHARED常量，也就是说节点类型（独占和共享）和等待队列中的后继节点公用一个字段
   * Thread thread：获取同步状态的线程
   
   !*注意! 第一次未获取到同步状态的线程 会使用`compareAndSetHead(Node node)`方法构造一个新节点，该节点的`thread=null`并使`head`和`tail`都等于该节点。

2. 独占式同步状态获取与释放  
   总结：在获取同步状态时，同步器维 护一个同步队列，获取状态失败的线程都会被加入到队列中（死循环cas设置成尾节点）并在队列中进行自旋；移出队列 （或停止自旋）的条件是前驱节点为头节点且成功获取了同步状态。在释放同步状态时，同步 器调用tryRelease(int arg)方法释放同步状态，然后唤醒头节点的后继节点。`独占式获取锁.jpg`
   
3. 共享式样获取同步状态与释放  
   
   读读共享，读写互斥，写写互斥。
   1. 当共享式获取同步状态的线程获取到同步状态时，允许所有共享式获取同步状态的线程进入，但不允许独占式获取同步状态的线程进入。
   2. 当独占式获取同步状态的线程获取到同步状态时，任何线程都不允许获取同步状态。  
   
   共享式获取与独占获取的不同：
   1. 共享式获取同步状态时，判断前置节点是否是头节点之后，获取同步状态返回值是否大于0。
      ```java
        //独占式判断
        if (p == head && tryAcquire()) {
              ....
        }
        //共享式获取
        if (p == head) {
            int r = tryAcquireShard();
            if (r > 0) {
                 ....
            }
        }
      ```
   2. 共享式释放锁需要cas操作保证线程安全，独占式不需要，因为共享式多线程访问，独占式永远只有一个线程来释放锁。
   
4. 独占式超时获取同步状态  
   `doAcquireNanos(int arg,long nanosTimeout)` 和独占式获取同步状态`acquire(int args)`在流程上非常相似，其主要区别在于未获取到同步状 态时的处理逻辑。acquire(int args)在未获取到同步状态时，将会使当前线程一直处于等待状 态，而doAcquireNanos(int arg,long nanosTimeout)会使当前线程等待nanosTimeout纳秒，如果当 前线程在nanosTimeout纳秒内没有获取到同步状态，将会从等待逻辑中自动返回。`独占式超时获取同步状态.jpg`
   
5. 自定义同步组件-TwinsLock  
   设计一个允许2个线程同时获取同步状态的锁  
   `例子:c5.TwinsLock`  
   sync 必须重写 `tryAcquire(int arg) tryAcquireShared(int arg)` AQS中在final方法`acquire(int arg) acquireShared(int arg)`中都会调用自己重写的`try...()`方法

### 5.3 重入锁
`synchronized`隐式支持重入

#### 5.3.1 实现重入 ReentrantLock
1. 线程再次获取锁。每次获取 +1。
2. 锁的最终释放。释放一次 -1，释放至0，其他线程方可进入。

#### 5.3.2 公平与非公平
```java
//公平锁
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() &&  // 与公平锁唯一的不同在此，判断是否有前驱节点
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
//非公平锁
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}                      
```
`示例：c5.TestReentrantLock.java`  

总结：公平性锁保证了锁的获取按照FIFO原则，而代价是进行大量的线程切换。非公平性锁虽然可能造成线程“饥饿”，但极少的线程切换，保证了其更大的吞吐量。

### 5.4 读写锁 ReentrantReadWriteLock
特性：
1. 公平性选择：默认非公平，支持公平，吞吐量非公平>公平
2. 重入性：读线程在获取读锁后，可以再次获取读锁。写线程在获取写锁后，可以再次获取锁，也可获取读锁。
3. 锁降级：遵循获取写锁、获取读锁再释放写锁的次序，写锁能够降级成为读锁。

#### 5.4.1 读写锁的接口示例
展示内部工作状态的方法：
* `int getReadLockCount()`: 返回当前读锁被获取的次数，该次数不等于获取读锁的线程数。
* `int getReadHoldCount()`: 返回当前线程获取读锁的次数。Java6中加入，使用ThreadLocal保存
* `boolean isWriteLock()`: 判断写锁还是读锁
* `int getWriteHoldCount()`: 返回当前写锁被获取次数  

`示例：c5.Cache.java`

#### 5.4.2 读写锁的实现分析
1. 读写状态的设计：  
   
   高位表示读，低位表示写。使用位移运算来获取状态，获取写`write = state & 0x0000FFFF`，获取读`read = state >>> 16`

2. 写锁的获取与释放：
   
   ```java
   protected final boolean tryAcquire(int acquires) {
       Thread current = Thread.currentThread();
       int c = getState();
       int w = exclusiveCount(c); // c & 0x0000FFFF 低位值（记录写）
       if (c != 0) {
           // (Note: if c != 0 and w == 0 then shared count != 0)即存在读锁，或者
           if (w == 0 || current != getExclusiveOwnerThread())
               return false;
           if (w + exclusiveCount(acquires) > MAX_COUNT)
               throw new Error("Maximum lock count exceeded");
           // Reentrant acquire
           setState(c + acquires);
           return true;
       }
       if (writerShouldBlock() ||
           !compareAndSetState(c, c + acquires))
           return false;
       setExclusiveOwnerThread(current);
       return true;
   }
   ```
   
   3. 读锁的获取与释放：
   
      ```java
      protected final int tryAcquireShared(int unused) {
          for(;;) {}
              int c = getStaus();
              int nextc = c + (1 >> 16);
              if (nextc < c) {
                  throw new Error("Maximum lock count exceeded");
              }
              // 判断读锁是否被获取 并且 获取读锁的线程非当前线程
              if (exclusiveCount(c) != 0 && owner != Thread.currentThread) {
                  return -1;
              }
              if (compareAndSetState(c, nextc))
                  return 1;       
          }
      ```
   4. 锁降级
   
      ```java
      public void processData() {
          readLock.lock();
          if (!update) {
              // 必须先释放读锁
              readLock.unLock();
              // 锁降级从写锁获取开始
              writeLock.lock();
              try {
                  if (!update) {
                      // 准备数据的流程（略）
                      update = true;
                  }
                  readLock.lock();
              } finally {
                  writeLock.unLock();
              }
              // 锁降级完成，写锁降级为读锁   
          }
          try {
              // 使用数据的流程（略）
          } finally {
              readLock.unLock();
          }
      }
      ```
 
 ### 5.5 LockSupport工具
 LockSupport提供的阻塞和唤醒方法：
 1. `void park()`:阻塞当前线程，如果调用`unpark(Thread thread)`方法或者当前线程被中断，才能从`park()`方法返回。
 2. `viod parkNanos(long nanos)`:阻塞当前线程，最长不超过nanos纳秒，返回条件在`park()`的基础上增加了超时返回。
 3. `void parkUntil(long deadline)`:阻塞当前线程，直到deadline时间（1970到deadline的毫秒值）
 4. `void unpark(Thread thread)`:唤醒阻塞状态的线程。
 
 ### 5.6 Condition接口
 Object的监视器方法与Condition接口的对比：
 * 前置条件：
   * Object：获取对象时
   * Condition：调用`lock.lock()`获取锁，调用`Lock.newCondition()`获取Conditiond对象
 * 调用方式：
   * 直接调用，如object.wait()
   * 直接调用，如condition.await()
 * 等待队列个数：
   * 一个
   * 多个
 * 当前线程释放锁并进入等待状态
   * 支持
   * 支持
 * 当前线程释放锁并进入等待状态，在等待状态中不响应中断
   * 不支持
   * 支持
 * 当前线程释放锁并进入等待状态到将来的某个时间
   * 不支持
   * 支持
 * 唤醒等待队列中的一个线程
   * 支持
   * 支持
 * 唤醒等待队列中的全部线程
   * 支持
   * 支持

#### 5.6.1 Condition接口示例
`示例: c5.BoundedQueue.java`

#### 5.6.2 Condition的实现分析
每个Condition都有一个自己的等待队列。

1. 等待队列
   等待队列是一个FIFO的队列，节点复用了同步队列。节点引用更新过程无CAS操作，因为都是加了锁的。  
   一个对象拥有 **一个同步队列** 和 **多个等待队列**。  
   Condition的实现是同步器的内部类，因此每个Condition实例都能访问同步器提供的方法，相当于每个Condition都拥所属同步器的引用。

2. 等待  
   `Condition.await()`方法：调用该方法的线程成功获取了锁的线程，也就是同步队列中的首节点，该方法会将当前 线程构造成节点并加入等待队列中，然后释放同步状态，唤醒同步队列中的后继节点，然后当 前线程会进入等待状态。  
   如果从队列的角度去看，当前线程加入Condition的等待队列

3. 唤醒
   `Condition.signal()`方法：调用Condition的signal()方法，将会唤醒在等待队列中等待时间最长的节点（首节点），在 唤醒节点之前，会将节点移到同步队列中。调用该方法的前置条件是当前线程必须获取了锁，可以看到signal()方法进行了 isHeldExclusively()检查，也就是当前线程必须是获取了锁的线程。接着获取等待队列的首节点，将其移动到同步队列并使用LockSupport唤醒节点中的线程。  
   节点从等待队列移动到同步队列的过程
   
### 5.7 本章小结
详细地剖析了队列同步器、重入锁、读写锁以及 Condition等API和组件的实现细节，只有理解这些API和组件的实现细节才能够更加准确地运 用它们。

## 第六章 Java并发容器和框架
### 6.1 ConcurrentHashMap的实现原理与使用
已过时，省略此节内容！！！！！

