package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    private String id;

    private String applicantName;

    private int applicantAge;

    private BigDecimal requestedAmount;

    private int creditScore;

    private BigDecimal monthlyIncome;

    private LoanStatus status;

    private String rejectionReason;

    private BigDecimal approvedAmount;

    private LocalDateTime processedAt;

    private String processingLog;

    public enum LoanStatus {
        SUBMITTED,
        VALIDATING,
        ENRICHING,
        SCORING,
        APPROVED,
        PARTIALLY_APPROVED,
        REJECTED
    }
}
