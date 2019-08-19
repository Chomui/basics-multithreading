package com.artemchep.basics_multithreading;

import android.os.Handler;

import com.artemchep.basics_multithreading.cipher.CipherUtil;
import com.artemchep.basics_multithreading.domain.Message;
import com.artemchep.basics_multithreading.domain.WithMillis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SimpleThread extends Thread{
    private Queue<Future<WithMillis<Message>>> queue = new LinkedList<>();
    private boolean isRunning = true;
    private Handler handler;

    public SimpleThread(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while(isRunning) {
            List<Future<WithMillis<Message>>> list;
            synchronized (queue) {
                list = new ArrayList<>(queue);
                queue.clear();
            }
            for(int i = 0; i < list.size(); i++) {
                android.os.Message msg = android.os.Message.obtain();
                try {
                    msg.obj = list.get(i).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msg.setTarget(handler);
                msg.sendToTarget();
            }
            synchronized (queue) {
                if(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void add(Future<WithMillis<Message>> future) {
        synchronized (queue) {
            queue.add(future);
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
