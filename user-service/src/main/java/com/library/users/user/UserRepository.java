package com.library.users.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserRecord> findByEmail(String email) {
        String sql = "SELECT id, name, email, phone, password_hash, status FROM users WHERE email = :email";
        List<UserRecord> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("email", email), new UserRowMapper());
        return rows.stream().findFirst();
    }

    public Optional<UserRecord> findById(Long id) {
        String sql = "SELECT id, name, email, phone, password_hash, status FROM users WHERE id = :id";
        List<UserRecord> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), new UserRowMapper());
        return rows.stream().findFirst();
    }

    public Long createUser(String name, String email, String phone, String passwordHash) {
        String sql = "INSERT INTO users(name, email, phone, password_hash, status) VALUES(:name, :email, :phone, :passwordHash, 'ACTIVE')";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("email", email)
                .addValue("phone", phone)
                .addValue("passwordHash", passwordHash);
        jdbcTemplate.update(sql, params);
        return findByEmail(email).map(UserRecord::id).orElseThrow();
    }

    public void updateProfile(Long userId, String name, String phone) {
        String sql = "UPDATE users SET name = :name, phone = :phone WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("phone", phone)
                .addValue("id", userId));
    }

    public Set<String> getRoles(Long userId) {
        String sql = """
                SELECT r.role_name
                FROM user_roles ur
                JOIN roles r ON ur.role_id = r.id
                WHERE ur.user_id = :userId
                """;
        List<String> roles = jdbcTemplate.queryForList(sql, new MapSqlParameterSource("userId", userId), String.class);
        return roles.stream().collect(Collectors.toSet());
    }

    public void assignRole(Long userId, String roleName) {
        String sql = """
                INSERT INTO user_roles(user_id, role_id)
                SELECT :userId, id FROM roles WHERE role_name = :roleName
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource().addValue("userId", userId).addValue("roleName", roleName));
    }

    public void insertPasswordResetToken(Long userId, String token, Instant expiry) {
        String sql = "INSERT INTO password_reset_tokens(user_id, token, expiry, used) VALUES(:userId, :token, :expiry, false)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("token", token)
                .addValue("expiry", expiry));
    }

    public Optional<Long> findValidTokenUser(String token) {
        String sql = """
                SELECT user_id FROM password_reset_tokens
                WHERE token = :token AND used = false AND expiry > NOW()
                ORDER BY id DESC LIMIT 1
                """;
        List<Long> users = jdbcTemplate.queryForList(sql, new MapSqlParameterSource("token", token), Long.class);
        return users.stream().findFirst();
    }

    public void markTokenUsed(String token) {
        String sql = "UPDATE password_reset_tokens SET used = true WHERE token = :token";
        jdbcTemplate.update(sql, new MapSqlParameterSource("token", token));
    }

    public void updatePassword(Long userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = :passwordHash WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("passwordHash", passwordHash)
                .addValue("id", userId));
    }

    public void auditLog(Long userId, String action, String ip, String userAgent) {
        String sql = "INSERT INTO audit_logs(user_id, action, ip, user_agent) VALUES(:userId, :action, :ip, :userAgent)";
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("action", action)
                .addValue("ip", ip)
                .addValue("userAgent", userAgent));
    }

    public record UserRecord(Long id, String name, String email, String phone, String passwordHash, String status) {}

    private static class UserRowMapper implements RowMapper<UserRecord> {
        @Override
        public UserRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserRecord(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password_hash"),
                    rs.getString("status")
            );
        }
    }
}
