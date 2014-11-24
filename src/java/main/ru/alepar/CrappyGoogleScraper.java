package ru.alepar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.alepar.MakeReport.loadResourceAsString;

public class CrappyGoogleScraper {

    public static final Pattern cleanupPattern = Pattern.compile("([^;]+).*(\\d{4}).*");

    public static void main (String args[]) throws Exception {
        try (BufferedReader reader = loadResourceAsString("ru/alepar/imdbmiss.txt")){
            String line;
            while ((line = reader.readLine()) != null) {
                final Matcher matcher = cleanupPattern.matcher(line);

                String query;
                if (!matcher.find()) {
                    query = line;
                } else {
                    final String year = (matcher.group(2) == null ? "" : matcher.group(2));
                    query = matcher.group(1).replaceAll("[()]", "") + " (" + year + ")";
                }

                query += " site:imdb.com";

                try{
                    final Document doc = Jsoup.connect("https://www.google.com/search?as_q=&as_epq=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name()) + "&as_oq=&as_eq=&as_nlo=&as_nhi=&lr=lang_en&cr=countryUS&as_qdr=all&as_sitesearch=&as_occt=any&safe=images&tbs=&as_filetype=&as_rights=").userAgent("Mozilla").ignoreHttpErrors(true).timeout(0).get();
                    final Elements links = doc.select("li[class=g]");

                    if (links.size() > 0) {
                        final Element link = links.first();

                        final String url = link.select("div[class=kv]").select("cite").get(0).childNodes().get(0).toString();

                        System.out.println(url + "\t" + line);

                        Thread.sleep(60000);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
