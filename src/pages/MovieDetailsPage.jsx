import MovieModal from "../components/MovieModal";

const MovieDetailsPage = ({ movie, onClose }) => {
  if (!movie) {
    return null;
  }

  return <MovieModal movie={movie} closeModal={onClose} />;
};

export default MovieDetailsPage;
