package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;

import java.util.List;

public interface BookService {

    BookResponse createBook(BookCreateRequest book);

    BookResponse getBookById(Long id);

    BookResponse getBookByIsbn(String isbn);

    List<BookResponse> getAllBooks(int page, int size, String sortBy);

    BookResponse updateBook(Long id, BookUpdateRequest bookUpdateRequest);

    BookResponse deleteBook(Long id);

    List<BookResponse> searchBooksByTitle(String title, int page, int size);

    List<BookResponse> searchBooksByAuthor(String author, int page, int size);

    List<BookResponse> searchBooksByGenre(String genre, int page, int size);

    Page<BookResponse> getAvailableBooks(int page, int size);
}

