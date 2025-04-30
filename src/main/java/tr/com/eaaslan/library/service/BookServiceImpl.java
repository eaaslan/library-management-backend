package tr.com.eaaslan.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.BookResponse;
import tr.com.eaaslan.library.model.dto.BookUpdateRequest;
import tr.com.eaaslan.library.model.mapper.BookMapper;
import tr.com.eaaslan.library.repository.BookRepository;

import java.util.List;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {
        bookRepository.findByIsbn(bookCreateRequest.isbn()).ifPresent(book -> {
            throw new RuntimeException("Book already exists");
        });
        Book book = bookMapper.toEntity(bookCreateRequest);
        log.info("Book created: {}", book);
        bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        log.info("Fetching book with ID: {}", id);
        return bookMapper.toResponse(book);
    }

    //todo create specific exception
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new RuntimeException("Book not found"));
        log.info("Fetching book with ISBN: {}", isbn);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks(int page, int size, String sortBy) {
        log.info("Fetching all books - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.getContent().stream().map(bookMapper::toResponse).toList();
    }

    //todo create specific exception
    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest bookUpdateRequest) {
        log.info("Updating book with ID: {}", id);
        Book book = bookRepository.getBooksById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        bookMapper.updateEntity(bookUpdateRequest, book);
        bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse deleteBook(Long id) {
        Book book = bookRepository.getBooksById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        log.info("Deleting book with ID: {}", id);
        bookRepository.delete(book);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByTitle(String title, int page, int size) {
        log.info("Searching books by title: {}", title);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        return bookPage.getContent().stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByAuthor(String author, int page, int size) {
        log.info("Searching books by author: {}", author);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        return bookPage.getContent().stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksByGenre(Genre genre, int page, int size) {
        log.info("Searching books by genre: {}", genre);
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByGenre(genre, pageable);
        return bookPage.getContent().stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAvailableBooks() {
        log.info("Fetching available books");
        return bookRepository.findAllByAvailableTrue().stream().map(bookMapper::toResponse).toList();
    }
}
