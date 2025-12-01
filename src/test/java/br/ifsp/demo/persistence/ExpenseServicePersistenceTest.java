package br.ifsp.demo.persistence;

import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.infra.persistence.repo.ExpenseJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class ExpenseServicePersistenceTest {

    @Autowired
    private ExpenseJpaRepository repository;

    private String userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Should Save Expense And Retrieve By Id")
    void shouldSaveExpenseAndRetrieveById() {
        var entity = new ExpenseEntity();
        entity.setUserId(userId);
        entity.setAmount(new BigDecimal("50.00"));
        entity.setType(ExpenseType.DEBIT);
        entity.setDescription("Cinema");
        entity.setTimestamp(Instant.now());
        entity.setCategoryId(null);

        var saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();

        var found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Cinema");
    }
}
