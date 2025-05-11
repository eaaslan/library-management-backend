package tr.com.eaaslan.library.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initializes test data for the library management system.
 * Creates users, books, and borrowings for testing and demonstration purposes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 && borrowingRepository.count() > 0) {
            log.info("Data already exists. Skipping initialization.");
            return;
        }

        log.info("Starting data initialization...");

        // Create users with separate transaction
        createUsers();

        // Create books with separate transaction
        List<Long> bookIds = createBooks();

        // Create borrowings with separate transaction
        createBorrowingsWithNewTransaction(bookIds);

        log.info("Data initialization completed successfully.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUsers() {
        // Admin user
        if (!userRepository.existsByEmail("admin@library.com")) {
            User adminUser = User.builder()
                    .email("admin@library.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("05501234567")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(adminUser);
            log.info("Admin user created: {}", adminUser.getEmail());
        }

        // Librarian user
        if (!userRepository.existsByEmail("librarian@library.com")) {
            User librarianUser = User.builder()
                    .email("librarian@library.com")
                    .password(passwordEncoder.encode("librarian123"))
                    .firstName("Librarian")
                    .lastName("User")
                    .phoneNumber("05551234567")
                    .role(UserRole.LIBRARIAN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(librarianUser);
            log.info("Librarian user created: {}", librarianUser.getEmail());
        }

        // Active patron user
        if (!userRepository.existsByEmail("patron@library.com")) {
            User patronUser = User.builder()
                    .email("patron@library.com")
                    .password(passwordEncoder.encode("patron123"))
                    .firstName("Patron")
                    .lastName("User")
                    .phoneNumber("05551234562")
                    .role(UserRole.PATRON)
                    .status(UserStatus.ACTIVE)
                    .maxAllowedBorrows(3)
                    .build();
            userRepository.save(patronUser);
            log.info("Active patron user created: {}", patronUser.getEmail());
        }

        // Second Active Patron
        if (!userRepository.existsByEmail("patron2@library.com")) {
            User patron2 = User.builder()
                    .email("patron2@library.com")
                    .password(passwordEncoder.encode("patron123"))
                    .firstName("Second")
                    .lastName("Patron")
                    .phoneNumber("05551234563")
                    .role(UserRole.PATRON)
                    .status(UserStatus.ACTIVE)
                    .maxAllowedBorrows(3)
                    .build();
            userRepository.save(patron2);
            log.info("Second active patron created: {}", patron2.getEmail());
        }

        // Pending User
        if (!userRepository.existsByEmail("pending@library.com")) {
            User pendingUser = User.builder()
                    .email("pending@library.com")
                    .password(passwordEncoder.encode("patron123"))
                    .firstName("Pending")
                    .lastName("User")
                    .phoneNumber("05551234564")
                    .role(UserRole.PATRON)
                    .status(UserStatus.PENDING)
                    .maxAllowedBorrows(3)
                    .build();
            userRepository.save(pendingUser);
            log.info("Pending user created: {}", pendingUser.getEmail());
        }

        // Suspended User
        if (!userRepository.existsByEmail("suspended@library.com")) {
            User suspendedUser = User.builder()
                    .email("suspended@library.com")
                    .password(passwordEncoder.encode("patron123"))
                    .firstName("Suspended")
                    .lastName("User")
                    .phoneNumber("05551234565")
                    .role(UserRole.PATRON)
                    .status(UserStatus.SUSPENDED)
                    .suspensionEndDate(LocalDate.now().plusDays(7))
                    .maxAllowedBorrows(3)
                    .build();
            userRepository.save(suspendedUser);
            log.info("Suspended user created: {}", suspendedUser.getEmail());
        }

        // Deleted User
        if (!userRepository.existsByEmail("deleted@library.com")) {
            User deletedUser = User.builder()
                    .email("deleted@library.com")
                    .password(passwordEncoder.encode("patron123"))
                    .firstName("Deleted")
                    .lastName("User")
                    .phoneNumber("05551234566")
                    .role(UserRole.PATRON)
                    .status(UserStatus.DELETED)
                    .deleted(true)
                    .deletedAt(LocalDateTime.now().minusDays(5))
                    .deletedBy("system")
                    .maxAllowedBorrows(3)
                    .build();
            userRepository.save(deletedUser);
            log.info("Deleted user created: {}", deletedUser.getEmail());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> createBooks() {
        if (bookRepository.count() > 0) {
            log.info("Books already exist. Using existing book IDs.");
            return bookRepository.findAll().stream().map(Book::getId).toList();
        }

        List<Book> books = new ArrayList<>();

        // Core test books
        Book book1 = Book.builder()
                .isbn("0195153448")
                .title("Classical Mythology")
                .author("Mark P. O. Morford")
                .publicationYear(Year.of(2002))
                .publisher("Oxford University Press")
                .genre(Genre.HISTORY)
                .description("A comprehensive survey of classical mythology.")
                .available(true)
                .quantity(3)
                .build();
        books.add(book1);

        Book book2 = Book.builder()
                .isbn("0002005018")
                .title("Clara Callan")
                .author("Richard Bruce Wright")
                .publicationYear(Year.of(2001))
                .publisher("HarperFlamingo Canada")
                .genre(Genre.FICTION)
                .description("A story about two sisters in 1930s Ontario.")
                .available(true)
                .quantity(2)
                .build();
        books.add(book2);

        Book book3 = Book.builder()
                .isbn("0060973129")
                .title("Decision in Normandy")
                .author("Carlo D'Este")
                .publicationYear(Year.of(1991))
                .publisher("HarperPerennial")
                .genre(Genre.HISTORY)
                .description("A book about the Normandy campaign in WWII.")
                .available(false) // Unavailable test book
                .quantity(0)
                .build();
        books.add(book3);

        Book book4 = Book.builder()
                .isbn("0374157065")
                .title("Flu: The Story of the Great Influenza Pandemic")
                .author("Gina Kolata")
                .publicationYear(Year.of(1999))
                .publisher("Farrar Straus Giroux")
                .genre(Genre.SCIENCE)
                .description("A history of the 1918 influenza pandemic.")
                .available(true)
                .quantity(1)
                .build();
        books.add(book4);

        Book book5 = Book.builder()
                .isbn("0393045218")
                .title("The Mummies of Urumchi")
                .author("E. J. W. Barber")
                .publicationYear(Year.of(1999))
                .publisher("W. W. Norton & Company")
                .genre(Genre.HISTORY)
                .description("A book about ancient mummies found in western China.")
                .available(true)
                .quantity(5)
                .build();
        books.add(book5);

        // Add additional books (total of 30 books)
        for (int i = 6; i <= 30; i++) {
            Book book = Book.builder()
                    .isbn("978" + String.format("%09d", i))
                    .title(getRandomBookTitle())
                    .author(getRandomAuthorName())
                    .publicationYear(Year.of(1980 + random.nextInt(43))) // 1980-2023
                    .publisher(getRandomPublisher())
                    .genre(getRandomGenre())
                    .description(getRandomBookDescription())
                    .available(true)
                    .quantity(1 + random.nextInt(5)) // 1-5 quantity
                    .build();
            books.add(book);
        }

        List<Book> savedBooks = bookRepository.saveAll(books);
        log.info("Created {} test books", savedBooks.size());

        return savedBooks.stream().map(Book::getId).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createBorrowingsWithNewTransaction(List<Long> bookIds) {
        createBorrowings(bookIds);
    }

    private void createBorrowings(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            log.error("No book IDs available for creating borrowings!");
            return;
        }

        List<Borrowing> borrowings = new ArrayList<>();

        User patron = userRepository.findByEmail("patron@library.com").orElse(null);
        User patron2 = userRepository.findByEmail("patron2@library.com").orElse(null);
        User suspendedUser = userRepository.findByEmail("suspended@library.com").orElse(null);

        if (patron == null || patron2 == null || suspendedUser == null) {
            log.error("Required users not found! Creating borrowings failed.");
            return;
        }

        // 1. Create active borrowings - each user gets up to 3
        // Patron with 3 active borrowings (max allowed)
        for (int i = 0; i < 3; i++) {
            Long bookId = getRandomAvailableBookId(bookIds);
            if (bookId == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(10) + 1);
            LocalDate dueDate = borrowDate.plusDays(14); // 2-week lending period

            Borrowing borrowing = Borrowing.builder()
                    .user(patron)
                    .book(bookRepository.getReferenceById(bookId)) // Use reference instead of loaded entity
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.ACTIVE)
                    .build();

            borrowings.add(borrowing);
            decrementBookQuantity(bookId);
        }

        // Patron2 with 2 active and 1 overdue (max 3 total)
        for (int i = 0; i < 2; i++) {
            Long bookId = getRandomAvailableBookId(bookIds);
            if (bookId == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(7) + 1);
            LocalDate dueDate = borrowDate.plusDays(14);

            Borrowing borrowing = Borrowing.builder()
                    .user(patron2)
                    .book(bookRepository.getReferenceById(bookId))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.ACTIVE)
                    .build();

            borrowings.add(borrowing);
            decrementBookQuantity(bookId);
        }

        // Overdue borrowing for patron2 (total becomes 3)
        Long bookId = getRandomAvailableBookId(bookIds);
        if (bookId != null) {
            LocalDate borrowDate = LocalDate.now().minusDays(20);
            LocalDate dueDate = borrowDate.plusDays(14); // Already past due

            Borrowing borrowing = Borrowing.builder()
                    .user(patron2)
                    .book(bookRepository.getReferenceById(bookId))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.OVERDUE)
                    .build();

            borrowings.add(borrowing);
            decrementBookQuantity(bookId);
        }

        // Suspended User with 2 active borrowings and 1 overdue
        for (int i = 0; i < 2; i++) {
            Long bookId2 = getRandomAvailableBookId(bookIds);
            if (bookId2 == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(5) + 1);
            LocalDate dueDate = borrowDate.plusDays(14);

            Borrowing borrowing = Borrowing.builder()
                    .user(suspendedUser)
                    .book(bookRepository.getReferenceById(bookId2))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.ACTIVE)
                    .build();

            borrowings.add(borrowing);
            decrementBookQuantity(bookId2);
        }

        // Overdue borrowing for suspended user
        Long bookId3 = getRandomAvailableBookId(bookIds);
        if (bookId3 != null) {
            LocalDate borrowDate = LocalDate.now().minusDays(20);
            LocalDate dueDate = borrowDate.plusDays(14);

            Borrowing borrowing = Borrowing.builder()
                    .user(suspendedUser)
                    .book(bookRepository.getReferenceById(bookId3))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.OVERDUE)
                    .build();

            borrowings.add(borrowing);
            decrementBookQuantity(bookId3);
        }

        // Create 5 late returned borrowings for suspended user (reason for suspension)
        createLateReturnedBorrowings(borrowings, suspendedUser, bookIds);

        // Create returned (on-time) borrowings
        createOnTimeReturnedBorrowings(borrowings, patron, patron2, bookIds);

        if (!borrowings.isEmpty()) {
            borrowingRepository.saveAll(borrowings);
            log.info("Created {} test borrowing records", borrowings.size());
        } else {
            log.warn("No borrowings were created!");
        }
    }

    // Helper method for creating late returned borrowings
    private void createLateReturnedBorrowings(List<Borrowing> borrowings, User user, List<Long> bookIds) {
        for (int i = 0; i < 5; i++) {
            Long bookId = getRandomBookId(bookIds);
            if (bookId == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(40 + random.nextInt(20)); // 40-60 days ago
            LocalDate dueDate = borrowDate.plusDays(14);
            LocalDate returnDate = dueDate.plusDays(random.nextInt(10) + 1); // 1-10 days late

            Borrowing borrowing = Borrowing.builder()
                    .user(user)
                    .book(bookRepository.getReferenceById(bookId))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .returnDate(returnDate)
                    .status(BorrowingStatus.RETURNED)
                    .returnedLate(true) // Returned late
                    .build();

            borrowings.add(borrowing);
        }
    }

    // Helper method for creating on-time returned borrowings
    private void createOnTimeReturnedBorrowings(List<Borrowing> borrowings, User patron, User patron2, List<Long> bookIds) {
        // For patron
        for (int i = 0; i < 12; i++) {
            Long bookId = getRandomBookId(bookIds);
            if (bookId == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(30 + random.nextInt(60)); // 30-90 days ago
            LocalDate dueDate = borrowDate.plusDays(14);
            LocalDate returnDate = dueDate.minusDays(1 + random.nextInt(12)); // 1-13 days before due date

            Borrowing borrowing = Borrowing.builder()
                    .user(patron)
                    .book(bookRepository.getReferenceById(bookId))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .returnDate(returnDate)
                    .status(BorrowingStatus.RETURNED)
                    .returnedLate(false)
                    .build();

            borrowings.add(borrowing);
        }

        // For patron2
        for (int i = 0; i < 12; i++) {
            Long bookId = getRandomBookId(bookIds);
            if (bookId == null) continue;

            LocalDate borrowDate = LocalDate.now().minusDays(30 + random.nextInt(60));
            LocalDate dueDate = borrowDate.plusDays(14);
            LocalDate returnDate = dueDate.minusDays(1 + random.nextInt(12));

            Borrowing borrowing = Borrowing.builder()
                    .user(patron2)
                    .book(bookRepository.getReferenceById(bookId))
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .returnDate(returnDate)
                    .status(BorrowingStatus.RETURNED)
                    .returnedLate(false)
                    .build();

            borrowings.add(borrowing);
        }
    }

    // Get a random available book ID
    @Transactional(propagation = Propagation.REQUIRED)
    protected Long getRandomAvailableBookId(List<Long> bookIds) {
        List<Long> availableBookIds = bookRepository.findAllById(bookIds).stream()
                .filter(Book::isAvailable)
                .filter(book -> book.getQuantity() > 0)
                .map(Book::getId)
                .toList();

        if (availableBookIds.isEmpty()) {
            // Make a book available if none are
            if (!bookIds.isEmpty()) {
                Long bookId = bookIds.get(random.nextInt(bookIds.size()));
                Book book = bookRepository.findById(bookId).orElse(null);
                if (book != null) {
                    book.setQuantity(1);
                    book.setAvailable(true);
                    bookRepository.save(book);
                    return bookId;
                }
            }
            return null;
        }

        return availableBookIds.get(random.nextInt(availableBookIds.size()));
    }

    // Get a random book ID (regardless of availability)
    private Long getRandomBookId(List<Long> bookIds) {
        if (bookIds.isEmpty()) return null;
        return bookIds.get(random.nextInt(bookIds.size()));
    }

    // Safely decrement book quantity
    @Transactional(propagation = Propagation.REQUIRED)
    protected void decrementBookQuantity(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setQuantity(book.getQuantity() - 1);
            if (book.getQuantity() <= 0) {
                book.setAvailable(false);
            }
            bookRepository.save(book);
        }
    }

    // Helper methods for random data generation
    private Genre getRandomGenre() {
        Genre[] genres = Genre.values();
        return genres[random.nextInt(genres.length)];
    }

    private String getRandomAuthorName() {
        String[] authors = {
                "John Smith", "Emily Johnson", "Michael Williams", "Sarah Brown", "David Jones",
                "Jessica Miller", "Christopher Davis", "Jennifer Garcia", "Matthew Rodriguez", "Amanda Martinez"
        };
        return authors[random.nextInt(authors.length)];
    }

    private String getRandomPublisher() {
        String[] publishers = {
                "Penguin Random House", "HarperCollins", "Simon & Schuster", "Hachette Book Group",
                "Macmillan Publishers", "Oxford University Press", "Cambridge University Press", "Scholastic"
        };
        return publishers[random.nextInt(publishers.length)];
    }

    private String getRandomBookTitle() {
        String[] titles = {
                "The Secret Garden", "Brave New World", "The Great Gatsby", "To Kill a Mockingbird",
                "1984", "Pride and Prejudice", "The Catcher in the Rye", "The Hobbit",
                "The Lord of the Rings", "Harry Potter", "The Chronicles of Narnia", "The Hunger Games"
        };
        return titles[random.nextInt(titles.length)] + " " + (random.nextInt(3) + 1);
    }

    private String getRandomBookDescription() {
        String[] descriptions = {
                "A captivating story that explores themes of identity and belonging.",
                "An insightful exploration of society and culture.",
                "A masterpiece that weaves together history and fiction.",
                "A thought-provoking narrative about human relationships.",
                "A classic tale of love, loss, and redemption."
        };
        return descriptions[random.nextInt(descriptions.length)];
    }
}