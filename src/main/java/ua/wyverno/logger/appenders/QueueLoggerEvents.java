package ua.wyverno.logger.appenders;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueLoggerEvents implements Closeable {

    private static QueueLoggerEvents instance = null;
    private final BlockingQueue<String> queueLogs = new LinkedBlockingQueue<>();

    private volatile boolean shouldTerminate = false;

    private QueueLoggerEvents() {
        Thread thread = new Thread(() -> {
            while (!this.shouldTerminate || !this.queueLogs.isEmpty()) {

                while (!this.queueLogs.isEmpty()) { // Виводимо всі логи
                    String log = this.queueLogs.poll();
                    System.out.print(log);
                }

                synchronized (this.queueLogs) { // Заморожуємо потік
                    while (this.queueLogs.isEmpty() && !this.shouldTerminate) {
                        try {
                            this.queueLogs.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }, "QueueLoggerEvents-Thread");

        thread.start();
    }

    public static synchronized QueueLoggerEvents getInstance() {
        if (instance == null) {
            instance = new QueueLoggerEvents();
        }
        return instance;
    }

    public static synchronized boolean hasInstance() {
        return instance != null;
    }

    protected synchronized void addLog(String log) {
        this.queueLogs.offer(log);
        synchronized (this.queueLogs) {
            this.queueLogs.notifyAll(); // Розморожуємо всі потоки
        }
    }

    @Override
    public void close() {
        synchronized (this.queueLogs) {
            this.shouldTerminate = true;
            this.queueLogs.notifyAll();
        }
    }
}