package ru.otus.hw.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Book;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {

    private long id;

    private String title;

    private AuthorDto author;

    private List<GenreDto> genres;

    private List<CommentDto> comments;

    public static BookDto of(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDto(
                book.getId(),
                book.getTitle(),
                AuthorDto.of(book.getAuthor()),
                GenreDto.fromList(book.getGenres()),
                CommentDto.fromList(book.getComments()));
    }

    public static List<BookDto> fromList(List<Book> books) {
        if (books == null) {
            return List.of();
        }
        return books.stream().map(BookDto::of).toList();

    }

}
