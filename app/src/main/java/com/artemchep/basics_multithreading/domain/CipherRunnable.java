package com.artemchep.basics_multithreading.domain;

import android.os.Handler;

import com.artemchep.basics_multithreading.cipher.CipherUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CipherRunnable implements Runnable {

    private Queue<WithMillis<Message>> queue;
    private Handler handler;

    public CipherRunnable(Queue<WithMillis<Message>> queue, Handler handler) {
        this.queue = queue;
        this.handler = handler;
    }

    @Override
    public void run() {
        List<WithMillis<Message>> list;
        synchronized (queue) {
            list = new ArrayList<>(queue);
            queue.clear();
        }
        list.addAll(list);
        for (int i = 0; i < list.size(); i++) {
            android.os.Message msg = android.os.Message.obtain();
            WithMillis<Message> temp = list.get(i);
            msg.obj = new WithMillis<Message>(temp.value.copy(CipherUtil.encrypt(temp.value.plainText)), System.currentTimeMillis());
            msg.setTarget(handler);
            msg.sendToTarget();
        }
        synchronized (queue) {
            if (queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
