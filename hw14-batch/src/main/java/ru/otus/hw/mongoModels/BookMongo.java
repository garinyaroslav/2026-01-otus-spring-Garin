package ru.otus.hw.mongoModels;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(collection = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookMongo {

    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    private String id;

    @ToString.Include
    private String title;

    private String authorId;

    private List<String> genreIds;

    @Indexed(unique = true)
    private Long sourceId;

}
