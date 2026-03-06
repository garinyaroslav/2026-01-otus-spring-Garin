package ru.otus.hw.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public List<Author> findAll() {
        List<Author> res;

        try {
            res = jdbc.query("select id, full_name from authors", new AuthorRowMapper());
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exceptions while getting authors");
        }

        return res != null ? res : new ArrayList<>();
    }

    @Override
    public Optional<Author> findById(long id) {
        try {
            Author author = jdbc.queryForObject(
                    "select id, full_name from authors where id = :id",
                    new MapSqlParameterSource().addValue("id", id, Types.BIGINT),
                    new AuthorRowMapper());

            return Optional.ofNullable(author);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exception while getting author by id: " + id);
        }
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            return new Author(rs.getLong("id"), rs.getString("full_name"));
        }
    }
}
