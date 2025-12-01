package br.ifsp.demo.persistence;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.service.ReportService;
import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import br.ifsp.demo.infra.persistence.repo.CategoryJpaRepository;
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
class ReportServicePersistenceTest {

    @Autowired
    private ReportService service;

    @Autowired
    private ExpenseJpaRepository expenseRepo;

    @Autowired
    private CategoryJpaRepository catRepo;

    private String userId;

    private CategoryEntity catRoot;
    private CategoryEntity catChild;

    private Instant start;
    private Instant end;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID().toString();
        start = Instant.parse("2025-01-01T00:00:00Z");
        end   = Instant.parse("2025-01-31T23:59:59Z");

        catRoot = catRepo.save(new CategoryEntity(
                null, userId, "Despesas", null, "Despesas"
        ));

        catChild = catRepo.save(new CategoryEntity(
                null, userId, "Lazer", catRoot.getId(), "Despesas/Lazer"
        ));
    }

    @Test
    @DisplayName("Should generate report for all categories")
    void ShouldGenerateReportForAllCategoriesIncluding() {

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("100.00"),
                ExpenseType.DEBIT, "Sem categoria",
                Instant.parse("2025-01-10T10:00:00Z"), null
        ));

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("50.00"),
                ExpenseType.DEBIT, "Lanche",
                Instant.parse("2025-01-15T12:00:00Z"), catRoot.getId()
        ));

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("30.00"),
                ExpenseType.CREDIT, "Promoção",
                Instant.parse("2025-01-20T12:00:00Z"), catChild.getId()
        ));

        Report report = service.generate(userId, start, end);

        assertThat(report.totalDebit()).isEqualByComparingTo("150.00");
        assertThat(report.totalCredit()).isEqualByComparingTo("30.00");
        assertThat(report.balance()).isEqualByComparingTo("-120.00");

        assertThat(report.items())
                .extracting(ReportItem::categoryPath)
                .containsExactly(
                        "Despesas",
                        "Despesas/Lazer",
                        "Sem categoria"
                );
    }
}
