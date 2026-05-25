package ru.otus.hw.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.models.LoanApplication;

@Slf4j
@Service
public class CreditBureauService {

    private static final Map<String, Integer> CREDIT_SCORES = new HashMap<>();

    static {
        CREDIT_SCORES.put("Ivan Ivanov", 750);
        CREDIT_SCORES.put("Maria Petrova", 820);
        CREDIT_SCORES.put("Alexey Sidorov", 450);
        CREDIT_SCORES.put("Elena Kuznetsova", 680);
        CREDIT_SCORES.put("Dmitry Novikov", 320);
        CREDIT_SCORES.put("Young Client", 500);
    }

    public LoanApplication enrichWithCreditScore(LoanApplication application) {
        int score = CREDIT_SCORES.getOrDefault(
                application.getApplicantName(),
                calculateDefaultScore(application));

        log.info("▶ Enrichment: applicant='{}', credit score={}",
                application.getApplicantName(), score);

        application.setCreditScore(score);
        application.setStatus(LoanApplication.LoanStatus.SCORING);
        appendLog(application, "Credit score was received: " + score);
        return application;
    }

    private int calculateDefaultScore(LoanApplication app) {
        int base = 500;

        if (app.getApplicantAge() > 25 && app.getApplicantAge() < 60) {
            base += 50;
        }
        if (app.getMonthlyIncome() != null && app.getMonthlyIncome().intValue() > 50_000) {
            base += 100;
        }

        return Math.min(base, 850);
    }

    private void appendLog(LoanApplication app, String entry) {
        String current = app.getProcessingLog() != null ? app.getProcessingLog() : "";
        app.setProcessingLog(current + " → " + entry);
    }
}
