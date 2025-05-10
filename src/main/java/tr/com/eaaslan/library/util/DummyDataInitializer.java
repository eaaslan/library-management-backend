//package tr.com.eaaslan.library.util;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import tr.com.eaaslan.library.model.Book;
//import tr.com.eaaslan.library.model.Borrowing;
//import tr.com.eaaslan.library.model.BorrowingStatus;
//import tr.com.eaaslan.library.model.Genre;
//import tr.com.eaaslan.library.model.User;
//import tr.com.eaaslan.library.model.UserRole;
//import tr.com.eaaslan.library.model.UserStatus;
//import tr.com.eaaslan.library.repository.BookRepository;
//import tr.com.eaaslan.library.repository.BorrowingRepository;
//import tr.com.eaaslan.library.repository.UserRepository;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
/// **
// * Initializes dummy data for testing and development purposes.
// * Creates patron users with different statuses and borrowing records with various statuses.
// * This initializer runs after the AdminUserInitializer to ensure admin accounts are already created.
// */
//@Component
//@RequiredArgsConstructor
//@Slf4j
//@Order(2) // Run after AdminUserInitializer which has default Order(1)
//public class DummyDataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final BookRepository bookRepository;
//    private final BorrowingRepository borrowingRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    private final Random random = new Random();
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        // Check if dummy data already exists
//        if (userRepository.count() > 2) { // More than admin and librarian
//            log.info("Dummy data already exists. Skipping initialization.");
//            return;
//        }
//
//        log.info("Starting dummy data initialization...");
//
//        // Create books if none exist
//        List<Book> allBooks = bookRepository.findAll();
//        if (allBooks.isEmpty()) {
//            createDummyBooks();
//            allBooks = bookRepository.findAll();
//        }
//
//        // Create users with different statuses
//        List<User> patrons = createPatronUsers();
//
//        // Create borrowing records with different statuses
//        createBorrowingRecords(patrons, allBooks);
//
//        log.info("Dummy data initialization completed successfully.");
//    }
//
//    /**
//     * Creates 20 patron users with different statuses as specified:
//     * - 5 PENDING
//     * - 10 ACTIVE
//     * - 4 SUSPENDED
//     * - 1 DELETED
//     */
//    private List<User> createPatronUsers() {
//        List<User> patrons = new ArrayList<>();
//
//        // 5 PENDING users
//        for (int i = 1; i <= 5; i++) {
//            User user = createUser("pending" + i, UserStatus.PENDING);
//            patrons.add(user);
//        }
//
//        // 10 ACTIVE users
//        for (int i = 1; i <= 10; i++) {
//            User user = createUser("active" + i, UserStatus.ACTIVE);
//            patrons.add(user);
//        }
//
//        // 4 SUSPENDED users
//        for (int i = 1; i <= 4; i++) {
//            User user = createUser("suspended" + i, UserStatus.SUSPENDED);
//            patrons.add(user);
//        }
//
//        // 1 DELETED user
//        User deletedUser = createUser("deleted1", UserStatus.DELETED);
//        deletedUser.setDeleted(true);
//        deletedUser.setDeletedAt(java.time.LocalDateTime.now().minusDays(5));
//        deletedUser.setDeletedBy("system");
//        patrons.add(deletedUser);
//
//        log.info("Created {} patron users with various statuses", patrons.size());
//        return patrons;
//    }
//
//    /**
//     * Helper method to create a single user
//     */
//    private User createUser(String uniqueName, UserStatus status) {
//        User user = User.builder()
//                .email(uniqueName + "@example.com")
//                .password(passwordEncoder.encode("password"))
//                .firstName(getRandomFirstName())
//                .lastName(getRandomLastName())
//                .phoneNumber(generateRandomTurkishPhoneNumber())
//                .role(UserRole.PATRON)
//                .status(status)
//                .maxAllowedBorrows(3)
//                .build();
//        return userRepository.save(user);
//    }
//
//    /**
//     * Creates borrowing records with different statuses:
//     * - 30 ACTIVE (not yet due)
//     * - 20 OVERDUE (past due date, not yet returned)
//     * - 20 RETURNED and returnedLate=false (returned on time)
//     * - 20 RETURNED and returnedLate=true (returned late)
//     */
//    private void createBorrowingRecords(List<User> patrons, List<Book> books) {
//        // Filter out active users only for borrowing
//        List<User> activeUsers = patrons.stream()
//                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
//                .toList();
//
//        if (activeUsers.isEmpty() || books.isEmpty()) {
//            log.warn("Cannot create borrowing records: no active users or books available");
//            return;
//        }
//
//        List<Borrowing> borrowings = new ArrayList<>();
//
//        // 30 ACTIVE borrowings (not yet due)
//        for (int i = 0; i < 30; i++) {
//            User user = activeUsers.get(random.nextInt(activeUsers.size()));
//            Book book = getRandomAvailableBook(books);
//
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(7) + 1); // 1-7 days ago
//            LocalDate dueDate = borrowDate.plusDays(14); // 2 weeks loan period
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(user)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//
//            // Update book availability
//            book.setQuantity(book.getQuantity() - 1);
//            if (book.getQuantity() <= 0) {
//                book.setAvailable(false);
//            }
//            bookRepository.save(book);
//        }
//
//        // 20 OVERDUE borrowings (past due date, not returned)
//        for (int i = 0; i < 20; i++) {
//            User user = activeUsers.get(random.nextInt(activeUsers.size()));
//            Book book = getRandomAvailableBook(books);
//
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(10) + 20); // 20-30 days ago
//            LocalDate dueDate = borrowDate.plusDays(14); // Due date already passed
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(user)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.OVERDUE)
//                    .build();
//
//            borrowings.add(borrowing);
//
//            // Update book availability
//            book.setQuantity(book.getQuantity() - 1);
//            if (book.getQuantity() <= 0) {
//                book.setAvailable(false);
//            }
//            bookRepository.save(book);
//        }
//
//        // 20 RETURNED borrowings - returned on time (returnedLate=false)
//        for (int i = 0; i < 20; i++) {
//            User user = activeUsers.get(random.nextInt(activeUsers.size()));
//            Book book = getRandomBook(books);
//
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(30) + 15); // 15-45 days ago
//            LocalDate dueDate = borrowDate.plusDays(14);
//            // Return before or on due date
//            LocalDate returnDate = borrowDate.plusDays(random.nextInt((int) (dueDate.toEpochDay() - borrowDate.toEpochDay()) + 1));
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(user)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .returnDate(returnDate)
//                    .status(BorrowingStatus.RETURNED)
//                    .returnedLate(false) // Not late - returned on or before due date
//                    .build();
//
//            borrowings.add(borrowing);
//        }
//
//        // 20 RETURNED borrowings - returned late (returnedLate=true)
//        for (int i = 0; i < 20; i++) {
//            User user = activeUsers.get(random.nextInt(activeUsers.size()));
//            Book book = getRandomBook(books);
//
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(20) + 30); // 30-50 days ago
//            LocalDate dueDate = borrowDate.plusDays(14);
//            // Return date after due date (1-10 days late)
//            LocalDate returnDate = dueDate.plusDays(random.nextInt(10) + 1);
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(user)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .returnDate(returnDate)
//                    .status(BorrowingStatus.RETURNED)
//                    .returnedLate(true) // Late - returned after due date
//                    .build();
//
//            borrowings.add(borrowing);
//        }
//
//        borrowingRepository.saveAll(borrowings);
//        log.info("Created {} borrowing records with various statuses and late return flags", borrowings.size());
//    }
//
//    /**
//     * Create some dummy books if the database is empty
//     */
//    private void createDummyBooks() {
//        List<Book> books = new ArrayList<>();
//
//        // Add 30 dummy books with various genres
//        for (int i = 1; i <= 30; i++) {
//            Book book = Book.builder()
//                    .isbn("978123456" + String.format("%04d", i))
//                    .title("Book Title " + i)
//                    .author(getRandomAuthorName())
//                    .publicationYear(java.time.Year.of(2000 + random.nextInt(23)))
//                    .publisher(getRandomPublisher())
//                    .genre(getRandomGenre())
//                    .description("Description for book " + i + ". " + getRandomBookDescription())
//                    .available(true)
//                    .quantity(1 + random.nextInt(5))
//                    .build();
//
//            books.add(book);
//        }
//
//        bookRepository.saveAll(books);
//        log.info("Created {} dummy books", books.size());
//    }
//
//    // Helper methods
//
//    private Book getRandomAvailableBook(List<Book> books) {
//        List<Book> availableBooks = books.stream()
//                .filter(Book::isAvailable)
//                .filter(book -> book.getQuantity() > 0)
//                .toList();
//
//        if (availableBooks.isEmpty()) {
//            // If no books are available, make one available
//            Book book = books.get(random.nextInt(books.size()));
//            book.setQuantity(1);
//            book.setAvailable(true);
//            bookRepository.save(book);
//            return book;
//        }
//
//        return availableBooks.get(random.nextInt(availableBooks.size()));
//    }
//
//    private Book getRandomBook(List<Book> books) {
//        return books.get(random.nextInt(books.size()));
//    }
//
//    private Genre getRandomGenre() {
//        Genre[] genres = Genre.values();
//        return genres[random.nextInt(genres.length)];
//    }
//
//    private String getRandomFirstName() {
//        String[] firstNames = {"Ali", "Ayşe", "Mehmet", "Fatma", "Mustafa", "Zeynep", "Ahmet", "Emine",
//                "Hasan", "Hatice", "Hüseyin", "Elif", "İbrahim", "Meryem", "Osman", "Zehra"};
//        return firstNames[random.nextInt(firstNames.length)];
//    }
//
//    private String getRandomLastName() {
//        String[] lastNames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Özdemir", "Arslan",
//                "Doğan", "Kılıç", "Aslan", "Çetin", "Koç", "Kurt", "Özkan", "Acar"};
//        return lastNames[random.nextInt(lastNames.length)];
//    }
//
//    private String getRandomAuthorName() {
//        String[] authors = {"Orhan Pamuk", "Elif Şafak", "Yaşar Kemal", "Sabahattin Ali", "Ahmet Hamdi Tanpınar",
//                "Oğuz Atay", "Nazım Hikmet", "Peyami Safa", "Halide Edip Adıvar", "Reşat Nuri Güntekin"};
//        return authors[random.nextInt(authors.length)];
//    }
//
//    private String getRandomPublisher() {
//        String[] publishers = {"Yapı Kredi Yayınları", "Can Yayınları", "İletişim Yayınları", "Doğan Kitap",
//                "Everest Yayınları", "İş Bankası Kültür Yayınları", "Metis Yayınları", "Alfa Yayınları"};
//        return publishers[random.nextInt(publishers.length)];
//    }
//
//    private String getRandomBookDescription() {
//        String[] descriptions = {
//                "A captivating story that explores themes of identity and belonging.",
//                "An insightful exploration of Turkish society and culture.",
//                "A masterpiece that weaves together history and fiction.",
//                "A thought-provoking narrative about human relationships.",
//                "A classic tale of love, loss, and redemption.",
//                "An engaging story set against the backdrop of political change.",
//                "A compelling character study with rich psychological depth."
//        };
//        return descriptions[random.nextInt(descriptions.length)];
//    }
//
//    private String generateRandomTurkishPhoneNumber() {
//        // Format: 05XXXXXXXXX (must be 11 digits total, starting with 05)
//        StringBuilder phoneNumber = new StringBuilder("05");
//        phoneNumber.append(30 + random.nextInt(70)); // 30-99 for the third and fourth digits
//
//        // Add remaining 7 digits
//        for (int i = 0; i < 7; i++) {
//            phoneNumber.append(random.nextInt(10));
//        }
//
//        return phoneNumber.toString();
//    }
//}