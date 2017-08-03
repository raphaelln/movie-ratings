package com.mycompany.movie.rattings.vo.genres;

import org.springframework.data.annotation.Id;

/**
 * Created by Raphael on 28/07/2017.
 */
public class BestGenre {

    @Id
    private String genre;

    private Double average;

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
