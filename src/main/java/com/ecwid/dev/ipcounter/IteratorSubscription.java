package uniqueip;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class IteratorSubscription<T> implements Flow.Subscription {
    private final Iterator<T> iterator;
    private final IteratorPublisher<? super T> publisher;
    private final Flow.Subscriber<? super T> subscriber;
    private final ExecutorService executorService;
    private final Deque<Future<?>> tasks = new ArrayDeque<>();
    private final AtomicBoolean isTerminated = new AtomicBoolean();

    public IteratorSubscription(Iterator<T> iterator, IteratorPublisher<? super T> publisher,
                                Flow.Subscriber<? super T> subscriber, ExecutorService executorService) {
        this.iterator = iterator;
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.executorService = executorService;
    }

    @Override
    public synchronized void request(long n) {
        if (n < 0) {
            cancel();
            throw new IllegalArgumentException();
        }
        int i = 0;
        while (iterator.hasNext()) {
            if (isTerminated.get() || i == n) {
                break;
            }
            T next = iterator.next();
            Future<?> task = executorService.submit(() -> subscriber.onNext(next));
            publisher.addTask(task);
            tasks.addLast(task);
            i++;
        }
        
        if (!iterator.hasNext()) {
            subscriber.onComplete();
        }
    }

    @Override
    public void cancel() {
        isTerminated.set(true);
        for (Future<?> task : tasks) {
            if (!task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        }
    }
}
