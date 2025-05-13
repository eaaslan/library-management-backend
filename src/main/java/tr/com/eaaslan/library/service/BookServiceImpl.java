package tr.com.eaaslan.library.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.exception.BusinessRuleException;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Borrowing;
import tr.com.eaaslan.library.model.BorrowingStatus;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.book.BookResponse;
import tr.com.eaaslan.library.model.dto.book.BookUpdateRequest;
import tr.com.eaaslan.library.model.mapper.BookMapper;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BorrowingRepository borrowingRepository;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper, BorrowingRepository borrowingRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.borrowingRepository = borrowingRepository;
    }

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {
        bookRepository.findByIsbn(bookCreateRequest.isbn()).ifPresent(book -> {
            throw new ResourceAlreadyExistException("Book", "ISBN", bookCreateRequest.isbn());
        });
        Book book = bookMapper.toEntity(bookCreateRequest);
        log.info("Book created: {}", book);
        bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", "ID", id));
        log.info("Fetching book with ID: {}", id);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new ResourceNotFoundException("Book", "ISBN", isbn));
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

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest bookUpdateRequest) {
        log.info("Updating book with ID: {}", id);
        Book book = bookRepository.getBooksById(id).orElseThrow(() -> new ResourceNotFoundException("Book", "ID", id));
        bookMapper.updateEntity(bookUpdateRequest, book);
        bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    public BookResponse deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "ID", id));

        List<Borrowing> activeBorrowings = borrowingRepository.findByBookIdAndStatus(id, BorrowingStatus.ACTIVE);

        if (!activeBorrowings.isEmpty()) {
            throw new BusinessRuleException("Cannot delete book: Book is currently borrowed by " +
                    activeBorrowings.size() + " user(s). Book ID: " + id);
        }

        borrowingRepository.deleteByBookId(id);
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
    public List<BookResponse> searchBooksByGenre(String genre, int page, int size) {
        log.info("Searching books by genre: {}", genre);
        Pageable pageable = PageRequest.of(page, size);
        Genre genreEnum = Genre.valueOf(genre.toUpperCase());
        Page<Book> bookPage = bookRepository.findByGenre(genreEnum, pageable);
        return bookPage.getContent().stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAvailableBooks(int page, int size) {
        log.info("Fetching available books");
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAllByAvailableTrue(pageable);
        return bookPage.map(bookMapper::toResponse);
    }
}
