package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;
import tr.com.eaaslan.library.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books in the library")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book", description = "Creates a new book with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Book already exists")
    })
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody
            @Parameter(description = "Book creation request", required = true)
            BookCreateRequest bookCreateRequest) {
        BookResponse createdBook = bookService.createBook(bookCreateRequest);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @Operation(summary = "Get book by ID", description = "Returns book details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Get book by ISBN", description = "Returns book details by ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(
            @Parameter(description = "Book ISBN", required = true)
            @PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @Operation(summary = "Get all books", description = "Returns a paginated list of books")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of books retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "title") String sortBy) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size, sortBy));
    }

    @Operation(summary = "Update book", description = "Updates an existing book with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id,
            @Valid @RequestBody
            @Parameter(description = "Book update request", required = true)
            BookUpdateRequest bookUpdateRequest) {
        return ResponseEntity.ok(bookService.updateBook(id, bookUpdateRequest));
    }

    @Operation(summary = "Delete book", description = "Deletes a book by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book deleted successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BookResponse> deleteBook(
            @Parameter(description = "Book ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    @Operation(summary = "Search books by title", description = "Returns books with titles containing the search term")
    @GetMapping("/search/title/{title}")
    public ResponseEntity<List<BookResponse>> searchBooksByTitle(
            @Parameter(description = "Title search term", required = true) @PathVariable String title,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByTitle(title, page, size));
    }

    @Operation(summary = "Search books by author", description = "Returns books by authors containing the search term")
    @GetMapping("/search/author/{author}")
    public ResponseEntity<List<BookResponse>> searchBooksByAuthor(
            @Parameter(description = "Author search term", required = true) @PathVariable String author,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByAuthor(author, page, size));
    }

    @Operation(summary = "Search books by genre", description = "Returns books in a specific genre")
    @GetMapping("/search/genre/{genre}")
    public ResponseEntity<List<BookResponse>> searchBooksByGenre(
            @Parameter(description = "Genre name", required = true) @PathVariable String genre,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooksByGenre(genre, page, size));
    }

    @Operation(summary = "Get available books", description = "Returns all books that are available for borrowing")
    @GetMapping("/available")
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getAvailableBooks(page, size));
    }
}