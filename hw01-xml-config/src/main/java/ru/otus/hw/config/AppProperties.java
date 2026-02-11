package ru.otus.hw.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AppProperties implements TestFileNameProvider {
    private String testFileName;
}
