package br.ifsp.demo.integration;

import br.ifsp.demo.controller.CategoryController;
import br.ifsp.demo.domain.model.Category;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;
import br.ifsp.demo.domain.service.CategoryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Tag("ApiTest")
@Tag("IntegrationTest")
class CategoryControllerUnitTest {

    private static final String BASE_URL = "/api/v1/categories";
    private static final String VALID_USER_ID = IntegrationTestUtils.VALID_USER_ID;
    private static final String CATEGORY_ID = "CAT-123";
    private static final String PARENT_ID = "PARENT-456";

    private static final String ROOT_PATH = "Root Category";
    private static final String CHILD_PATH = "Parent Category/Child Category";

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CategoryController categoryController;

    record ValidCreateRequest(String name) {}

    private CategoryNode createMockCategoryNode(String id, String userId, String name, String parentId, String path) {
        return new CategoryNode(id, userId, name, parentId, path);
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    // --- Testes para POST /api/v1/categories (createRoot) ---
    @Test
    @DisplayName("Should Return 201 And Category Node When Valid Payload - Root")
    void shouldReturn201AndCategoryNodeWhenValidPayloadRoot() throws Exception {
        var requestName = "Root Category";
        var request = new ValidCreateRequest(requestName);
        var savedCategory = Category.root(VALID_USER_ID, requestName).withId(CATEGORY_ID);
        var categoryNode = createMockCategoryNode(CATEGORY_ID, VALID_USER_ID, requestName, null, ROOT_PATH);

        when(categoryService.create(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepositoryPort.findNodeById(eq(CATEGORY_ID), eq(VALID_USER_ID))).thenReturn(categoryNode);

        mockMvc.perform(post(BASE_URL)
                        .headers(IntegrationTestUtils.createValidHeaders())
                        .content(IntegrationTestUtils.asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.name").value(requestName))
                .andExpect(jsonPath("$.parentId").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.path").value(ROOT_PATH));

        verify(categoryService).create(any(Category.class));
        verify(categoryRepositoryPort).findNodeById(eq(CATEGORY_ID), eq(VALID_USER_ID));
    }

    // --- Testes para POST /api/v1/categories/{parentId}/children (createChild) ---
    @Test
    @DisplayName("Should Return 204 When Valid Payload Rename - Children")
    void shouldReturn204AndCategoryNodeWhenValidPayloadChildren() throws Exception {
        var requestName = "Child Category";
        var request = new ValidCreateRequest(requestName);

        var savedCategory = Category.child(VALID_USER_ID, requestName, PARENT_ID).withId(CATEGORY_ID);
        var categoryNode = createMockCategoryNode(CATEGORY_ID, VALID_USER_ID, requestName, PARENT_ID, CHILD_PATH);

        when(categoryService.create(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepositoryPort.findNodeById(eq(CATEGORY_ID), eq(VALID_USER_ID))).thenReturn(categoryNode);

        mockMvc.perform(post(BASE_URL + "/" + PARENT_ID + "/children")
                        .headers(IntegrationTestUtils.createValidHeaders())
                        .content(IntegrationTestUtils.asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.name").value(requestName))
                .andExpect(jsonPath("$.parentId").value(PARENT_ID))
                .andExpect(jsonPath("$.path").value(CHILD_PATH));

        verify(categoryService).create(any(Category.class));
        verify(categoryRepositoryPort).findNodeById(eq(CATEGORY_ID), eq(VALID_USER_ID));
    }


    // --- Testes para DELETE /api/v1/categories/{id} (delete) ---
    @Test
    @DisplayName("Should Return 204 When Successful With No Content")
    void shouldReturn204WhenSuccessfulWithNoContent() throws Exception {
        doNothing().when(categoryService).delete(eq(CATEGORY_ID), eq(VALID_USER_ID));

        mockMvc.perform(delete(BASE_URL + "/" + CATEGORY_ID)
                        .headers(IntegrationTestUtils.createValidHeaders()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).delete(eq(CATEGORY_ID), eq(VALID_USER_ID));
    }

    // --- Testes para GET /api/v1/categories (list) ---
    @Test
    @DisplayName("Should Return 200 And List Of Category Nodes")
    void shouldReturn200AndListOfCategoryNodes() throws Exception {
        var node1 = createMockCategoryNode("ID1", VALID_USER_ID, "Node 1", null, "Node 1");
        var node2 = createMockCategoryNode("ID2", VALID_USER_ID, "Node 2", null, "Node 2");

        List<CategoryNode> mockList = Arrays.asList(node1, node2);

        when(categoryService.listOrdered(eq(VALID_USER_ID))).thenReturn(mockList);

        mockMvc.perform(get(BASE_URL)
                        .headers(IntegrationTestUtils.createValidHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("ID1"))
                .andExpect(jsonPath("$[1].name").value("Node 2"));

        verify(categoryService).listOrdered(eq(VALID_USER_ID));
    }
}