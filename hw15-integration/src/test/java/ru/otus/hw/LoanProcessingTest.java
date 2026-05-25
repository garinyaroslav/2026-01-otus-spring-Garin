package ru.otus.hw;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import ru.otus.hw.gateway.LoanGateway;
import ru.otus.hw.models.LoanApplication;
import ru.otus.hw.models.LoanProcessingResult;

@SpringBootTest
@SpringIntegrationTest
@ActiveProfiles("test")
@DisplayName("Integration Tests: Loan Application Processing")
class LoanProcessingIntegrationTest {

    @Autowired
    private LoanGateway loanGateway;

    @Test
    @DisplayName("Good applicant → APPROVED")
    void whenGoodApplicant_thenApproved() {
        LoanApplication application = LoanApplication.builder()
                .applicantName("Maria Petrova")
                .applicantAge(35)
                .requestedAmount(new BigDecimal("300000"))
                .monthlyIncome(new BigDecimal("150000"))
                .build();

        LoanProcessingResult result = loanGateway.processApplication(application);

        assertThat(result).isNotNull();
        assertThat(result.getApplicationId()).isNotNull();
        assertThat(result.getDecision()).isEqualTo(LoanApplication.LoanStatus.APPROVED);
        assertThat(result.getApprovedAmount()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(result.isApproved()).isTrue();
    }

    @Test
    @DisplayName("Low credit score → REJECTED")
    void whenLowCreditScore_thenRejected() {
        LoanApplication application = LoanApplication.builder()
                .applicantName("Dmitry Novikov")
                .applicantAge(42)
                .requestedAmount(new BigDecimal("500000"))
                .monthlyIncome(new BigDecimal("100000"))
                .build();

        LoanProcessingResult result = loanGateway.processApplication(application);

        assertThat(result.getDecision()).isEqualTo(LoanApplication.LoanStatus.REJECTED);
        assertThat(result.isApproved()).isFalse();
    }

    @Test
    @DisplayName("Applicant under 18 → REJECTED by validation filter")
    void whenApplicantUnderAge_thenRejectedByFilter() {
        LoanApplication application = LoanApplication.builder()
                .applicantName("Young Client")
                .applicantAge(16)
                .requestedAmount(new BigDecimal("100000"))
                .monthlyIncome(new BigDecimal("15000"))
                .build();

        LoanProcessingResult result = loanGateway.processApplication(application);

        assertThat(result.getDecision()).isEqualTo(LoanApplication.LoanStatus.REJECTED);
        assertThat(result.getProcessingLog()).contains("REJECTED during validation");
        assertThat(result.getProcessingLog()).doesNotContain("score");
    }

    @Test
    @DisplayName("High DTI with good score → PARTIALLY_APPROVED")
    void whenHighDtiWithGoodScore_thenPartiallyApproved() {
        LoanApplication application = LoanApplication.builder()
                .applicantName("Elena Kuznetsova")
                .applicantAge(30)
                .requestedAmount(new BigDecimal("3000000"))
                .monthlyIncome(new BigDecimal("60000"))
                .build();

        LoanProcessingResult result = loanGateway.processApplication(application);

        assertThat(result.getDecision()).isEqualTo(LoanApplication.LoanStatus.PARTIALLY_APPROVED);
        assertThat(result.isApproved()).isTrue();
        assertThat(result.getApprovedAmount())
                .isLessThan(new BigDecimal("3000000"))
                .isPositive();
    }

    @Test
    @DisplayName("Amount below minimum → REJECTED by validation")
    void whenAmountBelowMinimum_thenRejectedByFilter() {
        LoanApplication application = LoanApplication.builder()
                .applicantName("Ivan Ivanov")
                .applicantAge(30)
                .requestedAmount(new BigDecimal("5000"))
                .monthlyIncome(new BigDecimal("80000"))
                .build();

        LoanProcessingResult result = loanGateway.processApplication(application);

        assertThat(result.getDecision()).isEqualTo(LoanApplication.LoanStatus.REJECTED);
        assertThat(result.getProcessingLog()).contains("REJECTED during validation");
        assertThat(result.getProcessingLog()).doesNotContain("score");
    }
}
