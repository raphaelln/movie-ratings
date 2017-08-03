package com.mycompany.movie.rattings.collection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by Raphael on 22/07/2017.
 */
@Document(collection = "movies")
public class Movie {

    @Id
    private String id;

    private String title;

    private Integer year;

    private List<String> genres;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }



    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genreList) {
        this.genres = genres;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
