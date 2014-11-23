package ru.alepar.services.imdb;

import com.google.common.base.Function;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

class ExtractDetails implements Function<String, ImdbDetails> {

    @Override
    public ImdbDetails apply(String input) {
        try {
            final HtmlCleaner cleaner = new HtmlCleaner();
            final TagNode htmlNode = cleaner.clean(input);

            final String name = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/h1/span[1]/text()")[0].toString().trim();
            final String duration = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@class='infobar']/time/text()")[0].toString().trim();

            final Object[] genreTexts = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@class='infobar']/a/span[@itemprop='genre']/text()");
            final StringBuilder genres = new StringBuilder();
            for (Object genre : genreTexts) {
                genres.append(genre.toString().trim()).append(", ");
            }
            genres.setLength(genres.length()-2);

            final int year = Integer.valueOf(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/h1/span[@class='nobr']/a/text()")[0].toString().trim());
            final String rating = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[3]/div[3]/strong/span[@itemprop='ratingValue']/text()")[0].toString().trim();
            final String description = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/p[@itemprop='description']/text()")[0].toString().trim();
            final String director = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='director']/a/span[@itemprop='name']/text()")[0].toString().trim();
            final String writer = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='creator']/a/span[@itemprop='name']/text()")[0].toString().trim();

            final Object[] starTexts = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='actors']/a/span/text()");
            final StringBuilder stars = new StringBuilder();
            for (Object star : starTexts) {
                stars.append(star.toString().trim()).append(", ");
            }
            stars.setLength(stars.length()-2);

            final Object[] awardTexts = htmlNode.evaluateXPath("//*[@id=\"titleAwardsRanks\"]/span/text()");
            final StringBuilder awards = new StringBuilder();
            for (Object award : awardTexts) {
                if (!award.toString().contains("See more")) {
                    awards.append(award.toString().trim()).append(" ");
                }
            }
            awards.setLength(awards.length()-1);

            return new SimpleImdbDetails(
                    name, duration, year, rating, description, director, writer, genres.toString(), stars.toString(), awards.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to extract search results from imdb response", e);
        }
    }
}
