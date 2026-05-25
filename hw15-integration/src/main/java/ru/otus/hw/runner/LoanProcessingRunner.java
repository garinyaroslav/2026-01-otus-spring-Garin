package ru.otus.hw.runner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.gateway.LoanGateway;
import ru.otus.hw.models.LoanApplication;
import ru.otus.hw.models.LoanProcessingResult;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class LoanProcessingRunner implements CommandLineRunner {

    private final LoanGateway loanGateway;

    @Override
    public void run(String... args) {
        printSeparator("LOAN APPLICATION PROCESSING SYSTEM STARTED");
        List<Scenario> scenarios = getScenarios();
        List<LoanProcessingResult> results = new ArrayList<>();
        for (Scenario s : scenarios) {
            results.add(processScenario(s.title, s.application));
        }
        printSeparator("TOTAL APPLICATIONS PROCESSED: " + scenarios.size());
        long approved = results.stream().filter(LoanProcessingResult::isApproved).count();
        log.info("✅ Approved: {} | ❌ Rejected: {}", approved, scenarios.size() - approved);
    }

    private LoanProcessingResult processScenario(String title, LoanApplication app) {
        printSeparator(title);
        LoanProcessingResult result = loanGateway.processApplication(app);
        printResult(result);
        return result;
    }

    private void printResult(LoanProcessingResult result) {
        log.info("\n{}", result);
    }

    private void printSeparator(String title) {
        log.info(" {}", title);
    }

    private List<Scenario> getScenarios() {
        return List.of(
                new Scenario("Scenario 1: Good Credit History → APPROVAL", LoanApplication.builder()
                        .applicantName("Maria Petrova").applicantAge(35)
                        .requestedAmount(new BigDecimal("500000")).monthlyIncome(new BigDecimal("120000"))
                        .build()),
                new Scenario("Scenario 2: High Load → PARTIAL APPROVAL", LoanApplication.builder()
                        .applicantName("Elena Kuznetsova").applicantAge(28)
                        .requestedAmount(new BigDecimal("2000000")).monthlyIncome(new BigDecimal("80000"))
                        .build()),
                new Scenario("Scenario 3: Poor Credit History → REJECTION", LoanApplication.builder()
                        .applicantName("Dmitry Novikov").applicantAge(42)
                        .requestedAmount(new BigDecimal("300000")).monthlyIncome(new BigDecimal("70000"))
                        .build()),
                new Scenario("Scenario 4: Age < 18 → VALIDATION REJECTION", LoanApplication.builder()
                        .applicantName("Young Applicant").applicantAge(16)
                        .requestedAmount(new BigDecimal("100000")).monthlyIncome(new BigDecimal("20000"))
                        .build()),
                new Scenario("Scenario 5: Single Application with Excellent History", LoanApplication.builder()
                        .applicantName("Ivan Ivanov").applicantAge(45)
                        .requestedAmount(new BigDecimal("750000")).monthlyIncome(new BigDecimal("200000"))
                        .build()));
    }

    private record Scenario(String title, LoanApplication application) {
    }
}
