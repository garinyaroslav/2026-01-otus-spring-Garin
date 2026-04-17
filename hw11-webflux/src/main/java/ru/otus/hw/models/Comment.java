package ru.otus.hw.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("comments")
public class Comment {

    @Id
    private long id;

    private String text;

    @Column("book_id")
    private long bookId;

    @Transient
    private Book book;

    public Comment(long id, String text, long bookId) {
        this.id = id;
        this.text = text;
        this.bookId = bookId;
    }
}
