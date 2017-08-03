package com.mycompany.movie.rattings.collection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by Raphael on 22/07/2017.
 */
@Document(collection = "ratings")
public class Rating {

    @Id
    private String id;

    private Integer ratingValue;

    private String feeling;

    private Date ratingDate;

    @Indexed(collection = "movies")
    private Integer movieId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public Date getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(Date ratingDate) {
        this.ratingDate = ratingDate;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovie(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }
}
