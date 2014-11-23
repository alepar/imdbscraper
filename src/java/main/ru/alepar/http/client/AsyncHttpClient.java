package ru.alepar.http.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.Response;

public interface AsyncHttpClient {
    ListenableFuture<Response> doGet(String url);
}