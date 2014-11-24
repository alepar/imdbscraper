package ru.alepar.services.imdb;

import org.junit.Test;
import ru.alepar.TestSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ExtractDetailsTest {

    private final String detailPage = TestSupport.loadResourceAsString("ru/alepar/services/imdb/detailPage.html");
    private final String detailPage2 = TestSupport.loadResourceAsString("ru/alepar/services/imdb/detailPage2.html");

    @Test
    public void detailsExtractedFine() throws Exception {
        final ImdbDetails details = new ExtractDetails().apply(detailPage);

        assertThat(details.getName(), equalTo("Kinsey"));
        assertThat(details.getYear(), equalTo(2004));

    }

    @Test
    public void detailsExtractedFineFromAnotherPage() throws Exception {
        final ImdbDetails details = new ExtractDetails().apply(detailPage2);

        assertThat(details.getName(), notNullValue());

    }

}