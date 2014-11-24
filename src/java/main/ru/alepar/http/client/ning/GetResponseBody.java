package ru.alepar.http.client.ning;

import com.google.common.base.Function;
import com.ning.http.client.Response;

public class GetResponseBody implements Function<Response, String> {

    @Override
    public String apply(Response input) {
        try {
            return input.getResponseBody();
        } catch (Exception e) {
            throw new RuntimeException("failed to get response body", e);
        }
    }
}
