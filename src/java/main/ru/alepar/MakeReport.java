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

public class MakeReport {

    public static final Pattern cleanupPattern = Pattern.compile("([^;]+).*(\\d{4}).*");

    public static void main(String[] args) throws Exception{
        try (
                NingAsyncHttpClient client = new NingAsyncHttpClient();
                BufferedReader reader = loadResourceAsString("ru/alepar/psycho.txt")
        ) {
            final ImdbAsyncService imdb = ImdbAsyncService.Factory.create(client);
            final List<ListenableFuture<?>> futures = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                final Matcher matcher = cleanupPattern.matcher(line);

                final String query;
                String year = null;
                if (!matcher.find()) {
                    query = line;
                } else {
                    year = (matcher.group(2) == null ? "" : matcher.group(2));
                    query = matcher.group(1) + " " + year;
                }

                final Integer expectedYear;
                if (year != null && !year.isEmpty()) {
                    expectedYear = Integer.valueOf(year);
                } else {
                    expectedYear = null;
                }

                final ListenableFuture<List<ImdbSearchResult>> searchFuture = imdb.search(query);
                final ListenableFuture<ImdbDetails> detailFuture = Futures.transform(searchFuture, new FindMatchAndQueryDetails(imdb, expectedYear));
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

    private static class FindMatchAndQueryDetails implements AsyncFunction<List<ImdbSearchResult>, ImdbDetails> {
        private final ImdbAsyncService imdb;
        private final Integer expectedYear;

        public FindMatchAndQueryDetails(ImdbAsyncService imdb, Integer expectedYear) {
            this.imdb = imdb;
            this.expectedYear = expectedYear;
        }

        @Override
        public ListenableFuture<ImdbDetails> apply(List<ImdbSearchResult> input) {
            if (input == null || input.isEmpty()) {
                throw new RuntimeException("search returned no results");
            }

            final ImdbSearchResult searchResult = input.get(0);
            if(expectedYear != null && Math.abs(expectedYear - searchResult.getYear()) > 2) {
                throw new RuntimeException("year is too far off");
            }

            return imdb.details(searchResult.getUrl());
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
            System.out.println(String.format("0.0\t%s\t%s", t.getMessage(), line));

        }
    }

    private static BufferedReader loadResourceAsString(String url) {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
        if (is == null) {
            throw new RuntimeException("resource not found: " + url);
        }

        return new BufferedReader(new InputStreamReader(is));
    }

}
