# Task 1: Starting threads

Run `edu.itc.se.lab01.task1.Main` to start the countdowns concurrently, or pass
`--sequential` to start and join each countdown before starting the next. The
examples cover a `Thread` subclass, a `Runnable`, a lambda-backed thread, and a
virtual thread. Both modes wait for completion and print each thread's type and
final state.

## Task 2: Fixing a race condition

Run `edu.itc.se.lab01.task2.Bench` to compare an unsafe shared counter with
synchronized, lock-based, atomic, and `LongAdder` implementations. Each run
starts eight threads, performs one million increments per thread, then reports
the final value, whether it is the expected `8,000,000`, and elapsed time.
The unsafe counter is run three times to demonstrate lost updates.

`LongAdder` can outperform `AtomicLong` under heavy contention because it
spreads updates across multiple internal cells instead of making every thread
compete to update one value. Its `sum()` is suitable for this benchmark's
post-join read; it is not an atomic snapshot while updates are still happening.

## Why must `get()` also be synchronized?

If `increment()` is synchronized but `get()` is not, concurrent reads do not
acquire the same monitor as the writers. The read therefore has no
happens-before relationship with those updates, so it can observe a stale value
and is a data race. Synchronizing both methods on the same object provides
visibility and consistent access to the shared state.

## Task 3: Producer-consumer queues

Run `edu.itc.se.lab01.task3.Main` for the hand-written bounded buffer, or
`edu.itc.se.lab01.task3.Main2` for `ArrayBlockingQueue`. Both use the same
`Producer` and `Consumer` classes through `OrderQueue<Order>`. Two producers
create 50 uniquely numbered orders each; after all producers finish, three
poison pills let the three consumers stop. The program checks that exactly 100
orders were processed.

The hand-built buffer uses `while`-guarded waits because a thread can wake
spuriously, or wake after another thread has already changed the condition. It
must recheck that the queue is not full before putting or not empty before
taking. `notifyAll()` is needed because producers and consumers wait on the
same monitor: `notify()` can wake a thread whose condition is still false and
leave the thread that could make progress asleep. Replacing `while` with `if`
can then lead to an over-capacity buffer, removing from an empty buffer, or a
deadlock. The correct implementation intentionally retains `while` and
`notifyAll()`; these failures can be intermittent, so repeat experiments to
observe them rather than assuming one run will fail. To perform the experiment,
temporarily making both guarded waits use `if`, replacing `notifyAll()` with
`notify()`, compiling, and running `Main` 20 times reproduced the error
`NoSuchElementException` in a consumer on all 20 runs: a consumer woke after the
buffer was emptied by another consumer and proceeded without rechecking the
condition. The normal implementation was restored afterward. Buffer assertions
are enabled with the `-ea` Java option.

## Task 4: Executors, futures, and virtual threads

Run `edu.itc.se.lab01.task4.Main` for the prime-counting and download
comparisons. The CPU-bound task is split into 16 ranges and its result is
checked against a single-threaded count. The I/O simulation submits 500 tasks
to both a 20-thread platform pool and a virtual-thread-per-task executor;
every 50th task fails with `IOException`, and one task is cancelled after a
one-second timeout. Failures are collected from `Future.get()` so they do not
crash the run.

Virtual threads are typically advantageous here because sleeping downloads
spend most of their time waiting, allowing many virtual threads to suspend
without occupying a platform thread each. They do not inherently speed up
CPU-bound work, where the available processor cores limit useful parallelism.
Both executors are closed with try-with-resources so the JVM can exit normally.

## Task 5: Deadlock and graceful shutdown

Run `edu.itc.se.lab01.task5.Bank` to demonstrate a monitor deadlock. Its daemon
watchdog checks `ThreadMXBean.findDeadlockedThreads()` once per second and
reports the names of deadlocked workers. While it is hung, use `jps -l` to find
the Java process ID and `jstack <pid>` to capture the section beginning
`Found one Java-level deadlock:` into `deadlock.txt`. The deadlock excerpt must
come from an actual run; monitor identifiers and the specific thread pair vary.

Run `edu.itc.se.lab01.task5.OrderedBank` to perform 8 x 10,000 random transfers
while acquiring account monitors by ascending ID. Run
`edu.itc.se.lab01.task5.TryLockBank` to perform the same workload with
`ReentrantLock.tryLock(10, MILLISECONDS)`, unlock in all paths, random back-off,
and retry counting. Each verifies that the total balance remains 4,000.

Run `edu.itc.se.lab01.task5.GracefulShutdown` for the cancellation demonstration.
Eight workers transfer until interrupted; after two seconds main interrupts
them and gives each a one-second join timeout. Interrupted blocking calls restore
the interrupt flag, and the program reports successful transfers, attempts, retries,
termination status, and the final balance.