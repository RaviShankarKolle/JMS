import "./App.css";
import Header from "./components/Header";
import HomePage from "./pages/HomePage";
import SearchPage from "./pages/SearchPage";
import MovieDetailsPage from "./pages/MovieDetailsPage";
import useMovies from "./hooks/useMovies";

const App = () => {
  const {
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
    displayMovies,
    isSearchScreen,
    canLoadMore,
    handleLoadMore,
  } = useMovies();

  return (
    
  <div className={darkMode ? "app dark" : "app"}>
    <Header searchTerm={searchTerm}
     setSearchTerm={setSearchTerm} sortYear={sortYear} 
     setSortYear={setSortYear} 
    darkMode ={darkMode}
     setDarkMode={setDarkMode} 
     />
    {apiKeyMissing ? (
      <p className="status-message error">TMDB API key is missing. Add VITE_TMDB_API_KEY in .env file.</p>
    ) : null}
    {error ? <p className="status-message error">{error}</p> : null}
    {isSearchScreen ? (
      <SearchPage
        searchTerm={searchTerm}
        movies={displayMovies}
        openMovieDetails={setModalMovie}
        canLoadMore={canLoadMore}
        onLoadMore={handleLoadMore}
      />
    ) : (
      <HomePage
        movies={displayMovies}
        openMovieDetails={setModalMovie}
        canLoadMore={canLoadMore}
        onLoadMore={handleLoadMore}
      />
    )}

    <MovieDetailsPage movie={modalMovie} onClose={() => setModalMovie(null)} />
  

</div>
    


  );

  
};

export default App
