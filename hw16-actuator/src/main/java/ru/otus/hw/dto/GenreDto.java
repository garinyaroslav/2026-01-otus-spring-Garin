package ru.otus.hw.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Genre;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenreDto {

    private long id;

    private String name;

    public static GenreDto of(Genre genre) {
        if (genre == null) {
            return null;
        }
        return new GenreDto(genre.getId(), genre.getName());
    }

    public static List<GenreDto> fromList(List<Genre> genres) {
        if (genres == null) {
            return List.of();
        }
        return genres.stream().map(GenreDto::of).toList();
    }

}
