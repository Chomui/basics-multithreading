package com.artemchep.basics_multithreading;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artemchep.basics_multithreading.cipher.CipherUtil;
import com.artemchep.basics_multithreading.domain.Message;
import com.artemchep.basics_multithreading.domain.WithMillis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {

    private List<WithMillis<Message>> mList = new ArrayList<>();

    private MessageAdapter mAdapter = new MessageAdapter(mList);

    private Handler handler;
    private boolean isRunning = true;

    private Queue<WithMillis<Message>> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        queue = new LinkedList<>();

        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                WithMillis<Message> withMillis = (WithMillis<Message>) msg.obj;
                update(new WithMillis<Message>(withMillis.value.copy(withMillis.value.cipherText), (System.currentTimeMillis() - withMillis.elapsedMillis)));
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning) {
                    List<WithMillis<Message>> list;
                    synchronized (queue) {
                        list = new ArrayList<>(queue);
                        queue.clear();
                    }
                    for(int i = 0; i < list.size(); i++) {
                        android.os.Message msg = android.os.Message.obtain();
                        WithMillis<Message> temp = list.get(i);
                        msg.obj = new WithMillis<Message>(temp.value.copy(CipherUtil.encrypt(temp.value.plainText)), temp.elapsedMillis);
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
        }).start();
        //showWelcomeDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
                .setMessage("What are you going to need for this task: Thread, Handler.\n" +
                        "\n" +
                        "1. The main thread should never be blocked.\n" +
                        "2. Messages should be processed sequentially.\n" +
                        "3. The elapsed time SHOULD include the time message spent in the queue.")
                .show();
    }

    public void onPushBtnClick(View view) {
        Message message = Message.generate();
        insert(new WithMillis<>(message, System.currentTimeMillis()));
    }

    @UiThread
    public void insert(final WithMillis<Message> message) {
        mList.add(message);
        mAdapter.notifyItemInserted(mList.size() - 1);

        synchronized (queue) {
            queue.add(message);
            queue.notifyAll();
        }

        update(message);

        // TODO: Start processing the message (please use CipherUtil#encrypt(...)) here.
        //       After it has been processed, send it to the #update(...) method.

        // How it should look for the end user? Uncomment if you want to see. Please note that
        // you should not use poor decor view to send messages to UI thread.
        /*getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Message messageNew = message.value.copy("sample :)");
                final WithMillis<Message> messageNewWithMillis = new WithMillis<>(messageNew, CipherUtil.WORK_MILLIS);
                update(messageNewWithMillis);
            }
        }, CipherUtil.WORK_MILLIS);*/
    }

    @UiThread
    public void update(final WithMillis<Message> message) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).value.key.equals(message.value.key)) {
                mList.set(i, message);
                mAdapter.notifyItemChanged(i);
                return;
            }
        }

        throw new IllegalStateException();
    }
}
