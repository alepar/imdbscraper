package ru.alepar.services.imdb;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.Response;
import ru.alepar.http.client.AsyncHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
