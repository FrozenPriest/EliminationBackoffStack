package ru.frozenpriest.lab2;

import java.util.EmptyStackException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class EliminationBackoffStack<T> {
    static final int eliminationCapacity = 100;
    static final long timeout = 10;
    static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    AtomicReference<Node<T>> top;
    EliminationArray<T> eliminationArray;

    public EliminationBackoffStack() {
        top = new AtomicReference<>(null);
        eliminationArray = new EliminationArray<>(
                eliminationCapacity, timeout, timeUnit
        );
    }

    public void push(T x) {
        Node<T> n = new Node<>(x);
        while (true) {
            if (tryCASPush(n)) return;
            try {
                T y = eliminationArray.visit(x);
                if (y == null) return;
            } catch (TimeoutException | InterruptedException ignored) {
            }
        }
    }


    public T pop() throws EmptyStackException {
        while (true) {
            Node<T> n = tryCASPop();
            if (n != null) return n.value;
            try {
                T y = eliminationArray.visit(null);
                if (y != null) return y;
            } catch (TimeoutException | InterruptedException ignored) {
            }
        }
    }

    protected boolean tryCASPush(Node<T> n) {
        Node<T> m = top.get();
        n.next = m;
        return top.compareAndSet(m, n);
    }

    protected Node<T> tryCASPop() throws EmptyStackException {
        Node<T> m = top.get();
        if (m == null) throw new EmptyStackException();
        Node<T> n = m.next;
        return top.compareAndSet(m, n) ? m : null;
    }


    private static class Node<T> {
        public T value;
        public Node<T> next;

        public Node(T value) {
            this.value = value;
        }
    }
}
