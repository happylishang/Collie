package com.snail.collie.fps;

import java.util.concurrent.LinkedBlockingQueue;

public class FpsThread extends Thread {

    private LinkedBlockingQueue<Runnable> mLinkedBlockingQueue;

    public FpsThread(LinkedBlockingQueue<Runnable> queue) {
        super("fps_track");
        mLinkedBlockingQueue = queue;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                Runnable runnable = mLinkedBlockingQueue.take();
                runnable.run();
            } catch (Exception ignored) {
            }
        }

    }
}
