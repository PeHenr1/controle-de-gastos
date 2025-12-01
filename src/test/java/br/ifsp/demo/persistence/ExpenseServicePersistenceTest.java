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
import java.util.List;
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

    @Test
    @DisplayName("Should Find Expenses By Period")
    void shouldFindByPeriod() {
        Instant now = Instant.now();

        var e1 = new ExpenseEntity(null, userId, new BigDecimal("10"), ExpenseType.DEBIT,
                "A1", now.minusSeconds(100), null);
        var e2 = new ExpenseEntity(null, userId, new BigDecimal("20"), ExpenseType.DEBIT,
                "A2", now.minusSeconds(50), null);

        repository.saveAll(List.of(e1, e2));

        var result = repository.findByUserAndPeriod(userId, now.minusSeconds(120), now);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should Return True When Exists By User And Category")
    void shouldReturnTrueWhenExistsByUserAndCategory() {
        var categoryId = UUID.randomUUID().toString();

        var e = new ExpenseEntity();
        e.setUserId(userId);
        e.setAmount(new BigDecimal("15"));
        e.setType(ExpenseType.DEBIT);
        e.setDescription("Teste");
        e.setTimestamp(Instant.now());
        e.setCategoryId(categoryId);

        repository.save(e);

        boolean exists = repository.existsByUserAndCategory(userId, categoryId);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should Return False When Category Does Not Match User")
    void shouldReturnFalseWhenCategoryDoesNotMatchUser() {
        boolean exists = repository.existsByUserAndCategory(userId, UUID.randomUUID().toString());
        assertThat(exists).isFalse();
    }
}
