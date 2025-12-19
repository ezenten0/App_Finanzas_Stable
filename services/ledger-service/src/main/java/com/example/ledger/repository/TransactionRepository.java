package com.example.ledger.repository;

import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.TransactionStatus;
import com.example.ledger.domain.TransactionType;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepository.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Firestore firestore;

    public TransactionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Transaction> findAllForUser(String userId) {
        try {
            Query query = userTransactions(userId).orderBy("createdAt", Query.Direction.DESCENDING);
            return mapToTransactions(query.get().get().getDocuments());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading transactions", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error reading transactions from Firestore", e);
        }
    }

    public Optional<Transaction> findById(String userId, String id) {
        try {
            DocumentSnapshot snapshot = userTransactions(userId).document(id).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            return Optional.of(fromDocument(snapshot));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading transaction", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error reading transaction from Firestore", e);
        }
    }

    public Transaction save(String userId, Transaction transaction) {
        try {
            DocumentReference reference = transaction.getId() == null
                    ? userTransactions(userId).document()
                    : userTransactions(userId).document(transaction.getId());

            String documentId = reference.getId();
            transaction.setId(documentId);
            transaction.setUserId(userId);
            if (transaction.getCreatedAt() == null) {
                transaction.setCreatedAt(Instant.now());
            }

            WriteResult result = reference.set(toDocument(userId, transaction)).get();
            log.debug("Persisted transaction {} at {}", documentId, result.getUpdateTime());
            return transaction;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving transaction", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error saving transaction to Firestore", e);
        }
    }

    public boolean delete(String userId, String id) {
        try {
            DocumentReference reference = userTransactions(userId).document(id);
            if (!reference.get().get().exists()) {
                return false;
            }
            reference.delete().get();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while deleting transaction", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error deleting transaction from Firestore", e);
        }
    }

    private CollectionReference userTransactions(String userId) {
        return firestore.collection("users").document(userId).collection("transactions");
    }

    private List<Transaction> mapToTransactions(List<QueryDocumentSnapshot> documents) {
        List<Transaction> transactions = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            transactions.add(fromDocument(document));
        }
        return transactions;
    }

    private Transaction fromDocument(DocumentSnapshot document) {
        Transaction transaction = new Transaction();
        transaction.setId(document.getId());
        transaction.setUserId(document.getString("userId"));
        transaction.setTitle(document.getString("title"));
        transaction.setDescription(document.getString("description"));
        transaction.setCategory(document.getString("category"));
        transaction.setCurrency(document.getString("currency"));

        String eventDate = document.getString("eventDate");
        if (eventDate != null) {
            transaction.setEventDate(LocalDate.parse(eventDate, DATE_FORMATTER));
        }

        String type = document.getString("type");
        if (type != null) {
            transaction.setType(TransactionType.valueOf(type));
        }

        Double amount = document.getDouble("amount");
        if (amount != null) {
            transaction.setAmount(BigDecimal.valueOf(amount));
        }

        String status = document.getString("status");
        if (status != null) {
            transaction.setStatus(TransactionStatus.valueOf(status));
        }

        Timestamp createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toInstant());
        }

        return transaction;
    }

    private Map<String, Object> toDocument(String userId, Transaction transaction) {
        return Map.of(
                "userId", userId,
                "title", transaction.getTitle(),
                "description", transaction.getDescription(),
                "category", transaction.getCategory(),
                "type", transaction.getType().name(),
                "amount", transaction.getAmount().doubleValue(),
                "eventDate", transaction.getEventDate().format(DATE_FORMATTER),
                "status", transaction.getStatus().name(),
                "currency", transaction.getCurrency(),
                "createdAt", Timestamp.from(transaction.getCreatedAt())
        );
    }
}
