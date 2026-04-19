package ru.otus.hw.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private long id;

    private String text;

    private long bookId;

    public static CommentDto of(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getBook().getId());
    }

    public static List<CommentDto> fromList(List<Comment> comments) {
        if (comments == null) {
            return List.of();
        }
        return comments.stream().map(CommentDto::of).toList();
    }

}
