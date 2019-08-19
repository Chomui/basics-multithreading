package com.artemchep.basics_multithreading.domain;

import android.os.Handler;

import com.artemchep.basics_multithreading.cipher.CipherUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleThread extends Thread {
    private Queue<WithMillis<Message>> queue = new LinkedList<>();
    private List<WithMillis<Message>> list = new ArrayList<>();
    private boolean isRunning = true;
    private int countOut = 0;
    private Handler handler;

    public SimpleThread(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for(int i = 0; i < 5; i++) {
            executorService.submit(new CipherRunnable(queue, handler));
        }
        while (isRunning) {

        }
    }

    public void add(WithMillis<Message> message) {
        synchronized (queue) {
            queue.add(message);
            queue.notifyAll();
        }

    }

    public void finish() {
        isRunning = false;
        synchronized (queue) {
            queue.notifyAll();
        }
    }
}
