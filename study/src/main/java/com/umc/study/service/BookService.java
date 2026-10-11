package com.umc.study.service;

import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.entity.Book;
import com.umc.study.entity.Category;
import com.umc.study.exception.CategoryNotFoundException;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public List<BookResponse> getBooks(String keyword) {
        List<Book> books = keyword == null || keyword.isBlank()
                ? bookRepository.findAllByOrderByBookIdDesc()
                : bookRepository.findByTitleContainingIgnoreCaseOrderByBookIdDesc(keyword.trim());
        return books.stream().map(BookResponse::from).toList();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        Book book = new Book(category, request.title(), request.description());
        return BookResponse.from(bookRepository.save(book));
    }

    public List<BookResponse> getBooksByCategoryId(Long categoryId) {
        return bookRepository.findByCategory_CategoryIdOrderByBookIdDesc(categoryId)
                .stream().map(BookResponse::from).toList();
    }
}
