package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("Класс CsvQuestionDao")
class CsvQuestionDaoTest {

    private TestFileNameProvider fileNameProvider;
    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        fileNameProvider = Mockito.mock(TestFileNameProvider.class);
        csvQuestionDao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    @DisplayName("Должен корректно читать вопросы из существующего CSV файла")
    void shouldReadQuestionsFromExistingCsvFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");

        List<Question> questions = csvQuestionDao.findAll();

        assertThat(questions).isNotNull().hasSize(6);

        Question firstQuestion = questions.get(0);
        assertThat(firstQuestion.text()).isEqualTo("Is there life on Mars?");
        List<Answer> firstAnswers = firstQuestion.answers();
        assertThat(firstAnswers).hasSize(3);
        assertThat(firstAnswers.get(0).text()).isEqualTo("Science doesn't know this yet");
        assertThat(firstAnswers.get(1).text())
                .isEqualTo("Certainly. The red UFO is from Mars. And green is from Venus");
        assertThat(firstAnswers.get(2).text()).isEqualTo("Absolutely not");

        Question secondQuestion = questions.get(1);
        assertThat(secondQuestion.text()).isEqualTo("How should resources be loaded form jar in Java?");
        List<Answer> secondAnswers = secondQuestion.answers();
        assertThat(secondAnswers).hasSize(3);
        assertThat(secondAnswers.get(0).text())
                .isEqualTo("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream");
        assertThat(secondAnswers.get(1).text()).isEqualTo("ClassLoader#geResource#getFile + FileReader");
        assertThat(secondAnswers.get(2).text()).isEqualTo("Wingardium Leviosa");

        Question thirdQuestion = questions.get(2);
        assertThat(thirdQuestion.text()).isEqualTo("Which option is a good way to handle the exception?");
        List<Answer> thirdAnswers = thirdQuestion.answers();
        assertThat(thirdAnswers).hasSize(4);
        assertThat(thirdAnswers.get(0).text()).isEqualTo("@SneakyThrow");
        assertThat(thirdAnswers.get(1).text()).isEqualTo("e.printStackTrace()");
        assertThat(thirdAnswers.get(2).text())
                .isEqualTo("Rethrow with wrapping in business exception (for example, QuestionReadException)");
        assertThat(thirdAnswers.get(3).text()).isEqualTo("Ignoring exception");

        Question fourthQuestion = questions.get(3);
        assertThat(fourthQuestion.text()).isEqualTo("What is the correct way to start a thread in Java?");
        List<Answer> fourthAnswers = fourthQuestion.answers();
        assertThat(fourthAnswers).hasSize(4);
        assertThat(fourthAnswers.get(0).text()).isEqualTo("thread.start()");
        assertThat(fourthAnswers.get(1).text()).isEqualTo("thread.run()");
        assertThat(fourthAnswers.get(2).text()).isEqualTo("thread.execute()");
        assertThat(fourthAnswers.get(3).text()).isEqualTo("Calling the run() method directly is enough");

        Question fifthQuestion = questions.get(4);
        assertThat(fifthQuestion.text()).isEqualTo("Which HTTP method is idempotent and safe for retrieving data?");
        List<Answer> fifthAnswers = fifthQuestion.answers();
        assertThat(fifthAnswers).hasSize(4);
        assertThat(fifthAnswers.get(0).text()).isEqualTo("GET");
        assertThat(fifthAnswers.get(1).text()).isEqualTo("POST");
        assertThat(fifthAnswers.get(2).text()).isEqualTo("PUT");
        assertThat(fifthAnswers.get(3).text()).isEqualTo("DELETE");

        Question sixthQuestion = questions.get(5);
        assertThat(sixthQuestion.text()).isEqualTo("What is the default value of a local variable in Java?");
        List<Answer> sixthAnswers = sixthQuestion.answers();
        assertThat(sixthAnswers).hasSize(4);
        assertThat(sixthAnswers.get(0).text()).isEqualTo("null");
        assertThat(sixthAnswers.get(1).text()).isEqualTo("0 or false or '' depending on type");
        assertThat(sixthAnswers.get(2).text()).isEqualTo("It must be explicitly initialized");
        assertThat(sixthAnswers.get(3).text()).isEqualTo("undefined");
    }

    @Test
    @DisplayName("Должен бросать исключение при попытке чтения несуществующего CSV файла")
    void shouldThrowExceptionWhenCsvFileNotFound() {
        String nonExistentFileName = "non-existent-file.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(nonExistentFileName);

        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("File not found: " + nonExistentFileName);
    }
}
