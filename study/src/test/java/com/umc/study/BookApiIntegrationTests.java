package com.umc.study;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.study.entity.Book;
import com.umc.study.entity.Category;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookApiIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.saveAndFlush(new Category("문학"));
    }

    @Test
    void listsAllBooksNewestFirstWithCategoryInOneQuery() throws Exception {
        bookRepository.saveAndFlush(new Book(category, "첫 번째 책", null));
        Book latest = bookRepository.saveAndFlush(new Book(category, "두 번째 책", "설명"));
        // Include a book that is unavailable; GET /books must still return it.
        entityManager.createNativeQuery("UPDATE book SET is_available = 0 WHERE book_id = :id")
                .setParameter("id", latest.getBookId()).executeUpdate();
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookId").value(latest.getBookId()))
                .andExpect(jsonPath("$[0].title").value("두 번째 책"))
                .andExpect(jsonPath("$[0].categoryName").value("문학"))
                .andExpect(jsonPath("$[0].isAvailable").value(false))
                .andExpect(jsonPath("$[0].category").doesNotExist())
                .andExpect(jsonPath("$[0].book_id").doesNotExist());
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void createsBookAndReturns201WithResponseDto() throws Exception {
        String result = mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryId", category.getCategoryId(), "title", "ORM으로 등록한 책", "description", "실습"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("문학"))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andReturn().getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(result);
        entityManager.flush();
        entityManager.clear();
        Book saved = bookRepository.findById(response.get("bookId").asLong()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("ORM으로 등록한 책");
        assertThat(saved.getDescription()).isEqualTo("실습");
        assertThat(saved.getCategory().getCategoryId()).isEqualTo(category.getCategoryId());
    }

    @Test
    void allowsMissingDescriptionAnd100CharacterTitle() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryId", category.getCategoryId(), "title", "가".repeat(100)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankTitlesWithoutSaving(String title) throws Exception {
        long before = bookRepository.count();
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("categoryId", category.getCategoryId(), "title", title))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        assertThat(bookRepository.count()).isEqualTo(before);
    }

    @Test
    void rejects101CharacterTitle() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("categoryId", category.getCategoryId(), "title", "가".repeat(101)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
        assertThat(bookRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"title\":\"책\"}", "{\"categoryId\":0,\"title\":\"책\"}", "{\"categoryId\":-1,\"title\":\"책\"}"})
    void rejectsMissingOrNonPositiveCategory(String body) throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.categoryId").exists());
        assertThat(bookRepository.count()).isZero();
    }

    @Test
    void rejectsUnknownCategoryWithoutSaving() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":9223372036854775807,\"title\":\"책\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        assertThat(bookRepository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{broken", "{\"categoryId\":\"oops\",\"title\":\"책\"}", "{\"categoryId\":1.5,\"title\":\"책\"}"})
    void rejectsMalformedJsonOrInvalidCategoryFormat(String body) throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        assertThat(bookRepository.count()).isZero();
    }

    @Test
    void searchesTitlesAndReturnsEmptyForNoMatch() throws Exception {
        bookRepository.saveAndFlush(new Book(category, "스프링 첫걸음", null));
        Book latest = bookRepository.saveAndFlush(new Book(category, "스프링 JPA", null));
        bookRepository.saveAndFlush(new Book(category, "자바 기초", null));
        entityManager.clear();
        mockMvc.perform(get("/books").param("keyword", " 스프링 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookId").value(latest.getBookId()));
        mockMvc.perform(get("/books").param("keyword", "없는제목"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/books").param("keyword", "   "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void preservesCategoryFilterEndpoint() throws Exception {
        Category other = categoryRepository.saveAndFlush(new Category("과학"));
        bookRepository.saveAndFlush(new Book(category, "문학 책", null));
        bookRepository.saveAndFlush(new Book(other, "과학 책", null));
        entityManager.clear();
        mockMvc.perform(get("/books/category/{categoryId}", category.getCategoryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("문학 책"));
    }
}
