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
    private Long id;

    private String text;

    @Column("book_id")
    private Long bookId;

    @Transient
    private Book book;

    public Comment(Long id, String text, Long bookId) {
        this.id = id;
        this.text = text;
        this.bookId = bookId;
    }
}
