package mongoHackathon.Main.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import mongoHackathon.Main.exception.*;
import mongoHackathon.Main.model.Transaction;
import mongoHackathon.Main.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final MongoTemplate mongoTemplate;
    private final MeterRegistry meterRegistry;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              MongoTemplate mongoTemplate,
                              MeterRegistry meterRegistry) {
        this.transactionRepository = transactionRepository;
        this.mongoTemplate = mongoTemplate;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    @CacheEvict(value = "transactions", allEntries = true)
    public ResponseEntity<Transaction> createTransaction(Transaction transaction) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateTransaction(transaction);

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

            // Record metrics
            meterRegistry.counter("transactions.created",
                            "type", transaction.getType().toString(),
                            "status", transaction.getStatus().toString())
                    .increment();

            timer.stop(meterRegistry.timer("transaction.creation.time"));
            return ResponseEntity.ok(savedTransaction);
        } catch (TransactionValidationException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating transaction: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Cacheable(value = "transactions", key = "#id")
    public ResponseEntity<Transaction> getTransactionById(String id) {
        try {
            return transactionRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new TransactionNotFoundException(id));
        } catch (TransactionNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Page<Transaction>> searchTransactions(
            Map<String, String> filters, Pageable pageable) {
        try {
            Query query = new Query().with(pageable);

            filters.forEach((key, value) -> {
                switch (key) {
                    case "type":
                        query.addCriteria(Criteria.where("type").is(value));
                        break;
                    case "status":
                        query.addCriteria(Criteria.where("status").is(value));
                        break;
                    case "minAmount":
                        query.addCriteria(Criteria.where("amount").gte(new BigDecimal(value)));
                        break;
                    case "maxAmount":
                        query.addCriteria(Criteria.where("amount").lte(new BigDecimal(value)));
                        break;
                    case "dateFrom":
                        query.addCriteria(Criteria.where("createdAt").gte(LocalDateTime.parse(value)));
                        break;
                    case "dateTo":
                        query.addCriteria(Criteria.where("createdAt").lte(LocalDateTime.parse(value)));
                        break;
                }
            });

            long total = mongoTemplate.count(query, Transaction.class);
            List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

            return ResponseEntity.ok(new PageImpl<>(transactions, pageable, total));
        } catch (Exception e) {
            log.error("Error searching transactions: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Transactional
    public ResponseEntity<List<Transaction>> batchCreateTransactions(List<Transaction> transactions) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            transactions.forEach(this::validateTransaction);

            List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

            // Record metrics
            meterRegistry.counter("transactions.batch.created").increment(transactions.size());

            timer.stop(meterRegistry.timer("transaction.batch.creation.time"));
            return ResponseEntity.ok(savedTransactions);
        } catch (Exception e) {
            log.error("Error in batch transaction creation: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new TransactionValidationException("Transaction cannot be null");
        }

        // Validate amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException("Transaction amount must be positive");
        }

        // Validate currency
        if (transaction.getCurrency() == null || transaction.getCurrency().trim().isEmpty()) {
            throw new TransactionValidationException("Currency is required");
        }

        // Validate account
        if (transaction.getAccountId() == null || transaction.getAccountId().trim().isEmpty()) {
            throw new TransactionValidationException("Account ID is required");
        }

        // Validate transaction type
        if (transaction.getType() == null) {
            throw new TransactionValidationException("Transaction type is required");
        }
        // Validate status etc.
    }
}

       
