package ru.otus.hw.repositories;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public List<Genre> findAll() {
        List<Genre> res;

        try {
            res = jdbc.query("select id, name from genres", new GenreRowMapper());
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exceptions while getting genres");
        }

        return res != null ? res : new ArrayList<>();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<Genre> res;
        try {
            res = jdbc.query("select id, name from genres where id in (:ids)", Map.of("ids", ids),
                    new GenreRowMapper());
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exceptions while getting genres by ids");
        }

        return res != null ? res : new ArrayList<>();
    }

    private static class GenreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            return new Genre(rs.getLong("id"), rs.getString("name"));
        }
    }
}
