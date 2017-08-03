package com.mycompany.movie.rattings.vo.movie;

import org.springframework.data.annotation.Id;

/**
 * Created by Raphael on 28/07/2017.
 */
public class ByRatingYear {

    @Id
    private Integer year;

    private Double average;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }
}
