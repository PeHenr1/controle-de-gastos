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
    void shouldGenerateReportForAllCategoriesIncluding() {

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

    @Test
    @DisplayName("Should generate report only for category tree")
    void shouldGenerateReportOnlyForCategoryTree() {

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("40.00"),
                ExpenseType.DEBIT, "Restaurante",
                Instant.parse("2025-01-05T10:00:00Z"), catRoot.getId()
        ));

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("60.00"),
                ExpenseType.DEBIT, "Cinema",
                Instant.parse("2025-01-06T10:00:00Z"), catChild.getId()
        ));

        CategoryEntity other = catRepo.save(new CategoryEntity(
                null, userId, "Receitas", null, "Receitas"
        ));

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("500.00"),
                ExpenseType.CREDIT, "Salário",
                Instant.parse("2025-01-05T12:00:00Z"), other.getId()
        ));

        Report report = service.generateForCategoryTree(userId, start, end, catRoot.getId());

        assertThat(report.totalDebit()).isEqualByComparingTo("100.00");
        assertThat(report.totalCredit()).isZero();

        assertThat(report.items())
                .extracting(ReportItem::categoryPath)
                .containsExactly("Despesas", "Despesas/Lazer");
    }

    @Test
    @DisplayName(value = "Should ignore expenses with category not found")
    void shouldIgnoreExpensesWithCategoryNotFound() {

        CategoryEntity temp = catRepo.save(new CategoryEntity(
                null, userId, "Temp", null, "Temp"
        ));

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("80.00"),
                ExpenseType.DEBIT, "Compra",
                Instant.parse("2025-01-05T10:00:00Z"), catRoot.getId()
        ));

        String deletedId = temp.getId();
        catRepo.delete(temp);

        expenseRepo.save(new ExpenseEntity(
                null, userId, new BigDecimal("90.00"),
                ExpenseType.DEBIT, "Inválida",
                Instant.parse("2025-01-07T15:00:00Z"), deletedId
        ));

        Report report = service.generate(userId, start, end);

        assertThat(report.totalDebit()).isEqualByComparingTo("80.00");
        assertThat(report.items()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw error when period is invalid")
    void shouldThrowErrorWhenPeriodIsInvalid() {
        assertThatThrownBy(() ->
                service.generate(userId, end, start))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw error when category is invalid")
    void shouldThrowErrorWhenCategoryIsInvalid() {
        assertThatThrownBy(() ->
                service.generateForCategoryTree(userId, start, end, "xxx"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
