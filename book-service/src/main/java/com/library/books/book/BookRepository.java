package com.library.books.book;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(String title, String author, String genre, String isbn, int totalCopies, String description) {
        String sql = """
                INSERT INTO books(title, author, genre, isbn, total_copies, available_copies, status, description)
                VALUES(:title, :author, :genre, :isbn, :totalCopies, :totalCopies, 'AVAILABLE', :description)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("title", title)
                .addValue("author", author)
                .addValue("genre", genre)
                .addValue("isbn", isbn)
                .addValue("totalCopies", totalCopies)
                .addValue("description", description));
        return findByIsbn(isbn).map(BookRecord::id).orElseThrow();
    }

    public Optional<BookRecord> findById(Long id) {
        String sql = """
                SELECT id, title, author, genre, isbn, total_copies, available_copies, status, description, updated_at
                FROM books WHERE id = :id
                """;
        List<BookRecord> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), new BookRowMapper());
        return rows.stream().findFirst();
    }

    public Optional<BookRecord> findByIsbn(String isbn) {
        String sql = """
                SELECT id, title, author, genre, isbn, total_copies, available_copies, status, description, updated_at
                FROM books WHERE isbn = :isbn
                """;
        List<BookRecord> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("isbn", isbn), new BookRowMapper());
        return rows.stream().findFirst();
    }

    public void update(Long id, String title, String author, String genre, String isbn, int totalCopies, String description) {
        String sql = """
                UPDATE books
                SET title = :title, author = :author, genre = :genre, isbn = :isbn, total_copies = :totalCopies,
                    description = :description, updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("title", title)
                .addValue("author", author)
                .addValue("genre", genre)
                .addValue("isbn", isbn)
                .addValue("totalCopies", totalCopies)
                .addValue("description", description));
    }

    public void markRemoved(Long id) {
        String sql = "UPDATE books SET status = 'REMOVED', updated_at = CURRENT_TIMESTAMP WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    public int reserveCopy(Long id) {
        String sql = """
                UPDATE books
                SET available_copies = available_copies - 1, status = IF(available_copies - 1 > 0, 'AVAILABLE', 'RESERVED'),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id AND status <> 'REMOVED' AND available_copies > 0
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    public int issueCopy(Long id) {
        String sql = """
                UPDATE books
                SET status = IF(available_copies > 0, 'AVAILABLE', 'BORROWED'), updated_at = CURRENT_TIMESTAMP
                WHERE id = :id AND status <> 'REMOVED'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    public int returnCopy(Long id) {
        String sql = """
                UPDATE books
                SET available_copies = available_copies + 1, status = 'AVAILABLE', updated_at = CURRENT_TIMESTAMP
                WHERE id = :id AND status <> 'REMOVED'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    public List<BookRecord> list(int offset, int size) {
        String sql = """
                SELECT id, title, author, genre, isbn, total_copies, available_copies, status, description, updated_at
                FROM books
                WHERE status = 'AVAILABLE'
                ORDER BY id DESC
                LIMIT :size OFFSET :offset
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("size", size).addValue("offset", offset), new BookRowMapper());
    }

    public long countActive() {
        String sql = "SELECT COUNT(*) FROM books WHERE status = 'AVAILABLE'";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return count == null ? 0L : count;
    }

    public record BookRecord(Long id, String title, String author, String genre, String isbn,
                             int totalCopies, int availableCopies, String status, String description, Instant updatedAt) {}

    private static class BookRowMapper implements RowMapper<BookRecord> {
        @Override
        public BookRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new BookRecord(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("isbn"),
                    rs.getInt("total_copies"),
                    rs.getInt("available_copies"),
                    rs.getString("status"),
                    rs.getString("description"),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}
