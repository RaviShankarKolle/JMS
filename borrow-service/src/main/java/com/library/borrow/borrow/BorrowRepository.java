package com.library.borrow.borrow;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BorrowRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createPending(Long userId, Long bookId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                INSERT INTO borrow_records(user_id, book_id, start_date, end_date, status)
                VALUES(:userId, :bookId, :startDate, :endDate, 'PENDING_PICKUP')
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("bookId", bookId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate));
        String idSql = "SELECT id FROM borrow_records WHERE user_id = :userId AND book_id = :bookId ORDER BY id DESC LIMIT 1";
        Long id = jdbcTemplate.queryForObject(idSql, new MapSqlParameterSource().addValue("userId", userId).addValue("bookId", bookId), Long.class);
        return id == null ? 0L : id;
    }

    public Optional<BorrowRecord> findById(Long borrowId) {
        String sql = """
                SELECT id, user_id, book_id, start_date, end_date, due_date, status, updated_at
                FROM borrow_records
                WHERE id = :id
                """;
        List<BorrowRecord> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("id", borrowId), new BorrowRowMapper());
        return rows.stream().findFirst();
    }

    public void markAllocated(Long borrowId, LocalDate dueDate) {
        String sql = """
                UPDATE borrow_records
                SET status = 'ALLOCATED', due_date = :dueDate, updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", borrowId).addValue("dueDate", dueDate));
    }

    public void markReturned(Long borrowId) {
        String sql = "UPDATE borrow_records SET status = 'RETURNED', updated_at = CURRENT_TIMESTAMP WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", borrowId));
    }

    public int countActiveLoans(Long userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = :userId AND status IN ('PENDING_PICKUP', 'ALLOCATED')";
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), Integer.class);
        return count == null ? 0 : count;
    }

    public List<BorrowRecord> listByUser(Long userId, int offset, int size) {
        String sql = """
                SELECT id, user_id, book_id, start_date, end_date, due_date, status, updated_at
                FROM borrow_records
                WHERE user_id = :userId
                ORDER BY id DESC
                LIMIT :size OFFSET :offset
                """;
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource().addValue("userId", userId).addValue("size", size).addValue("offset", offset),
                new BorrowRowMapper());
    }

    public long countByUser(Long userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = :userId";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("userId", userId), Long.class);
        return count == null ? 0L : count;
    }

    public List<BorrowRecord> findAllocatedOverdue(LocalDate today) {
        String sql = """
                SELECT id, user_id, book_id, start_date, end_date, due_date, status, updated_at
                FROM borrow_records
                WHERE status = 'ALLOCATED' AND due_date IS NOT NULL AND due_date < :today
                ORDER BY due_date ASC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("today", today), new BorrowRowMapper());
    }

    public void markOverdue(Long borrowId) {
        String sql = "UPDATE borrow_records SET status = 'OVERDUE', updated_at = CURRENT_TIMESTAMP WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", borrowId));
    }

    public record BorrowRecord(
            Long id,
            Long userId,
            Long bookId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate dueDate,
            String status,
            Instant updatedAt
    ) {}

    private static class BorrowRowMapper implements RowMapper<BorrowRecord> {
        @Override
        public BorrowRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new BorrowRecord(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getLong("book_id"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date").toLocalDate(),
                    rs.getDate("due_date") == null ? null : rs.getDate("due_date").toLocalDate(),
                    rs.getString("status"),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}
