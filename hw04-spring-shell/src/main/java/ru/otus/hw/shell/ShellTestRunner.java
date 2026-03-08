package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.service.TestRunnerService;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@RequiredArgsConstructor
public class ShellTestRunner {

    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Запустить тестирование", key = { "test", "start" })
    public void runTest() {
        testRunnerService.run();
    }

}
