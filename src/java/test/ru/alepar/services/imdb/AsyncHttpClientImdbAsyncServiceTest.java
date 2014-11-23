package ru.alepar.services.imdb;

import org.junit.Test;
import ru.alepar.http.client.ning.NingAsyncHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class AsyncHttpClientImdbAsyncServiceTest {

    private final ImdbAsyncService service = ImdbAsyncService.Factory.create(new NingAsyncHttpClient());

    @Test
    public void searchReturnsResults() throws Exception {
        final List<ImdbSearchResult> results = service.search("Kinsey").get(5, TimeUnit.SECONDS);

        assertThat(results.size(), greaterThan(1));
        assertThat(results.get(0).getName(), equalTo("Kinsey"));
    }

    @Test
    public void workaroundForEmptySearchResultsWorks() throws Exception {
        for (int i=0; i<10; i++) {
            final List<ImdbSearchResult> results = service.search("Агнозия / Agnosia 2010").get(5, TimeUnit.SECONDS);
            assertThat(results.size(), greaterThan(1));
            assertThat(results.get(0).getName(), equalTo("Agnosia"));
        }
    }

    @Test
    public void detailsReturnsResults() throws Exception {
        final ImdbDetails imdbDetails = service.details("/title/tt0362269").get(5, TimeUnit.SECONDS);

        assertThat(imdbDetails.getName(), equalTo("Kinsey"));
        assertThat(imdbDetails.getYear(), equalTo(2004));
        assertThat(imdbDetails.getDirector(), equalTo("Bill Condon"));
        assertThat(Double.valueOf(imdbDetails.getRating()), greaterThan(0.0));
    }
}