package tr.com.eaaslan.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.BookResponse;
import tr.com.eaaslan.library.model.dto.BookUpdateRequest;
import tr.com.eaaslan.library.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest bookCreateRequest) {
        BookResponse createdBook = bookService.createBook(bookCreateRequest);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "title") String sortBy) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size, sortBy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest bookUpdateRequest) {
        return ResponseEntity.ok(bookService.updateBook(id, bookUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BookResponse> deleteBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    @GetMapping("/search/title/{title}")
    public ResponseEntity<List<BookResponse>> searchBooksByTitle(
            @PathVariable String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByTitle(title, page, size));
    }

    @GetMapping("/search/author/{author}")
    public ResponseEntity<List<BookResponse>> searchBooksByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByAuthor(author, page, size));
    }

    @GetMapping("/search/genre/{genre}")
    public ResponseEntity<List<BookResponse>> searchBooksByGenre(
            @PathVariable Genre genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByGenre(genre, page, size));
    }

    @GetMapping("/available")
    public ResponseEntity<List<BookResponse>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }
}