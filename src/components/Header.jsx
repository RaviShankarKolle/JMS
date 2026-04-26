import "./styles/Header.css";

const Header = ({searchTerm,setSearchTerm,sortYear,setSortYear,darkMode,setDarkMode}) => {
   return (<header>
    <div className="logo">
        <h1>Coders Movie</h1>

    </div>
    <div className="header-controls">
        <input type="text" placeholder="Search movies..." value={searchTerm} 
        onChange={(e)=>setSearchTerm(e.target.value)}/>
       
      
        <select  value={sortYear || ""} 
       onChange={(e)=>setSortYear(e.target.value)}>
            <option value="">Sort By Year</option>
            <option value="asc">Year Ascending</option>
            <option value="desc">Year Descending</option>
            <option value="popular">Popular Movie List</option>

        </select>
        <label>
            <input type="checkbox"
             checked={darkMode} 
             onClick={()=>setDarkMode(!darkMode)}/>
            <span>Dark Mode</span>
        </label>
    </div>
   </header>)
};

export default Header;
