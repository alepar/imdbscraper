package ru.alepar.http.client.ning;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
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
        bc.setMaximumConnectionsPerHost(1);
        bc.setMaximumConnectionsTotal(1000);
        bc.setConnectionTimeoutInMs(30000);
        bc.setRequestTimeoutInMs(90000);
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
            final com.ning.http.client.AsyncHttpClient.BoundRequestBuilder builder = client.prepareGet(url);
            builder.addCookie(makeImdbCookie("session-id", "594-4939534-6166379"));
            builder.addCookie(makeImdbCookie("session-id-time", "1572619534"));
            builder.addCookie(makeImdbCookie("cs", "l3by83yK60rbJvGBjZqIKAl/A+3+SVT9zsFEdZ3KZ57dWjeOjun37r5JVP3JH9S93klUy70tg139CjA+yK4UXY5JVMtpSdz9/kliy+7BVP39KnR17Tonfs1aF+6dSlRdWF+Srs7fYurOSVS9XlkU3f5pVP3+SVS9vhkkzf"));
            builder.addCookie(makeImdbCookie("uu", "BCYqG4Tlm-btvw3aX1HZRWi2wn_qyVylInLC88qzwEE8Ius2aJK1zSiuzDfFPIBq4KlEbZV_f2gnly0BEkr4Nbyae2KKrOPiLbK15sHABPsH6bOjjwjlBQ7modGZbY5i6K3tpeX9hjqJakaBdfh1gZL5YRRibfepqv4vbzA61R8ZQB2R4KIyCbpSqhY3mYjSY1w-B4FERmqrRIuHna1IYoLH8RUKUY6lRv7dvcw0G8xW1QpkSRUj3z2886vhnIURfB93W0r7Cadkw-CnZcQHk-P7k5JtKJKIQ2XuNQlVwhGjhVj6qzGqtHQRBxdlbAj4TsWR"));
            builder.addCookie(makeImdbCookie("as", "%7B%22h%22%3A%7B%22t%22%3A%5B0%2C0%5D%2C%22tr%22%3A%5B0%2C0%5D%2C%22in%22%3A%5B0%2C0%5D%2C%22ib%22%3A%5B0%2C0%5D%7D%2C%22n%22%3A%7B%22t%22%3A%5B0%2C0%5D%2C%22tr%22%3A%5B0%2C0%5D%2C%22in%22%3A%5B0%2C0%5D%2C%22ib%22%3A%5B0%2C0%5D%7D%7D"));

            return ningToGuava(builder.execute());
        } catch (IOException e) {
            throw new RuntimeException("could not submit http get request", e);
        }
    }

    private static Cookie makeImdbCookie(String name, String value) {
        return new Cookie(name, value, value, "www.imdb.com", null, Long.MAX_VALUE, 9999, true, false);
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