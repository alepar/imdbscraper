package ru.alepar.services.imdb;

import com.google.common.util.concurrent.ListenableFuture;
import ru.alepar.http.client.AsyncHttpClient;

import java.util.List;

public interface ImdbAsyncService {

    ListenableFuture<List<ImdbSearchResult>> search(String query);
    ListenableFuture<ImdbDetails> details(String url);


    public static class Factory {
        public static ImdbAsyncService create(AsyncHttpClient client) {
            return new AsyncHttpClientImdbAsyncService(client);
        }
    }

}
