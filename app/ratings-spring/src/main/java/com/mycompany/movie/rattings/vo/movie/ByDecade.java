package com.mycompany.movie.rattings.vo.movie;

import org.springframework.data.annotation.Id;

/**
 * Created by Raphael on 26/07/2017.
 */
public class ByDecade {

    @Id
    private Integer decade;

    private Integer movies;

    public Integer getMovies() {
        return movies;
    }

    public void setMovies(Integer movies) {
        this.movies = movies;
    }

    public Integer getDecade() {
        return decade;
    }

    public void setDecade(Integer decade) {
        this.decade = decade;
    }
}
