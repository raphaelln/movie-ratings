package com.mycompany.movie.rattings.controller;

import com.mycompany.movie.rattings.vo.genres.BestGenre;
import com.mycompany.movie.rattings.vo.genres.ByMovie;
import com.mycompany.movie.rattings.vo.movie.BestMovie;
import com.mycompany.movie.rattings.vo.movie.ByDecade;
import com.mycompany.movie.rattings.vo.movie.ByRatingYear;
import com.mycompany.movie.rattings.vo.movie.ByYear;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("insights")
@Profile("insight")
public class InsightController {

    @Autowired
    private MongoTemplate template;

    @RequestMapping(value = "/movies/best/{top}",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestMovie> bestTopMoviesRatings(@PathVariable(value="top", required=false) String topResults) {
        return template.find(new Query().with(new Sort(Sort.Direction.DESC, "average")).limit(Integer.valueOf(topResults)), BestMovie.class, "bestMovies");
    }

    @RequestMapping(value = "/movies/best",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestMovie> bestTopMoviesRatings() {
        return template.find(new Query(), BestMovie.class, "bestMovies");
    }


    @RequestMapping(value = "/movies/by/decade", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByDecade> moviesByDecade() {

        return template.find(new Query(), ByDecade.class, "moviesByDecade");

    }

    @RequestMapping(value = "/movies/by/year", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByYear> moviesByYear() {

        return template.find(new Query(), ByYear.class, "moviesByYear");
    }


    @RequestMapping(value = "/movies/year",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByRatingYear> averageByYear() {

        return template.find(new Query(), ByRatingYear.class, "yearRatings");
    }


    @RequestMapping(value = "genres/by/movies", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ByMovie> genreByMovies() {

        return template.find(new Query(), ByMovie.class, "moviesByGenre");
    }

    @RequestMapping(value = "/genres/best", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestGenre> bestGenresRatings() {

        return template.find(new Query(), BestGenre.class, "bestGenres");
    }


    @RequestMapping(value = "genres/best/{top}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BestGenre> bestTopGenresRatings(@PathVariable(value="top", required=false) String topResults) {

        return template.find(new Query().limit(Integer.valueOf(topResults)), BestGenre.class, "bestGenres");
    }

}
