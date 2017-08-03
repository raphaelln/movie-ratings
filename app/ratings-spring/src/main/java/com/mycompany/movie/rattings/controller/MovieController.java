package com.mycompany.movie.rattings.controller;

import com.mycompany.movie.rattings.vo.movie.BestMovie;
import com.mycompany.movie.rattings.vo.movie.ByRatingYear;
import com.mycompany.movie.rattings.vo.movie.ByDecade;
import com.mycompany.movie.rattings.vo.movie.ByYear;
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
 * Created by Raphael on 22/07/2017.
 */
@Controller()
@RequestMapping("movies")
@Profile({"!insight"})
public class MovieController {

    @Autowired
    private MongoTemplate template;

    @RequestMapping(value = "/best/{top}",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestMovie> bestTopMoviesRatings(@PathVariable(value="top", required=false) String topResults) {

        Aggregation aggregation = newAggregation(
                group("movieId").count().as("numRatings").avg("ratingValue").as("average"),
                match(Criteria.where("numRatings").gt(5)),
                lookup("movies","_id","_id", "m"),
                unwind("m"),
                project("m.title","average", "m.genres", "m.year", "m._id"),
                sort(Sort.Direction.DESC, "average", "title","year"),
                limit(StringUtils.isNotBlank(topResults) ? Integer.valueOf(topResults) : Long.MAX_VALUE )
        );

        AggregationResults<BestMovie> groupResults
                = template.aggregate(aggregation, Rating.class, BestMovie.class);
        List<BestMovie> result = groupResults.getMappedResults();

        return result;
    }

    @RequestMapping(value = "/best",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestMovie> bestMoviesRatings( ) {

        return bestTopMoviesRatings(StringUtils.EMPTY);
    }

    @RequestMapping(value = "/by/decade", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByDecade> moviesByDecade() {

        Aggregation aggregation = newAggregation(
                    project().andExpression("floor(year/10)").as("decade"),
                group("decade").count().as("movies"),
                sort(Sort.Direction.DESC, "movies")
        );

        AggregationResults<ByDecade> groupResults
                = template.aggregate(aggregation, "movies", ByDecade.class);

        List<ByDecade> result = groupResults.getMappedResults();

        return result;

    }

    @RequestMapping(value = "/by/year", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByYear> moviesByYear() {

        Aggregation aggregation = newAggregation(
                group("year").count().as("movies"),
                sort(Sort.Direction.DESC, "movies")
        );

        AggregationResults<ByYear> groupResults
                = template.aggregate(aggregation, "movies", ByYear.class);

        List<ByYear> result = groupResults.getMappedResults();

        return result;

    }

    @RequestMapping(value = "/year",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByRatingYear> averageByYear() {

        Aggregation aggregation = newAggregation(
                project("ratingValue").andExpression("year(ratingDate)").as("year"),
                group("year").avg("ratingValue").as("average")
        );

        AggregationResults<ByRatingYear> groupResults
                = template.aggregate(aggregation, Rating.class, ByRatingYear.class);
        List<ByRatingYear> result = groupResults.getMappedResults();

        return result;
    }
}
