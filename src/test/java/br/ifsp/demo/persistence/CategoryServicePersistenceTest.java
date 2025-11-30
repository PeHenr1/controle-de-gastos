package br.ifsp.demo.persistence;

import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import br.ifsp.demo.infra.persistence.repo.CategoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class CategoryServicePersistenceTest {

    @Autowired
    private CategoryJpaRepository jpa;

    private String userId;
    private String rootId;
    private String childId;
    private String grandChildId;
    private String nonExistentId = "fake";

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID().toString();

        var root = new CategoryEntity(null, userId, "Despesas", null, "Despesas");
        rootId = jpa.save(root).getId();

        var child = new CategoryEntity(null, userId, "Lazer", rootId, "Despesas/Lazer");
        childId = jpa.save(child).getId();

        var grand = new CategoryEntity(null, userId, "Cinema", childId, "Despesas/Lazer/Cinema");
        grandChildId = jpa.save(grand).getId();
    }

    @Test
    @DisplayName("Should Rename Root And Cascade Update Paths")
    void shouldRenameRootAndCascadeUpdatePaths() {

        jpa.rename(rootId, userId, "Gastos", "Gastos");
        jpa.updatePathPrefix(userId, "Despesas/", "Gastos/");

        String pathRoot = jpa.findPath(rootId, userId);
        assertThat(pathRoot).isEqualTo("Gastos");

        String pathChild = jpa.findPath(childId, userId);
        assertThat(pathChild).isEqualTo("Gastos/Lazer");

        String pathGrandChild = jpa.findPath(grandChildId, userId);
        assertThat(pathGrandChild).isEqualTo("Gastos/Lazer/Cinema");
    }

    @Test
    @DisplayName("Should Move Subtree And Cascade Update Paths")
    void shouldMoveSubtreeAndCascadeUpdatePaths() {

        var newRoot = new CategoryEntity(null, userId, "Receitas", null, "Receitas");
        String newRootId = jpa.save(newRoot).getId();

        String oldPath = "Despesas/Lazer";
        String newPath = "Receitas/Lazer";

        jpa.move(childId, userId, newRootId, newPath);
        jpa.updatePathPrefix(userId, oldPath + "/", newPath + "/");

        String pathChild = jpa.findPath(childId, userId);
        assertThat(pathChild).isEqualTo(newPath);

        String pathGrandChild = jpa.findPath(grandChildId, userId);
        assertThat(pathGrandChild).isEqualTo("Receitas/Lazer/Cinema");
    }

    @Test
    @DisplayName("Should Return True For Exists By User And Path")
    void shouldReturnTrueForExistsByUserAndPath() {

        boolean exists = jpa.existsByUserAndPath(userId, "Despesas/Lazer");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should Return True For Children When They Exist")
    void shouldReturnTrueForChildren() {

        assertThat(jpa.hasChildren(rootId, userId)).isTrue();
        assertThat(jpa.hasChildren(grandChildId, userId)).isFalse();
    }

    @Test
    @DisplayName("Should List All Categories Ordered By Path")
    void shouldListAllCategoriesOrderedByPath() {

        var categories = jpa.findAllOrdered(userId);

        assertThat(categories)
                .extracting(CategoryEntity::getPath)
                .containsExactly(
                        "Despesas",
                        "Despesas/Lazer",
                        "Despesas/Lazer/Cinema"
                );
    }

    @Test
    @DisplayName("Should Rename Child And Cascade Update Paths")
    void shouldRenameChildAndCascadeUpdatePaths() {

        String oldPath = "Despesas/Lazer";
        String newPath = "Despesas/Entretenimento";

        jpa.rename(childId, userId, "Entretenimento", newPath);
        jpa.updatePathPrefix(userId, oldPath + "/", newPath + "/");

        assertThat(jpa.findPath(childId, userId)).isEqualTo(newPath);
        assertThat(jpa.findPath(grandChildId, userId))
                .isEqualTo("Despesas/Entretenimento/Cinema");
    }

    @Test
    @DisplayName("Should Check Unique Name Per Parent")
    void shouldCheckUniqueNamePerParent() {

        boolean existsSibling =
                jpa.existsSiblingByNormalized(userId, rootId, "lazer");

        assertThat(existsSibling).isTrue();

        boolean existsUnderDifferentParent =
                jpa.existsSiblingByNormalized(userId, null, "lazer");

        assertThat(existsUnderDifferentParent).isFalse();
    }

    @Test
    @DisplayName("Should Return Null For Nonexistent Id")
    void shouldReturnNullForNonexistentId() {

        assertThat(jpa.findPath(nonExistentId, userId)).isNull();
    }
}