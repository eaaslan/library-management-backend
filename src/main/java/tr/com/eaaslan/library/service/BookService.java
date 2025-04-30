package tr.com.eaaslan.library.service;

import org.springframework.stereotype.Service;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.repository.BookRepository;

import java.util.List;

public interface BookService {

    Book createBook(Book book);

    Book getBookById(Long id);

    Book getBookByIsbn(String isbn);

    List<Book> getAllBooks(int page, int size, String sortBy);

    Book updateBook(Long id, Book book);

    void deleteBook(Long id);

    List<Book> searchBooksByTitle(String title, int page, int size);

    List<Book> searchBooksByAuthor(String author, int page, int size);

    List<Book> searchBooksByGenre(Genre genre, int page, int size);

    List<Book> getAvailableBooks();
}

