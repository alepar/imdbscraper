package ru.alepar.services.imdb;

import com.google.common.base.Function;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExtractSearchResults implements Function<String, List<ImdbSearchResult>> {

    private static final String SEARCH_RESULTS_PATH = "/body/div[@id='wrapper']/div[@id='root']/div[@id='pagecontent']/div[@id='content-2-wide']/div[@id='main']/div[@class='article']/div[@class='findSection']/table[@class='findList']/tbody/tr/td[@class='result_text']";
    private static final Pattern addTextPattern = Pattern.compile("\\((\\d+)\\)\\s*(\\(([^)]+)\\))?");

    @Override
    public List<ImdbSearchResult> apply(String input) {
        try {
            final HtmlCleaner cleaner = new HtmlCleaner();
            final TagNode htmlNode = cleaner.clean(input);

            final Object[] resultNodes = htmlNode.evaluateXPath(SEARCH_RESULTS_PATH);

            final List<ImdbSearchResult> results = new ArrayList<>(resultNodes.length);
            for (Object node : resultNodes) {
                final TagNode td = (TagNode) node;
                final String name = td.evaluateXPath("a/text()")[0].toString();
                final String url = (String) td.evaluateXPath("a/@href")[0];
                final String addText = (td.getAllChildren().get(2).toString());

                Integer year = null;
                String type = null;
                final Matcher matcher = addTextPattern.matcher(addText);
                if(matcher.find()) {
                    year = Integer.valueOf(matcher.group(1));
                    type = matcher.group(3);
                }

                results.add(new SimpleImdbSearchResult(name, year, type, url));
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("failed to extract search results from imdb response", e);
        }
    }

}
