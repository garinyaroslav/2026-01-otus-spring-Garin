package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@DisplayName("Тест сервиса тестирования")
class TestServiceImplTest {

    private IOService ioService;
    private QuestionDao questionDao;
    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    @DisplayName("Должен вывести все вопросы и ответы")
    void shouldPrintAllQuestionsAndAnswers() {
        List<Question> testQuestions = Arrays.asList(
                new Question("Question 1?", Arrays.asList(
                        new Answer("Answer 1.1", true),
                        new Answer("Answer 1.2", false)
                ))
        );
        
        when(questionDao.findAll()).thenReturn(testQuestions);

        testService.executeTest();

        verify(ioService, atLeastOnce()).printFormattedLine("Please answer the questions below%n");
        verify(ioService, atLeastOnce()).printFormattedLine("Question %d: %s", 1, "Question 1?");
        verify(ioService, atLeastOnce()).printLine("Options:");
        verify(ioService, atLeastOnce()).printFormattedLine("  %d. %s", 1, "Answer 1.1");
        verify(ioService, atLeastOnce()).printFormattedLine("  %d. %s", 2, "Answer 1.2");
    }

    @Test
    @DisplayName("Должен сообщить, если вопросы не найдены")
    void shouldPrintMessageWhenNoQuestionsFound() {
        when(questionDao.findAll()).thenReturn(Collections.emptyList());

        testService.executeTest();

        verify(ioService, atLeastOnce()).printLine("No questions found");
        verify(ioService, never()).printFormattedLine(eq("Question %d: %s"), anyInt(), anyString());
    }

    @Test
    @DisplayName("Должен корректно обрабатывать вопрос без ответов")
    void shouldHandleQuestionWithoutAnswers() {
        Question questionWithoutAnswers = new Question("Empty question?", 
                Collections.emptyList());
        
        when(questionDao.findAll()).thenReturn(
                Collections.singletonList(questionWithoutAnswers));

        testService.executeTest();

        verify(ioService, atLeastOnce()).printFormattedLine("Question %d: %s", 1, "Empty question?");
        verify(ioService, atLeastOnce()).printLine("Options:");
        verify(ioService, never()).printFormattedLine(eq("  %d. %s"), anyInt(), anyString());
    }

    @Test
    @DisplayName("Должен корректно нумеровать вопросы и ответы")
    void shouldNumberQuestionsAndAnswersCorrectly() {
        Question question = new Question("Test?", Arrays.asList(
                new Answer("A", true),
                new Answer("B", false),
                new Answer("C", false)
        ));
        
        when(questionDao.findAll()).thenReturn(
                Collections.singletonList(question));

        testService.executeTest();

        verify(ioService, atLeastOnce()).printFormattedLine("Question %d: %s", 1, "Test?");
        verify(ioService, atLeastOnce()).printFormattedLine("  %d. %s", 1, "A");
        verify(ioService, atLeastOnce()).printFormattedLine("  %d. %s", 2, "B");
        verify(ioService, atLeastOnce()).printFormattedLine("  %d. %s", 3, "C");
    }
}
