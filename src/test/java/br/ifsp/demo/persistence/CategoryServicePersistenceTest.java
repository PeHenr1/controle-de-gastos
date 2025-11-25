package br.ifsp.demo.persistence;

import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
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
    private CategoryRepositoryPort repository;

    private String userId;

    private String rootId;
    private String childId;
    private String grandChildId;
    private String newRootId;
    private String nonExistentId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID().toString();

        Category rootCat = Category.root(userId, "Despesas");
        rootId = repository.save(rootCat).id();

        Category childCat = Category.child(userId, "Lazer", rootId);
        childId = repository.save(childCat).id();

        Category grandChildCat = Category.child(userId, "Cinema", childId);
        grandChildId = repository.save(grandChildCat).id();

        String rootPath = repository.findPathById(rootId, userId);
        String childNewPath = rootPath + "/" + childCat.name();
        repository.rename(childId, userId, childCat.name(), childNewPath);

        String childPath = repository.findPathById(childId, userId);
        String grandChildNewPath = childPath + "/" + grandChildCat.name();
        repository.rename(grandChildId, userId, grandChildCat.name(), grandChildNewPath);
    }

    @Test
    @DisplayName("Should Rename Root And Cascade Update Paths")
    void shouldRenameRootAndCascadeUpdatePaths() {
        String prefixoAntigo = "Despesas";
        String novoNome = "Gastos";
        String prefixoNovo = "Gastos";

        repository.rename(rootId, userId, novoNome, prefixoNovo);
        repository.updatePathPrefix(userId, prefixoAntigo + "/", prefixoNovo + "/");

        String pathRoot = repository.findPathById(rootId, userId);
        assertThat(pathRoot).isEqualTo(prefixoNovo);

        String pathChild = repository.findPathById(childId, userId);
        assertThat(pathChild).isEqualTo("Gastos/Lazer");

        String pathGrandChild = repository.findPathById(grandChildId, userId);
        assertThat(pathGrandChild).isEqualTo("Gastos/Lazer/Cinema");
    }

    @Test
    @DisplayName("Should Move Subtree And Cascade Update Paths")
    void shouldMoveSubtreeAndCascadeUpdatePaths() {
        String oldPath = "Despesas/Lazer";
        String newPath = "Receitas/Lazer";

        repository.move(childId, userId, newRootId, newPath);
        repository.updatePathPrefix(userId, oldPath + "/", newPath + "/");

        String pathChild = repository.findPathById(childId, userId);
        assertThat(pathChild).isEqualTo(newPath);

        String pathGrandChild = repository.findPathById(grandChildId, userId);
        assertThat(pathGrandChild).isEqualTo("Receitas/Lazer/Cinema");
    }

    @Test
    @DisplayName("Should Return True For Exists By User And Path When Path Exists")
    void shouldReturnTrueForExistsByUserAndPathWhenPathExists() {
        String existingPath = "Despesas/Lazer";

        boolean exists = repository.existsByUserAndPath(userId, existingPath);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should Return True For Children When Children Exists")
    void shouldReturnTrueForChildrenWhenChildrenExist() {
        boolean hasChildren = repository.hasChildren(rootId, userId);
        assertThat(hasChildren).isTrue();

        boolean leafHasChildren = repository.hasChildren(grandChildId, userId);
        assertThat(leafHasChildren).isFalse();
    }

    @Test
    @DisplayName("Should List All Categories Ordered By Path")
    void shouldListAllCategoriesOrderedByPath() {
        var categories = repository.findAllByUserOrdered(userId);

        assertThat(categories)
                .extracting("path")
                .containsExactly("Despesas", "Despesas/Lazer", "Despesas/Lazer/Cinema", "Receitas");
    }

    @Test
    @DisplayName("Should Rename Child And Cascade Update Paths")
    void shouldRenameChildAndCascadeUpdatePaths() {
        String oldChildPath = "Despesas/Lazer";
        String novoNome = "Entretenimento";
        String newChildPath = "Despesas/Entretenimento";

        repository.rename(childId, userId, novoNome, newChildPath);
        repository.updatePathPrefix(userId, oldChildPath + "/", newChildPath + "/");

        String pathChild = repository.findPathById(childId, userId);
        assertThat(pathChild).isEqualTo(newChildPath);

        String pathGrandChild = repository.findPathById(grandChildId, userId);
        assertThat(pathGrandChild).isEqualTo("Despesas/Entretenimento/Cinema");
    }

    @Test
    @DisplayName("Should Check Unique Name Per Parent")
    void shouldCheckUniqueNamePerParent() {
        boolean existsWithDifferentCase = repository.existsByUserAndParentAndNameNormalized(userId, rootId, "lazer");
        assertThat(existsWithDifferentCase).isTrue();

        boolean existsUnderDifferentParent = repository.existsByUserAndParentAndNameNormalized(userId, newRootId, "lazer");
        assertThat(existsUnderDifferentParent).isFalse();
    }

    @Test
    @DisplayName("Should Return Null When Finding Path For Non Existent Id")
    void shouldReturnNullWhenFindingPathForNonExistentId() {
        String path = repository.findPathById(nonExistentId, userId);

        assertThat(path).isNull();
    }
}