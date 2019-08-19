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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity {

    private List<WithMillis<Message>> mList = new ArrayList<>();

    private MessageAdapter mAdapter = new MessageAdapter(mList);

    private CipherHandler handler;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private SimpleThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        handler = new CipherHandler();
        thread = new SimpleThread(handler);
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        thread.finish();
    }

    public void onPushBtnClick(View view) {
        Message message = Message.generate();
        insert(new WithMillis<>(message, System.currentTimeMillis()));
    }

    @UiThread
    public void insert(final WithMillis<Message> message) {
        mList.add(message);
        mAdapter.notifyItemInserted(mList.size() - 1);

        Future<WithMillis<Message>> future = executorService.submit(new Callable<WithMillis<Message>>() {
            @Override
            public WithMillis<Message> call() throws Exception {
                return new WithMillis<Message>(message.value.copy(CipherUtil.encrypt(message.value.plainText)), System.currentTimeMillis() - message.elapsedMillis);
            }
        });

        thread.add(future);

        update(message);
    }

    class CipherHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            update((WithMillis<Message>) msg.obj);
        }
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
