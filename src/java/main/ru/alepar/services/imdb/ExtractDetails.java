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

            final String name = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/h1/span[1]/text()"));
            final String duration = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@class='infobar']/time/text()"));

            final Object[] genreTexts = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@class='infobar']/a/span[@itemprop='genre']/text()");
            final StringBuilder genres = new StringBuilder();
            for (Object genre : genreTexts) {
                genres.append(genre.toString().trim()).append(", ");
            }
            if (genres.length() >= 2) {
                genres.setLength(genres.length()-2);
            }

            final int year = Integer.valueOf(safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/h1/span[@class='nobr']/text()")).replaceAll(".*(\\d{4}).*", "$1"));
            final String rating = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div/div/strong/span[@itemprop='ratingValue']/text()"));

            String description = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/p[@itemprop='description']/text()"));
            if(description != null) {
                description = description.replaceAll("(.*?)(See full s.*)", "$1").trim();
            }

            final String director = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='director']/a/span[@itemprop='name']/text()"));
            final String writer = safeToString(htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='creator']/a/span[@itemprop='name']/text()"));

            final Object[] starTexts = htmlNode.evaluateXPath("//*[@id=\"overview-top\"]/div[@itemprop='actors']/a/span/text()");
            final StringBuilder stars = new StringBuilder();
            for (Object star : starTexts) {
                stars.append(star.toString().trim()).append(", ");
            }
            if (stars.length() >= 2) {
                stars.setLength(stars.length()-2);
            }

            final Object[] awardTexts = htmlNode.evaluateXPath("//*[@id=\"titleAwardsRanks\"]/span/text()");
            final StringBuilder awards = new StringBuilder();
            for (Object award : awardTexts) {
                if (!award.toString().contains("See more")) {
                    awards.append(award.toString().trim()).append(" ");
                }
            }
            if (awards.length() >= 1) {
                awards.setLength(awards.length()-1);
            }

            return new SimpleImdbDetails(
                    name, duration, year, rating, description, director, writer, genres.toString(), stars.toString(), awards.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to extract detailpage results from imdb response", e);
        }
    }

    private static String safeToString(Object[] objects) {
        if (objects.length == 0) {
            return null;
        }

        return objects[0].toString().trim();
    }
}
