import MovieCard from "../components/MovieCard";

const HomePage = ({ movies, openMovieDetails, canLoadMore, onLoadMore }) => {
  return (
    <>
      <div className="movie-list">
        {movies.map((movie) => (
          <MovieCard key={movie.id} movie={movie} openModal={openMovieDetails} />
        ))}
      </div>

      {canLoadMore ? (
        <div className="load-more-container">
          <button onClick={onLoadMore} className="load-more-btn">
            Show More
          </button>
        </div>
      ) : null}
    </>
  );
};

export default HomePage;
