package com.ecwid.dev.ipcounter;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class IteratorPublisher<T> implements Flow.Publisher<T> {
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR_SERVICE = parallelPool();

    private static ThreadPoolExecutor parallelPool() {
        ThreadFactory threadFactory = new NamedThreadFactory("iterator-publisher-thread");
        ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(NUM_CORES * 2);
        return new ThreadPoolExecutor(NUM_CORES, NUM_CORES, 60, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    private final Supplier<Iterator<T>> iteratorSupplier;
    private final AtomicBoolean subscribed = new AtomicBoolean();
    private Runnable finalAction;

    IteratorPublisher(Supplier<Iterator<T>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        if (!subscribed.getAndSet(true)) {
            IteratorSubscription<T> subscription = new IteratorSubscription<>(iteratorSupplier.get(), this, subscriber, EXECUTOR_SERVICE);
            subscriber.onSubscribe(subscription);
        } else {
            throw new IllegalStateException("Only one subscription is possible");
        }
    }

    public void doFinally(Runnable runnable) {
        this.finalAction = runnable;
    }

    void doFinally() {
        if (finalAction != null) {
            finalAction.run();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger();
        private final String prefix;

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + "-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
