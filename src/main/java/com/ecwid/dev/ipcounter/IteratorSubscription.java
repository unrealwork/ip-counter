package com.ecwid.dev.ipcounter;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class IteratorSubscription<T> implements Flow.Subscription {
    private final Iterator<T> iterator;
    private final IteratorPublisher<? super T> publisher;
    private final Flow.Subscriber<? super T> subscriber;
    private final ExecutorService executorService;
    private final Set<Future<?>> tasks = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final AtomicLong leftTasks = new AtomicLong();

    IteratorSubscription(Iterator<T> iterator, IteratorPublisher<? super T> publisher,
                         Flow.Subscriber<? super T> subscriber, ExecutorService executorService) {
        this.iterator = iterator;
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.executorService = executorService;
    }

    @Override
    public  void request(long n) {
        if (n < 0) {
            cancel();
            throw new IllegalArgumentException();
        }
        int i = 0;
        synchronized (iterator) {
            while (iterator.hasNext()) {
                if (isTerminated.get() || i == n) {
                    break;
                }
                T next = iterator.next();
                leftTasks.incrementAndGet();
                CompletableFuture<Void> task = CompletableFuture.runAsync(() -> subscriber.onNext(next), executorService);
                tasks.add(task);
                task.thenRun(() -> {
                    tasks.remove(task);
                    if (leftTasks.decrementAndGet() == 0 && !iterator.hasNext()) {
                        subscriber.onComplete();
                        publisher.doFinally();
                    }
                });
                i++;
            }
        }
    }

    @Override
    public synchronized void cancel() {
        if (!isTerminated.getAndSet(true)) {
            for (Future<?> task : tasks) {
                if (!task.isDone() && !task.isCancelled()) {
                    task.cancel(true);
                }
            }
        }
    }
}
