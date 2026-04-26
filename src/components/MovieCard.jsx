import "./styles/MovieCard.css";

const MovieCard = ({movie,openModal}) => {
  return ( <div className="movie-card" key={movie.id} onClick={() => openModal(movie)}>
    <img src={movie.poster} alt="" />
    <h3>{movie.title}</h3>
    <p>{movie.rating}</p>
  
  </div>)
};

export default MovieCard;
