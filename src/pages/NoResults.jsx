const NoResults = ({ searchTerm }) => {
  return (
    <div className="no-results-page">
      <h2>Sorry, your searched movie is not available.</h2>
      <p>Try searching with a different movie name.</p>
      {searchTerm ? <p>Search term: "{searchTerm}"</p> : null}
    </div>
  );
};

export default NoResults;
