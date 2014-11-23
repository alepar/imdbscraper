package ru.alepar.http.client.ning;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import ru.alepar.http.client.AsyncHttpClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NingAsyncHttpClient implements AsyncHttpClient, AutoCloseable {

    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory());
    private final com.ning.http.client.AsyncHttpClient client = makeNingClient(executorService);

    private static com.ning.http.client.AsyncHttpClient makeNingClient(ExecutorService executorService) {
        final AsyncHttpClientConfig.Builder bc = new AsyncHttpClientConfig.Builder();
        bc.setUserAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36");
        bc.setAllowPoolingConnection(true);
        bc.setMaximumConnectionsPerHost(50);
        bc.setMaximumConnectionsTotal(5000);
        bc.setConnectionTimeoutInMs(2000);
        bc.setRequestTimeoutInMs(30000);
        bc.setFollowRedirects(true);
        bc.setCompressionEnabled(true);
        bc.setMaximumNumberOfRedirects(2);
        bc.setMaxRequestRetry(2);
        bc.setIOThreadMultiplier(1);
        bc.setExecutorService(executorService);
        return new com.ning.http.client.AsyncHttpClient(bc.build());
    }

    @Override
    public ListenableFuture<Response> doGet(String url) {
        try {
            return ningToGuava(client.prepareGet(url).execute());
        } catch (IOException e) {
            throw new RuntimeException("could not submit http get request", e);
        }
    }

    @Override
    public void close() {
        client.close();
    }

    public ListenableFuture<Response> ningToGuava(com.ning.http.client.ListenableFuture<Response> ning) {
        final SettableFuture<Response> guava = SettableFuture.create();
        ning.addListener(new PassThrough(guava, ning), executorService);
        return guava;
    }

    private static class ThreadFactory implements java.util.concurrent.ThreadFactory {
        private final ThreadGroup tg = new ThreadGroup("NingHttpClientPool");
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new Thread(tg, r, String.valueOf(count.getAndIncrement()));
        }
    }

    private static class PassThrough implements Runnable {
        private final SettableFuture<Response> guava;
        private final com.ning.http.client.ListenableFuture<Response> ning;

        public PassThrough(SettableFuture<Response> guava, com.ning.http.client.ListenableFuture<Response> ning) {
            this.guava = guava;
            this.ning = ning;
        }

        @Override
        public void run() {
            try {
                guava.set(ning.get());
            } catch (Exception e) {
                guava.setException(e);
            }
        }
    }
}