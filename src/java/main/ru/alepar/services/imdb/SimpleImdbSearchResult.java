package ru.alepar.services.imdb;

class SimpleImdbSearchResult implements ImdbSearchResult {
    private final String name;
    private final Integer year;
    private final String type;
    private final String url;

    public SimpleImdbSearchResult(String name, Integer year, String type, String url) {
        this.name = name;
        this.year = year;
        this.type = type;
        this.url = url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getYear() {
        return year;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
