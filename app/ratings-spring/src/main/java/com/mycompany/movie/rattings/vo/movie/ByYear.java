package com.mycompany.movie.rattings.vo.movie;

import org.springframework.data.annotation.Id;

/**
 * Created by Raphael on 26/07/2017.
 */
public class ByYear {

    @Id
    private Integer year;

    private Integer movies;

    public Integer getMovies() {
        return movies;
    }

    public void setMovies(Integer movies) {
        this.movies = movies;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
