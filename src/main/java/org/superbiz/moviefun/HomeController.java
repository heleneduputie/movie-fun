package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private PlatformTransactionManager moviePlatformTransactionManager;
    private PlatformTransactionManager albumPlatformTransactionManager;

    private final TransactionTemplate movieTransactionTemplate;
    private final TransactionTemplate albumTransactionTemplate;

    public HomeController(MoviesBean moviesBean,
                          AlbumsBean albumsBean,
                          MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures,
                          @Qualifier("moviePlatformTransactionManager") PlatformTransactionManager moviePlatformTransactionManager,
                          @Qualifier("albumPlatformTransactionManager") PlatformTransactionManager albumPlatformTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.moviePlatformTransactionManager = moviePlatformTransactionManager;
        this.albumPlatformTransactionManager = albumPlatformTransactionManager;
        this.movieTransactionTemplate = new TransactionTemplate(moviePlatformTransactionManager);
        this.albumTransactionTemplate = new TransactionTemplate(albumPlatformTransactionManager);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        for (Movie movie : movieFixtures.load()) {
            movieTransactionTemplate.execute(new TransactionCallback() {
                // the code in this method executes in a transactional context
                public Object doInTransaction(TransactionStatus status) {
                    moviesBean.addMovie(movie);
                    return null;
                }
            });

        }

        for (Album album : albumFixtures.load()) {
            albumTransactionTemplate.execute(new TransactionCallback() {
                // the code in this method executes in a transactional context
                public Object doInTransaction(TransactionStatus status) {
                    albumsBean.addAlbum(album);
                    return null;
                }
            });
        }

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
