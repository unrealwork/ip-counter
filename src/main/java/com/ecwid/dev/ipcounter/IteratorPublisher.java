package com.ecwid.dev.ipcounter;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;

class IteratorPublisher<T> implements Flow.Publisher<T> {
    private static final ExecutorService EXECUTOR_SERVICE = ForkJoinPool.commonPool();
    private final Supplier<Iterator<T>> iteratorSupplier;
    private final Deque<Future<?>> activeTasks = new ConcurrentLinkedDeque<>();
    private boolean subscribed = false;

    IteratorPublisher(Supplier<Iterator<T>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    @Override
    public synchronized void subscribe(Flow.Subscriber<? super T> subscriber) {
        if (!subscribed) {
            IteratorSubscription<T> subscription = new IteratorSubscription<>(iteratorSupplier.get(), this, subscriber, EXECUTOR_SERVICE);
            subscriber.onSubscribe(subscription);
            this.subscribed = true;
        } else {
            throw new IllegalStateException("Only one subscription is possible");
        }
    }

    public synchronized void block() throws InterruptedException, ExecutionException {
        while (!activeTasks.isEmpty()) {
            activeTasks.removeFirst().get();
        }
    }

    public void addTask(Future<?> task) {
        activeTasks.addLast(task);
    }
}
