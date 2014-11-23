package ru.alepar.services.imdb;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.Response;
import ru.alepar.http.client.AsyncHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class AsyncHttpClientImdbAsyncService implements ImdbAsyncService {

    private final ExtractSearchResults extractSearchResults = new ExtractSearchResults();
    private final ExtractDetails extractDetails = new ExtractDetails();
    private final Function<Response, String> getResponseBody = new GetResponseBody();
    private final AsyncHttpClient client;

    public AsyncHttpClientImdbAsyncService(AsyncHttpClient client) {
        this.client = client;
    }

    @Override
    public ListenableFuture<List<ImdbSearchResult>> search(String query) {
        /*
         * if your query has non-english words, sometimes, when you're unlucky, you get empty results
         * this is a retry workaround for it
         */
        final SettableFuture<List<ImdbSearchResult>> retryFuture = SettableFuture.create();
        final AtomicInteger retries = new AtomicInteger();

        Futures.addCallback(searchInternal(query), new FutureCallback<List<ImdbSearchResult>>() {
            @Override
            public void onSuccess(List<ImdbSearchResult> result) {
                if (result.isEmpty()) {
                    retry(result);
                } else {
                    completed(result);
                }
            }

            private void completed(List<ImdbSearchResult> result) {
                retryFuture.set(result);
            }

            private void retry(List<ImdbSearchResult> result) {
                if (retries.incrementAndGet() > 3) {
                    completed(result);
                }
                else {
                    Futures.addCallback(searchInternal(query), this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                retry(Collections.emptyList());
            }
        });

        return retryFuture;
    }

    private ListenableFuture<List<ImdbSearchResult>> searchInternal(String query) {
        try {
            final String url = "http://www.imdb.com/find?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name()) + "&s=tt";
            return Futures.transform(
                    Futures.transform(
                            client.doGet(url), getResponseBody
                    ), extractSearchResults);
        } catch (Exception e) {
            throw new RuntimeException("failed to submit search request", e);
        }
    }

    @Override
    public ListenableFuture<ImdbDetails> details(String url) {
        try {
            url = "http://www.imdb.com" + url;
            return Futures.transform(
                    Futures.transform(
                            client.doGet(url), getResponseBody
                    ), extractDetails);
        } catch (Exception e) {
            throw new RuntimeException("failed to submit detail request", e);
        }
    }

    private static class GetResponseBody implements Function<Response, String> {

        @Override
        public String apply(Response input) {
            try {
                return input.getResponseBody();
            } catch (Exception e) {
                throw new RuntimeException("failed to get response body", e);
            }
        }
    }
}
