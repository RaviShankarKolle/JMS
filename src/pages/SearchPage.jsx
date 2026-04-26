import MovieCard from "../components/MovieCard";
import NoResults from "./NoResults";

const SearchPage = ({
  searchTerm,
  movies,
  openMovieDetails,
  canLoadMore,
  onLoadMore,
}) => {
  const showNoResults = searchTerm.trim() !== "" && movies.length === 0;

  if (showNoResults) {
    return <NoResults searchTerm={searchTerm} />;
  }

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

export default SearchPage;
