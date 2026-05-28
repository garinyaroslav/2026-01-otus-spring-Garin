package ru.otus.hw.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanProcessingResult {

    private String applicationId;

    private String applicantName;

    private LoanApplication.LoanStatus decision;

    private BigDecimal approvedAmount;

    private String message;

    private LocalDateTime decidedAt;

    private String processingLog;

    public boolean isApproved() {
        return decision == LoanApplication.LoanStatus.APPROVED
                || decision == LoanApplication.LoanStatus.PARTIALLY_APPROVED;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] Bid #%s (%s): %s%s%n  Log: %s",
                decidedAt,
                applicationId,
                applicantName,
                decision,
                approvedAmount != null ? " | Sum: " + approvedAmount : "",
                processingLog);
    }
}
