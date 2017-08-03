package com.mycompany.movie.rattings.controller;

import com.mycompany.movie.rattings.vo.genres.ByMovie;
import com.mycompany.movie.rattings.vo.genres.BestGenre;
import com.mycompany.movie.rattings.collection.Rating;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Created by Raphael on 29/07/2017.
 */
@Controller
@RequestMapping("genres")
@Profile({"!insight"})
public class GenreController {

    @Autowired
    private MongoTemplate template;


    @RequestMapping(value = "by/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByMovie> genreByMovies() {

        Aggregation aggregation = newAggregation(
                unwind("genres"),
                group( "genres").count().as("movies"),
                sort(Sort.Direction.DESC, "movies")
        );

        AggregationResults<ByMovie> groupResults
                = template.aggregate(aggregation, "movies", ByMovie.class);

        List<ByMovie> result = groupResults.getMappedResults();
        return result;
    }

    @RequestMapping(value = "/best", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestGenre> bestGenresRatings() {
        return bestTopGenresRatings(StringUtils.EMPTY);
    }


    @RequestMapping(value = "/best/{top}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestGenre> bestTopGenresRatings(@PathVariable(value="top", required=false) String topResults) {

        Aggregation aggregation = newAggregation(
                project("movieId", "ratingValue"),
                group("movieId").count().as("numMovies").sum("ratingValue").as("movieRatings"),
                lookup("movies","_id","_id", "movies"),
                unwind("movies", true),
                unwind("movies.genres", true),
                project("numMovies", "movies.genres","movieRatings"),
                match(Criteria.where("genres").ne("")),
                group("genres").sum("numMovies").as("totalMovies").sum("movieRatings").as("sumGenRatings"),
                project("genres").andExpression("sumGenRatings / totalMovies").as("average"),
                limit(StringUtils.isNotBlank(topResults) ? Integer.valueOf(topResults) : Long.MAX_VALUE),
                sort(Sort.Direction.DESC, "average")

        );


        AggregationResults<BestGenre> groupResults
                = template.aggregate(aggregation, Rating.class, BestGenre.class);
        List<BestGenre> result = groupResults.getMappedResults();

        return result;
    }
}
