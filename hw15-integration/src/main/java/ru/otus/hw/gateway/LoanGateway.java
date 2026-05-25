package ru.otus.hw.gateway;

import java.util.List;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import ru.otus.hw.models.LoanApplication;
import ru.otus.hw.models.LoanProcessingResult;

@MessagingGateway
public interface LoanGateway {

    @Gateway(requestChannel = "loanApplicationChannel")
    LoanProcessingResult processApplication(LoanApplication application);

    @Gateway(requestChannel = "batchLoanChannel")
    List<LoanProcessingResult> processBatch(List<LoanApplication> applications);

}
