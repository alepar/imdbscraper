package ru.alepar.services.imdb;

class SimpleImdbDetails implements ImdbDetails {
    private final String name;
    private final String duration;
    private final int year;
    private final String rating;
    private final String description;
    private final String director;
    private final String writer;
    private final String genres;
    private final String stars;
    private final String awards;

    public SimpleImdbDetails(String name, String duration, int year, String rating, String description, String director, String writer, String genres, String stars, String awards) {
        this.name = name;
        this.duration = duration;
        this.year = year;
        this.rating = rating;
        this.description = description;
        this.director = director;
        this.writer = writer;
        this.genres = genres;
        this.stars = stars;
        this.awards = awards;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDuration() {
        return duration;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public String getRating() {
        return rating;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDirector() {
        return director;
    }

    @Override
    public String getWriter() {
        return writer;
    }

    @Override
    public String getGenres() {
        return genres;
    }

    @Override
    public String getStars() {
        return stars;
    }

    @Override
    public String getAwards() {
        return awards;
    }
}
