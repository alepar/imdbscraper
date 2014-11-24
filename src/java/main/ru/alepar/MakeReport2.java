package ru.alepar;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import ru.alepar.http.client.ning.NingAsyncHttpClient;
import ru.alepar.services.imdb.ImdbAsyncService;
import ru.alepar.services.imdb.ImdbDetails;
import ru.alepar.services.imdb.ImdbSearchResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeReport2 {

    public static void main(String[] args) throws Exception{
        try (
                NingAsyncHttpClient client = new NingAsyncHttpClient();
                BufferedReader reader = loadResourceAsString("ru/alepar/imdbmiss2.txt")
        ) {
            final ImdbAsyncService imdb = ImdbAsyncService.Factory.create(client);
            final List<ListenableFuture<?>> futures = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {

                final String[] split = line.split("\t");
                final String url = split[0];
                if("-".equals(url.trim())) {
                    continue;
                }

                final ListenableFuture<ImdbDetails> detailFuture = imdb.details(url);
                Futures.addCallback(detailFuture, new PrintOutResult(line));

                futures.add(detailFuture);
            }

            for (ListenableFuture<?> future : futures) {
                try {
                    future.get(); // let everyone finish
                } catch (Exception ignored) { }
            }
        }
    }

    private static class PrintOutResult implements FutureCallback<ImdbDetails> {

        private final String line;

        public PrintOutResult(String line) {
            this.line = line;
        }

        @Override
        public void onSuccess(ImdbDetails result) {
            System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                    result.getRating(),
                    result.getName(),
                    result.getYear(),
                    result.getGenres(),
                    result.getDirector(),
                    result.getWriter(),
                    result.getStars(),
                    result.getAwards(),
                    result.getDuration(),
                    result.getDescription(),
                    line));
        }

        @Override
        public void onFailure(Throwable t) {
            System.out.println(String.format("0.0\t%s\t%s", t.toString(), line));

        }
    }

    public static BufferedReader loadResourceAsString(String url) {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
        if (is == null) {
            throw new RuntimeException("resource not found: " + url);
        }

        return new BufferedReader(new InputStreamReader(is));
    }

}
