package br.ifsp.demo.persistence;

import br.ifsp.demo.infra.persistence.entity.GoalEntity;
import br.ifsp.demo.infra.persistence.repo.GoalJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class GoalServicePersistenceTest {

    @Autowired
    private GoalJpaRepository repository;

    private String userId;
    private String categoryId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID().toString();
        categoryId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("Should Save Goal And Retrieve By Id")
    void shouldSaveGoalAndRetrieveById() {
        var entity = new GoalEntity(
                null,
                userId,
                categoryId,
                "2025-01",
                new BigDecimal("500.00")
        );

        var saved = repository.save(entity);

        assertThat(saved.getId()).isNotNull();

        Optional<GoalEntity> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLimitAmount()).isEqualTo("500.00");
        assertThat(found.get().getMonth()).isEqualTo("2025-01");
    }

    @Test
    @DisplayName("Should Find Monthly Goal By User, Category And Month")
    void shouldFindMonthlyGoal() {
        var entity = new GoalEntity(
                null,
                userId,
                categoryId,
                "2025-02",
                new BigDecimal("300")
        );

        repository.save(entity);
        var result = repository.findMonthly(userId, categoryId, "2025-02");

        assertThat(result).isPresent();
        assertThat(result.get().getLimitAmount()).isEqualByComparingTo("300");
    }

    @Test
    @DisplayName("Should Return Empty When Monthly Goal Not Found")
    void shouldReturnEmptyWhenMonthlyNotFound() {
        var result = repository.findMonthly(userId, categoryId, "2030-01");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should Allow Same Month But Different Category")
    void shouldAllowSameMonthDifferentCategory() {
        var goal1 = new GoalEntity(
                null,
                userId,
                categoryId,
                "2025-04",
                new BigDecimal("100")
        );

        var goal2 = new GoalEntity(
                null,
                userId,
                UUID.randomUUID().toString(),
                "2025-04",
                new BigDecimal("100")
        );

        repository.save(goal1);
        repository.save(goal2);

        assertThat(repository.findAll()).hasSize(2);
    }
}
