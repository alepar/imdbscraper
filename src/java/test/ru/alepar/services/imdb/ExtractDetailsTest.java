package ru.alepar.services.imdb;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExtractDetailsTest {

    private final String detailPage = TestSupport.loadResourceAsString("ru/alepar/services/imdb/detailPage.html");

    @Test
    public void searchResultsExtractedFine() throws Exception {
        final ImdbDetails details = new ExtractDetails().apply(detailPage);

        assertThat(details.getName(), equalTo("Kinsey"));

    }

}