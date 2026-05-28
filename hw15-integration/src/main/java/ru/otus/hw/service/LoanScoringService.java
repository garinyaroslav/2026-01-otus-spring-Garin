package ru.otus.hw.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.models.LoanApplication;
import ru.otus.hw.models.LoanProcessingResult;

@Slf4j
@Service
public class LoanScoringService {

    private static final int MIN_CREDIT_SCORE = 400;

    private static final int PARTIAL_SCORE_LIMIT = 600;

    private static final BigDecimal MAX_DTI_RATIO = new BigDecimal("0.40");

    public LoanApplication makeDecision(LoanApplication application) {
        int score = application.getCreditScore();
        BigDecimal income = application.getMonthlyIncome();
        BigDecimal requested = application.getRequestedAmount();

        BigDecimal monthlyPayment = computeMonthlyPayment(requested);
        BigDecimal dtiRatio = computeDtiRatio(monthlyPayment, income);

        log.info("▶ Scoring: applicant='{}', score={}, DTI={}",
                application.getApplicantName(), score, dtiRatio);

        return evaluate(application, score, dtiRatio, monthlyPayment, income);
    }

    private BigDecimal computeMonthlyPayment(BigDecimal requested) {
        return requested
                .divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("1.01"));
    }

    private BigDecimal computeDtiRatio(BigDecimal monthlyPayment, BigDecimal income) {
        return income != null && income.compareTo(BigDecimal.ZERO) > 0
                ? monthlyPayment.divide(income, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
    }

    private LoanApplication evaluate(LoanApplication app, int score,
            BigDecimal dtiRatio, BigDecimal monthlyPayment,
            BigDecimal income) {
        if (score < MIN_CREDIT_SCORE) {
            return reject(app, "Credit score is very low: " + score);
        }
        if (dtiRatio.compareTo(MAX_DTI_RATIO) > 0) {
            if (score >= PARTIAL_SCORE_LIMIT) {
                BigDecimal maxAllowed = income
                        .multiply(MAX_DTI_RATIO)
                        .multiply(BigDecimal.valueOf(24))
                        .divide(new BigDecimal("1.01"), 2, RoundingMode.HALF_UP);
                return partialApprove(app, maxAllowed);
            } else {
                return reject(app, "High debt burden (DTI=" + dtiRatio + ")");
            }
        }
        return approve(app);
    }

    private LoanApplication approve(LoanApplication app) {
        app.setStatus(LoanApplication.LoanStatus.APPROVED);
        app.setApprovedAmount(app.getRequestedAmount());
        appendLog(app, "APPROVED for the full amount " + app.getRequestedAmount());
        log.info("APPROVED: {}", app.getApplicantName());
        return app;
    }

    private LoanApplication partialApprove(LoanApplication app, BigDecimal amount) {
        app.setStatus(LoanApplication.LoanStatus.PARTIALLY_APPROVED);
        app.setApprovedAmount(amount);
        appendLog(app, "PARTIALLY APPROVED " + amount + " (requested " + app.getRequestedAmount() + ")");
        log.info("⚠️ PARTIALLY APPROVED: {} → {}", app.getApplicantName(), amount);
        return app;
    }

    private LoanApplication reject(LoanApplication app, String reason) {
        app.setStatus(LoanApplication.LoanStatus.REJECTED);
        app.setRejectionReason(reason);
        appendLog(app, "REJECTED: " + reason);
        log.info("❌ REJECTED: {} — {}", app.getApplicantName(), reason);
        return app;
    }

    public LoanProcessingResult toResult(LoanApplication application) {
        return LoanProcessingResult.builder()
                .applicationId(application.getId())
                .applicantName(application.getApplicantName())
                .decision(application.getStatus())
                .approvedAmount(application.getApprovedAmount())
                .message(application.getRejectionReason() != null
                        ? application.getRejectionReason()
                        : "The application was processed successfully")
                .decidedAt(LocalDateTime.now())
                .processingLog(application.getProcessingLog())
                .build();
    }

    private void appendLog(LoanApplication app, String entry) {
        String current = app.getProcessingLog() != null ? app.getProcessingLog() : "";
        app.setProcessingLog(current + " → " + entry);
    }
}
