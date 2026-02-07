package ru.otus.hw.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        List<Question> questions = questionDao.findAll();
        
        if (questions.isEmpty()) {
            ioService.printLine("No questions found");
            return;
        }
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            
            ioService.printFormattedLine("Question %d: %s", i + 1, question.text());
            ioService.printLine("Options:");
            
            List<Answer> answers = question.answers();
            for (int j = 0; j < answers.size(); j++) {
                Answer answer = answers.get(j);
                ioService.printFormattedLine("  %d. %s", j + 1, answer.text());
            }
            
            ioService.printLine("");
        }
    }
}
