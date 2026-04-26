const TMDB_BASE_URL = "https://api.themoviedb.org/3";
const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
const TMDB_API_KEY = import.meta.env.VITE_TMDB_API_KEY;

export const isTmdbApiKeyMissing = !TMDB_API_KEY;

const mapTmdbMovie = (movie) => ({
  id: movie.id,
  title: movie.title || "Untitled",
  year: movie.release_date ? movie.release_date.split("-")[0] : "N/A",
  rating: Number(movie.vote_average || 0).toFixed(1),
  overview: movie.overview || "Overview not available.",
  poster: movie.poster_path
    ? `${TMDB_IMAGE_BASE_URL}${movie.poster_path}`
    : "https://via.placeholder.com/500x750?text=No+Image",
});

export const fetchTmdbMovies = async ({ page = 1, searchTerm = "" }) => {
  if (isTmdbApiKeyMissing) {
    throw new Error("TMDB API key is missing. Add VITE_TMDB_API_KEY in .env file.");
  }

  const trimmedSearch = searchTerm.trim();
  const endpoint = trimmedSearch ? "/search/movie" : "/movie/popular";

  const params = new URLSearchParams({
    api_key: TMDB_API_KEY,
    page: String(page),
  });

  if (trimmedSearch) {
    params.append("query", trimmedSearch);
  }

  const response = await fetch(`${TMDB_BASE_URL}${endpoint}?${params.toString()}`);

  if (!response.ok) {
    throw new Error("Failed to fetch movies from TMDB.");
  }

  const data = await response.json();

  return {
    movies: (data.results || []).map(mapTmdbMovie),
    totalPages: data.total_pages || 1,
  };
};
