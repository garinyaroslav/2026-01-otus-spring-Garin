package ru.otus.hw.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLineLocalized("TestService.answer.the.questions");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question : questions) {
            var isAnswerValid = askQuestion(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private boolean askQuestion(Question question) {
        ioService.printLine(question.text());

        var answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            ioService.printFormattedLine("%d. %s", i + 1, answer.text());
        }

        int answerNumber = ioService.readIntForRangeLocalized(1, answers.size(), "TestService.error.message", 1,
                answers.size());

        Answer selectedAnswer = answers.get(answerNumber - 1);
        return selectedAnswer.isCorrect();
    }
}
