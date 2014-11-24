package ru.alepar.services.imdb;

import org.junit.Test;
import ru.alepar.TestSupport;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ExtractSearchResultsTest {

    private final String searchPage = TestSupport.loadResourceAsString("ru/alepar/services/imdb/searchPage.html");

    @Test
    public void searchResultsExtractedFine() throws Exception {
        final List<ImdbSearchResult> results = new ExtractSearchResults().apply(searchPage);

        assertThat(results.size(), greaterThan(0));

        final ImdbSearchResult first = results.get(0);
        assertThat(first.getName(), equalTo("Kinsey"));
        assertThat(first.getYear(), equalTo(2004));
        assertThat(first.getType(), equalTo(null));

        final ImdbSearchResult second = results.get(1);
        assertThat(second.getName(), equalTo("Kinsey"));
        assertThat(second.getYear(), equalTo(1990));
        assertThat(second.getType(), equalTo("TV Series"));
    }

}