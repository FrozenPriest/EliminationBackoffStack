package ru.frozenpriest.lab2;

import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {
    Exchanger<T>[] exchangers;
    final long Timeout;
    final TimeUnit timeUnit;
    Random random;

    @SuppressWarnings("unchecked")
    public EliminationArray(int capacity, long timeout, TimeUnit unit) {
        exchangers = new Exchanger[capacity];
        for (int i=0; i<capacity; i++)
            exchangers[i] = new Exchanger<>();
        random = new Random();
        Timeout = timeout;
        timeUnit = unit;
    }

    public T visit(T x) throws TimeoutException, InterruptedException {
        int i = random.nextInt(exchangers.length);
        return exchangers[i].exchange(x, Timeout, timeUnit);
    }
}
