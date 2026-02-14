package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Тест сервиса тестирования")
class TestServiceImplTest {

    private IOService ioService;
    private QuestionDao questionDao;
    private TestServiceImpl testService;
    private Student student;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);
        student = new Student("Ivan", "Ivanov");
    }

    @Test
    @DisplayName("Должен вывести все вопросы и варианты ответов")
    void shouldPrintAllQuestionsAndAnswers() {
        List<Question> testQuestions = Arrays.asList(
                new Question("What is 2+2?", Arrays.asList(
                        new Answer("3", false),
                        new Answer("4", true),
                        new Answer("5", false))));

        when(questionDao.findAll()).thenReturn(testQuestions);
        when(ioService.readIntForRange(anyInt(), anyInt(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        verify(ioService, times(1)).printFormattedLine("Please answer the questions below%n");

        verify(ioService, times(1)).printLine("What is 2+2?");
        verify(ioService, times(1)).printFormattedLine("%d. %s", 1, "3");
        verify(ioService, times(1)).printFormattedLine("%d. %s", 2, "4");
        verify(ioService, times(1)).printFormattedLine("%d. %s", 3, "5");

        verify(ioService, times(1)).readIntForRange(1, 3,
                "Please enter answer number (1-3): ");

        assertThat(result.getAnsweredQuestions()).hasSize(1);
        assertThat(result.getRightAnswersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать неправильные ответы")
    void shouldHandleWrongAnswers() {
        List<Question> testQuestions = Arrays.asList(
                new Question("What is 2+2?", Arrays.asList(
                        new Answer("3", false),
                        new Answer("4", true),
                        new Answer("5", false))));

        when(questionDao.findAll()).thenReturn(testQuestions);
        when(ioService.readIntForRange(anyInt(), anyInt(), anyString()))
                .thenReturn(3);

        TestResult result = testService.executeTestFor(student);

        verify(ioService, times(1)).readIntForRange(1, 3,
                "Please enter answer number (1-3): ");

        assertThat(result.getAnsweredQuestions()).hasSize(1);
        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Должен обрабатывать пустой список вопросов")
    void shouldHandleEmptyQuestionsList() {
        when(questionDao.findAll()).thenReturn(Collections.emptyList());

        TestResult result = testService.executeTestFor(student);

        verify(ioService, times(1)).printLine("");
        verify(ioService, times(1)).printFormattedLine("Please answer the questions below%n");

        verify(ioService, never()).printLine(argThat(s -> !s.isEmpty() && !s.equals("")));
        verify(ioService, never()).printFormattedLine(eq("%d. %s"), anyInt(), anyString());
        verify(ioService, never()).readIntForRange(anyInt(), anyInt(), anyString());

        assertThat(result.getAnsweredQuestions()).isEmpty();
        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }
}
