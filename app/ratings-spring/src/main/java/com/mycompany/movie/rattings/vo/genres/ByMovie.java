package com.mycompany.movie.rattings.vo.genres;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by Raphael on 26/07/2017.
 */
public class ByMovie {

    @Id
    @Field("genres")
    private String genre;

    private Integer movies;

    public Integer getMovies() {
        return movies;
    }

    public void setMovies(Integer movies) {
        this.movies = movies;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
