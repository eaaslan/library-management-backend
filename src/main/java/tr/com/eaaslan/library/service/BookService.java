package tr.com.eaaslan.library.service;

import org.springframework.stereotype.Service;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.BookResponse;
import tr.com.eaaslan.library.model.dto.BookUpdateRequest;
import tr.com.eaaslan.library.repository.BookRepository;

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

    List<BookResponse> searchBooksByGenre(Genre genre, int page, int size);

    List<BookResponse> getAvailableBooks();
}

