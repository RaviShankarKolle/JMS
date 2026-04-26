import { useState, useEffect, useCallback } from "react";
import { fetchTmdbMovies, isTmdbApiKeyMissing } from "../services/tmdbService";

const useMovies = () => {
  const [movies, setMovies] = useState([]);
  const [modalMovie, setModalMovie] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortYear, setSortYear] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [error, setError] = useState("");
  const apiKeyMissing = isTmdbApiKeyMissing;

  const fetchMovies = useCallback(
    async (page = 1, replace = false) => {
      try {
        setError("");
        const data = await fetchTmdbMovies({ page, searchTerm });

        setMovies((prevMovies) =>
          replace ? data.movies : [...prevMovies, ...data.movies]
        );
        setCurrentPage(page);
        setTotalPages(data.totalPages);
      } catch (fetchError) {
        setError(fetchError.message || "Something went wrong while fetching movies.");
      }
    },
    [searchTerm]
  );

  useEffect(() => {
    if (apiKeyMissing) {
      return;
    }

    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchMovies(1, true);
  }, [apiKeyMissing, fetchMovies]);

  const handleLoadMore = () => {
    if (currentPage < totalPages) {
      fetchMovies(currentPage + 1, false);
    }
  };

  const filteredMovies = [...movies];

  if (sortYear === "asc") {
    filteredMovies.sort((a, b) => a.year - b.year);
  } else if (sortYear === "desc") {
    filteredMovies.sort((a, b) => b.year - a.year);
  } else if (sortYear === "popular") {
    filteredMovies.sort((a, b) => parseFloat(b.rating) - parseFloat(a.rating));
  }

  return {
    modalMovie,
    setModalMovie,
    searchTerm,
    setSearchTerm,
    sortYear,
    setSortYear,
    darkMode,
    setDarkMode,
    error,
    apiKeyMissing,
    displayMovies: filteredMovies,
    isSearchScreen: searchTerm.trim() !== "",
    canLoadMore: currentPage < totalPages,
    handleLoadMore,
  };
};

export default useMovies;
