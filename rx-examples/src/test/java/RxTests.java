import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import util.Color;

import java.io.FileNotFoundException;

import static util.ColorUtil.print;
import static util.ColorUtil.printThread;

public class RxTests {

    private static final String MOVIES1_DB = "movies1";

    private static final String MOVIES2_DB = "movies2";

    /**
     * Example 1: Creating and subscribing observable from iterable.
     */
    @Test
    public void loadMoviesAsList() throws FileNotFoundException {
        var movieReader = new MovieReader();

        movieReader.getMoviesFromList(MOVIES1_DB)
                .subscribe(movie -> print(movie, Color.GREEN));
    }

    /**
     * Example 2: Creating and subscribing observable from custom emitter.
     */
    @Test
    public void loadMoviesAsStream() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .subscribe(movie -> print(movie, Color.GREEN));
    }

    /**
     * Example 3: Handling errors.
     */
    @Test
    public void loadMoviesAsStreamAndHandleError() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream("nonexisting DB")
                .subscribe(movie -> print(movie, Color.GREEN),
                        error -> print("Nie pykło: " + error, Color.MAGENTA));
    }

    /**
     * Example 4: Signaling end of a stream.
     */
    @Test
    public void loadMoviesAsStreamAndFinishWithMessage() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .take(10)
                .subscribe(movie -> print(movie, Color.GREEN),
                        error -> print("Nie pykło: " + error, Color.MAGENTA),
                        () -> print("The end!", Color.BLUE));
    }

    /**
     * Example 5: Filtering stream data.
     */
    @Test
    public void displayLongMovies() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .filter(movie -> movie.getLength() > 150)
                .subscribe(movie -> print(movie, Color.GREEN));
    }

    /**
     * Example 6: Transforming stream data.
     */
    @Test
    public void displaySortedMoviesTitles() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .map(Movie::getDescription)
//                .take(10) // takes first 10 elements BEFORE sorting them
                .sorted() // caches all elements in order to sort them later
                .take(10) // takes first 10 elements AFTER they are sorted
                .subscribe(description -> print(description, Color.GREEN));
    }

    /**
     * Example 7: Monads are like burritos.
     */
    @Test
    public void displayActorsForMovies() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .flatMap(movie -> Observable.fromIterable(movieReader.readActors(movie)))
                .distinct()
                .sorted()
                .subscribe(actors -> print(actors, Color.GREEN));
    }

    /**
     * Example 8: Combining observables.
     */
    @Test
    public void loadMoviesFromManySources() {
        var movieReader = new MovieReader();

        Observable<Movie> movies1 = movieReader.getMoviesAsStream(MOVIES1_DB)
                .doOnNext(movie -> print(movie, Color.RED));

        Observable<Movie> movies2 = movieReader.getMoviesAsStream(MOVIES2_DB)
                .doOnNext(movie -> print(movie, Color.BLUE));

        Observable.merge(movies1, movies2)
                .subscribe(movie -> print("RECEIVED: " + movie, Color.GREEN));
    }

    /**
     * Example 9: Playing with threads (subscribeOn).
     */
    @Test
    public void loadMoviesInBackground() throws InterruptedException {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .subscribeOn(Schedulers.newThread())
                .doOnNext(movie -> printThread(movie.getIndex(), Color.BLUE))
                .subscribe(movie -> printThread(movie.getIndex(), Color.GREEN));
        print("The end", Color.RED);
        Thread.sleep(10000);
    }

    /**
     * Example 10: Playing with threads (observeOn).
     */
    @Test
    public void switchThreadsDuringMoviesProcessing() {

    }

    /**
     * Example 11: Combining parallel streams.
     */
    @Test
    public void loadMoviesFromManySourcesParallel() throws InterruptedException {
        // Static merge solution
        var movieReader = new MovieReader();

//        Observable<Movie> movies1 = movieReader.getMoviesAsStream(MOVIES1_DB)
//                .subscribeOn(Schedulers.io())
//                .doOnNext(movie -> print(movie, Color.RED));
//
//        Observable<Movie> movies2 = movieReader.getMoviesAsStream(MOVIES2_DB)
//                .subscribeOn(Schedulers.io())
//                .doOnNext(movie -> print(movie, Color.BLUE));
//
////        Observable.merge(movies1, movies2)
//        Observable.concat(movies1, movies2)
//                .subscribe(movie -> print("RECEIVED: " + movie, Color.GREEN));
//
//        Thread.sleep(10000);


        // FlatMap solution:
        final MovieDescriptor movie1Descriptor = new MovieDescriptor(MOVIES1_DB, Color.GREEN);
        final MovieDescriptor movie2Descriptor = new MovieDescriptor(MOVIES2_DB, Color.BLUE);

        Observable.just(movie1Descriptor, movie2Descriptor)
//                .map(db -> movieReader.getMoviesAsStream(db)) // Observable<Observable<Movie>>
                .concatMap(db -> movieReader.getMoviesAsStream(db.movieDbFilename)
                        .doOnNext(movie -> print(movie, db.debugColor))
                        .subscribeOn(Schedulers.io())) // every "pip" on different thread
                .subscribe(movie -> print("RECEIVED: " + movie, Color.RED));

        Thread.sleep(10000);

    }

    /**
     * Example 12: Zip operator.
     */
    @Test
    public void loadMoviesWithDelay() {

    }

    /**
     * Example 13: Backpressure.
     */
    @Test
    public void trackMoviesLoadingWithBackpressure() {
        var movieReader = new MovieReader();

        movieReader.getMoviesAsStream(MOVIES1_DB)
                .doOnNext(movie -> print(movie, Color.RED))
                .doOnNext(movie -> Thread.sleep(10))
                .subscribeOn(Schedulers.newThread())
                .toFlowable(BackpressureStrategy.LATEST) // comment line to see the problem
                .observeOn(Schedulers.io(), true, 1)
                .doOnNext(this::displayProgress)
                .blockingSubscribe();
    }

    /**
     * Example 14: Cold and hot observables.
     */
    @Test
    public void oneMovieStreamManyDifferentSubscribers() {

    }

    /**
     * Example 15: Caching observables (hot-cold hybrid).
     */
    @Test
    public void cacheMoviesInfo() {

    }

    private void displayProgress(Movie movie) throws InterruptedException {
        print((movie.getIndex() / 500.0 * 100) + "%", Color.GREEN);
        Thread.sleep(50);
    }

    private class MovieDescriptor {
        private final String movieDbFilename;

        private final Color debugColor;

        private MovieDescriptor(String movieDbFilename, Color debugColor) {
            this.movieDbFilename = movieDbFilename;
            this.debugColor = debugColor;
        }

        public Color getDebugColor() {
            return debugColor;
        }

        public String getMovieDbFilename() {
            return movieDbFilename;
        }
    }
}
