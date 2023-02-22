package io.dataease.dds;

import java.util.ArrayDeque;
import java.util.Deque;

public class DynamicContextHolder {

    private static final ThreadLocal<Deque<String>> CONTEXT_HOLDER = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new ArrayDeque();
        }
    };


    public static String peek() {
        return CONTEXT_HOLDER.get().peek();
    }


    public static void push(String dataSource) {
        CONTEXT_HOLDER.get().push(dataSource);
    }


    public static void poll() {
        Deque<String> deque = CONTEXT_HOLDER.get();
        deque.poll();
        if (deque.isEmpty()) {
            CONTEXT_HOLDER.remove();
        }
    }

}
