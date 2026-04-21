package ru.otus.hw.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {

    private long id;

    private String title;

    private AuthorDto author;

    private List<GenreDto> genres;

    private List<CommentDto> comments;

    public static BookDto of(Book book, Author author, List<Genre> genres, List<Comment> comments) {
        if (book == null) {
            return null;
        }
        return new BookDto(
                book.getId(),
                book.getTitle(),
                AuthorDto.of(author),
                GenreDto.fromList(genres),
                CommentDto.fromList(comments));
    }

}
