package ru.otus.hw.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("books")
public class Book {

    @Id
    private Long id;

    private String title;

    @Column("author_id")
    private Long authorId;

    @Transient
    private Author author;

    @Transient
    private List<Genre> genres;

    @Transient
    private List<Comment> comments;

    public Book(Long id, String title, Long authorId) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
    }
}
