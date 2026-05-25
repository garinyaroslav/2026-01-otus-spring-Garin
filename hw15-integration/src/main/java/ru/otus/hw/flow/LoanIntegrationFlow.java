package ru.otus.hw.flow;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.EnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.RouterSpec;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.models.LoanApplication;
import ru.otus.hw.service.CreditBureauService;
import ru.otus.hw.service.LoanScoringService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoanIntegrationFlow {

    private final CreditBureauService creditBureauService;

    private final LoanScoringService loanScoringService;

    @Bean
    public MessageChannel loanApplicationChannel() {
        return MessageChannels.direct("loanApplicationChannel").getObject();
    }

    @Bean
    public MessageChannel batchLoanChannel() {
        return MessageChannels.direct("batchLoanChannel").getObject();
    }

    @Bean
    public MessageChannel approvedChannel() {
        return MessageChannels.direct("approvedChannel").getObject();
    }

    @Bean
    public MessageChannel rejectedChannel() {
        return MessageChannels.direct("rejectedChannel").getObject();
    }

    @Bean
    public MessageChannel partialChannel() {
        return MessageChannels.direct("partialChannel").getObject();
    }

    @Bean
    public IntegrationFlow loanProcessingFlow() {
        return IntegrationFlow
                .from("loanApplicationChannel")
                .transform(LoanApplication.class, this::initializeApplication)
                .filter(LoanApplication.class, this::validateApplication,
                        f -> f.discardFlow(validationDiscardFlow()))
                .enrich(enrichmentConfig())
                .handle(LoanApplication.class, this::performScoring).<LoanApplication, LoanApplication.LoanStatus>route(
                        LoanApplication::getStatus,
                        this::configureRouter)
                .transform(LoanApplication.class, loanScoringService::toResult)
                .get();
    }

    private LoanApplication initializeApplication(LoanApplication app) {
        if (app.getId() == null) {
            app.setId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        app.setStatus(LoanApplication.LoanStatus.VALIDATING);
        app.setProcessingLog("SUBMITTED");
        log.info("Step 1 [Transformer] Initializing a request #{} от '{}'",
                app.getId(), app.getApplicantName());
        return app;
    }

    private boolean validateApplication(LoanApplication app) {
        boolean valid = app.getApplicantAge() >= 18
                && app.getApplicantAge() <= 75
                && app.getRequestedAmount() != null
                && app.getRequestedAmount().compareTo(BigDecimal.valueOf(10_000)) >= 0
                && app.getRequestedAmount().compareTo(BigDecimal.valueOf(10_000_000)) <= 0;
        if (!valid) {
            log.warn("Step 2 [Filter] Request #{} was rejected during validation.", app.getId());
        } else {
            appendLog(app, "Validation passed");
            log.info("Step 2 [Filter] Request #{} has been validated", app.getId());
        }
        return valid;
    }

    private IntegrationFlow validationDiscardFlow() {
        return f -> f
                .transform(LoanApplication.class, app -> {
                    app.setStatus(LoanApplication.LoanStatus.REJECTED);
                    app.setRejectionReason("Basic validation failed: age or amount out of range");
                    appendLog(app, "REJECTED during validation");
                    return app;
                })
                .transform(LoanApplication.class, loanScoringService::toResult);
    }

    private Consumer<EnricherSpec> enrichmentConfig() {
        return e -> e
                .requestPayloadExpression("payload")
                .requestChannel(enrichmentRequestChannel())
                .replyChannel(enrichmentReplyChannel())
                .propertyExpression("creditScore", "payload.creditScore")
                .propertyExpression("status", "payload.status")
                .propertyExpression("processingLog", "payload.processingLog");
    }

    private LoanApplication performScoring(LoanApplication app, MessageHeaders headers) {
        log.info("Step 4 [Scoring] Calculating the decision for application #{}", app.getId());
        return loanScoringService.makeDecision(app);
    }

    private void configureRouter(RouterSpec<LoanApplication.LoanStatus, ?> router) {
        router
                .subFlowMapping(LoanApplication.LoanStatus.APPROVED,
                        sf -> sf.handle((app, h) -> {
                            log.info("Step 5 [Router → subflow APPROVED] #{}",
                                    ((LoanApplication) app).getId());
                            return app;
                        }))
                .subFlowMapping(LoanApplication.LoanStatus.PARTIALLY_APPROVED,
                        sf -> sf.handle((app, h) -> {
                            log.info("Step 5 [Router → subflow PARTIAL] #{}",
                                    ((LoanApplication) app).getId());
                            return app;
                        }))
                .subFlowMapping(LoanApplication.LoanStatus.REJECTED,
                        sf -> sf.handle((app, h) -> {
                            log.info("Step 5 [Router → subflow REJECTED] #{}",
                                    ((LoanApplication) app).getId());
                            return app;
                        }))
                .defaultOutputToParentFlow();
    }

    @Bean
    public MessageChannel enrichmentRequestChannel() {
        return MessageChannels.direct("enrichmentRequestChannel").getObject();
    }

    @Bean
    public MessageChannel enrichmentReplyChannel() {
        return MessageChannels.direct("enrichmentReplyChannel").getObject();
    }

    @Bean
    public IntegrationFlow enrichmentSubFlow() {
        return IntegrationFlow
                .from("enrichmentRequestChannel")
                .handle(LoanApplication.class, (app, headers) -> {
                    log.info("Step 3 [Enricher subflow] Request credit score for '{}'",
                            app.getApplicantName());
                    app.setStatus(LoanApplication.LoanStatus.ENRICHING);
                    return creditBureauService.enrichWithCreditScore(app);
                })
                .channel("enrichmentReplyChannel")
                .get();
    }

    @Bean
    public IntegrationFlow batchLoanProcessingFlow() {
        return IntegrationFlow
                .from("batchLoanChannel")
                .split()
                .channel("loanApplicationChannel")
                .get();
    }

    private void appendLog(LoanApplication app, String entry) {
        String current = app.getProcessingLog() != null ? app.getProcessingLog() : "";
        app.setProcessingLog(current + " → " + entry);
    }
}
