package com.example.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.TransactionStatus;
import com.example.ledger.domain.TransactionType;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.web.dto.TransactionRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String USER_ID = "user-123";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InsightsService insightsService;

    @InjectMocks
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest(
                "Salary",
                TransactionType.CREDIT,
                BigDecimal.valueOf(1500),
                "Monthly salary",
                "income",
                "2024-06-01"
        );
    }

    @Test
    void createShouldPersistTransactionForUserAndRecalculateInsights() {
        Transaction persisted = new Transaction(
                "tx-1",
                USER_ID,
                request.type(),
                request.amount(),
                request.title(),
                request.description(),
                request.category(),
                LocalDate.parse(request.date()),
                TransactionStatus.POSTED,
                "USD",
                Instant.parse("2024-06-05T10:00:00Z")
        );
        when(transactionRepository.save(eq(USER_ID), any(Transaction.class))).thenReturn(persisted);

        Transaction result = transactionService.create(USER_ID, request);

        assertThat(result).isSameAs(persisted);
        verify(transactionRepository).save(eq(USER_ID), transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getAmount()).isEqualByComparingTo(request.amount());
        assertThat(saved.getType()).isEqualTo(request.type());
        assertThat(saved.getTitle()).isEqualTo(request.title());
        assertThat(saved.getDescription()).isEqualTo(request.description());
        assertThat(saved.getCategory()).isEqualTo(request.category());
        assertThat(saved.getEventDate()).isEqualTo(LocalDate.parse(request.date()));
        verify(insightsService).recalculate(USER_ID);
    }

    @Test
    void updateShouldApplyChangesAndRecalculateInsights() {
        String transactionId = "tx-5";
        Transaction existing = new Transaction(
                transactionId,
                USER_ID,
                TransactionType.DEBIT,
                BigDecimal.valueOf(100),
                "Groceries",
                "Weekly shopping",
                "food",
                LocalDate.parse("2024-05-28"),
                TransactionStatus.POSTED,
                "USD",
                Instant.parse("2024-05-28T10:00:00Z")
        );
        when(transactionRepository.findById(USER_ID, transactionId)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(eq(USER_ID), any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(1));

        Optional<Transaction> result = transactionService.update(USER_ID, transactionId, request);

        assertThat(result).isPresent();
        Transaction updated = result.orElseThrow();
        assertThat(updated.getId()).isEqualTo(transactionId);
        assertThat(updated.getType()).isEqualTo(request.type());
        assertThat(updated.getAmount()).isEqualByComparingTo(request.amount());
        assertThat(updated.getTitle()).isEqualTo(request.title());
        assertThat(updated.getDescription()).isEqualTo(request.description());
        assertThat(updated.getCategory()).isEqualTo(request.category());
        assertThat(updated.getEventDate()).isEqualTo(LocalDate.parse(request.date()));

        verify(transactionRepository).save(eq(USER_ID), transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getUserId()).isEqualTo(USER_ID);
        verify(insightsService).recalculate(USER_ID);
    }

    @Test
    void updateShouldReturnEmptyWhenTransactionDoesNotExist() {
        when(transactionRepository.findById(USER_ID, "missing")).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.update(USER_ID, "missing", request);

        assertThat(result).isEmpty();
        verify(transactionRepository, never()).save(eq(USER_ID), any(Transaction.class));
        verify(insightsService, never()).recalculate(USER_ID);
    }

    @Test
    void deleteShouldRecalculateInsightsOnlyWhenDeletionSucceeds() {
        when(transactionRepository.delete(USER_ID, "tx-7")).thenReturn(true);
        when(transactionRepository.delete(USER_ID, "tx-8")).thenReturn(false);

        boolean deleted = transactionService.delete(USER_ID, "tx-7");
        boolean notFound = transactionService.delete(USER_ID, "tx-8");

        assertThat(deleted).isTrue();
        assertThat(notFound).isFalse();
        verify(insightsService, times(1)).recalculate(USER_ID);
    }
}
