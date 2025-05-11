//package tr.com.eaaslan.library.util;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import tr.com.eaaslan.library.model.*;
//import tr.com.eaaslan.library.repository.BookRepository;
//import tr.com.eaaslan.library.repository.BorrowingRepository;
//import tr.com.eaaslan.library.repository.UserRepository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.Year;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
/// **
// * Initializes dummy data for testing and development purposes.
// * Creates sample users with different statuses and test books.
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
//    private final Random random = new Random();
//
//    @Override
//    public void run(String... args) {
//        if (borrowingRepository.count() > 0) {
//            log.info("Borrowing data already exists. Skipping initialization.");
//            return;
//        }
//
//        log.info("Starting dummy data initialization...");
//
//        // Her metot kendi transaction'ını yönetecek
//        createUsersIfNeeded();
//
//        List<Book> books = createBooksIfNeeded();
//
//        // Önemli: Metot çağrıları arasında transaction sınırını kır
//        createBorrowingsWithNewTransaction(books);
//
//        log.info("Dummy data initialization completed successfully.");
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void createUsersIfNeeded() {
//        // Second Active User - AdminUserInitializer'dan sonra çalıştığımız için patron@library.com zaten var
//        if (!userRepository.existsByEmail("patron2@library.com")) {
//            User patron2 = User.builder()
//                    .email("patron2@library.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .firstName("Second")
//                    .lastName("Patron")
//                    .phoneNumber("05551234563")
//                    .role(UserRole.PATRON)
//                    .status(UserStatus.ACTIVE)
//                    .maxAllowedBorrows(3)
//                    .build();
//            userRepository.save(patron2);
//            log.info("Active patron 2 created: {}", patron2.getEmail());
//        }
//
//        // Pending User - Admin initializer'daki pendingPatron@library.com ile çakışmaması için farklı email
//        if (!userRepository.existsByEmail("pending@library.com")) {
//            User pendingUser = User.builder()
//                    .email("pending@library.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .firstName("Pending")
//                    .lastName("User")
//                    .phoneNumber("05551234564")
//                    .role(UserRole.PATRON)
//                    .status(UserStatus.PENDING)
//                    .maxAllowedBorrows(3)
//                    .build();
//            userRepository.save(pendingUser);
//            log.info("Pending user created: {}", pendingUser.getEmail());
//        }
//
//        // Suspended User
//        if (!userRepository.existsByEmail("suspended@library.com")) {
//            User suspendedUser = User.builder()
//                    .email("suspended@library.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .firstName("Suspended")
//                    .lastName("User")
//                    .phoneNumber("05551234565")
//                    .role(UserRole.PATRON)
//                    .status(UserStatus.SUSPENDED)
//                    .suspensionEndDate(LocalDate.now().plusDays(7))
//                    .maxAllowedBorrows(3)
//                    .build();
//            userRepository.save(suspendedUser);
//            log.info("Suspended user created: {}", suspendedUser.getEmail());
//        }
//
//        // Deleted User
//        if (!userRepository.existsByEmail("deleted@library.com")) {
//            User deletedUser = User.builder()
//                    .email("deleted@library.com")
//                    .password(passwordEncoder.encode("password123"))
//                    .firstName("Deleted")
//                    .lastName("User")
//                    .phoneNumber("05551234566")
//                    .role(UserRole.PATRON)
//                    .status(UserStatus.DELETED)
//                    .deleted(true)
//                    .deletedAt(LocalDateTime.now().minusDays(5))
//                    .deletedBy("system")
//                    .maxAllowedBorrows(3)
//                    .build();
//            userRepository.save(deletedUser);
//            log.info("Deleted user created: {}", deletedUser.getEmail());
//        }
//
//        // Ekstra kullanıcılar oluştur
//        createExtraUsers();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void createExtraUsers() {
//        // 4 ekstra kullanıcı oluştur
//        for (int i = 1; i <= 4; i++) {
//            String email = "extra_patron" + i + "@library.com";
//            if (!userRepository.existsByEmail(email)) {
//                User user = User.builder()
//                        .email(email)
//                        .password(passwordEncoder.encode("password123"))
//                        .firstName("Extra")
//                        .lastName("Patron " + i)
//                        .phoneNumber("05551234" + (570 + i))
//                        .role(UserRole.PATRON)
//                        .status(UserStatus.ACTIVE)
//                        .maxAllowedBorrows(3)
//                        .build();
//                userRepository.save(user);
//                log.info("Created extra user: {}", email);
//            }
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public List<Book> createBooksIfNeeded() {
//        if (bookRepository.count() > 0) {
//            log.info("Books already exist. Using existing books.");
//            return bookRepository.findAll();
//        }
//
//        List<Book> books = new ArrayList<>();
//
//        // Özel test kitapları
//        Book book1 = Book.builder()
//                .isbn("0195153448")
//                .title("Classical Mythology")
//                .author("Mark P. O. Morford")
//                .publicationYear(Year.of(2002))
//                .publisher("Oxford University Press")
//                .genre(Genre.HISTORY)
//                .description("A comprehensive survey of classical mythology.")
//                .available(true)
//                .quantity(3)
//                .build();
//        books.add(book1);
//
//        Book book2 = Book.builder()
//                .isbn("0002005018")
//                .title("Clara Callan")
//                .author("Richard Bruce Wright")
//                .publicationYear(Year.of(2001))
//                .publisher("HarperFlamingo Canada")
//                .genre(Genre.FICTION)
//                .description("A story about two sisters in 1930s Ontario.")
//                .available(true)
//                .quantity(2)
//                .build();
//        books.add(book2);
//
//        Book book3 = Book.builder()
//                .isbn("0060973129")
//                .title("Decision in Normandy")
//                .author("Carlo D'Este")
//                .publicationYear(Year.of(1991))
//                .publisher("HarperPerennial")
//                .genre(Genre.HISTORY)
//                .description("A book about the Normandy campaign in WWII.")
//                .available(false) // 0 miktara sahip test kitabı
//                .quantity(0)
//                .build();
//        books.add(book3);
//
//        Book book4 = Book.builder()
//                .isbn("0374157065")
//                .title("Flu: The Story of the Great Influenza Pandemic")
//                .author("Gina Kolata")
//                .publicationYear(Year.of(1999))
//                .publisher("Farrar Straus Giroux")
//                .genre(Genre.SCIENCE)
//                .description("A history of the 1918 influenza pandemic.")
//                .available(true)
//                .quantity(1)
//                .build();
//        books.add(book4);
//
//        Book book5 = Book.builder()
//                .isbn("0393045218")
//                .title("The Mummies of Urumchi")
//                .author("E. J. W. Barber")
//                .publicationYear(Year.of(1999))
//                .publisher("W. W. Norton & Company")
//                .genre(Genre.HISTORY)
//                .description("A book about ancient mummies found in western China.")
//                .available(true)
//                .quantity(5)
//                .build();
//        books.add(book5);
//
//        // Daha fazla kitap ekleyelim (toplam 40 kitap olacak)
//        for (int i = 6; i <= 40; i++) {
//            Book book = Book.builder()
//                    .isbn("978" + String.format("%09d", i))
//                    .title(getRandomBookTitle())
//                    .author(getRandomAuthorName())
//                    .publicationYear(Year.of(1980 + random.nextInt(43))) // 1980-2023 arası
//                    .publisher(getRandomPublisher())
//                    .genre(getRandomGenre())
//                    .description(getRandomBookDescription())
//                    .available(true)
//                    .quantity(1 + random.nextInt(10)) // 1-10 arası rastgele miktar
//                    .build();
//            books.add(book);
//        }
//
//        bookRepository.saveAll(books);
//        log.info("Created {} test books", books.size());
//
//        return books;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void createBorrowingsWithNewTransaction(List<Book> books) {
//        createBorrowings(books);
//    }
//
//    private void createBorrowings(List<Book> books) {
//        if (books == null || books.isEmpty()) {
//            log.error("No books available for creating borrowings!");
//            return;
//        }
//
//        List<Borrowing> borrowings = new ArrayList<>();
//
//        User patron = userRepository.findByEmail("patron@library.com").orElse(null);
//        User patron2 = userRepository.findByEmail("patron2@library.com").orElse(null);
//        User suspendedUser = userRepository.findByEmail("suspended@library.com").orElse(null);
//        User extraPatron1 = userRepository.findByEmail("extra_patron1@library.com").orElse(null);
//        User extraPatron2 = userRepository.findByEmail("extra_patron2@library.com").orElse(null);
//        User extraPatron3 = userRepository.findByEmail("extra_patron3@library.com").orElse(null);
//        User extraPatron4 = userRepository.findByEmail("extra_patron4@library.com").orElse(null);
//
//        if (patron == null || patron2 == null || suspendedUser == null ||
//                extraPatron1 == null || extraPatron2 == null || extraPatron3 == null || extraPatron4 == null) {
//            log.error("Required users not found! Creating borrowings failed.");
//            log.error("patron exists: {}, patron2 exists: {}, suspended exists: {}, extra_patron1 exists: {}, extra_patron2 exists: {}, extra_patron3 exists: {}, extra_patron4 exists: {}",
//                    patron != null, patron2 != null, suspendedUser != null,
//                    extraPatron1 != null, extraPatron2 != null, extraPatron3 != null, extraPatron4 != null);
//            return;
//        }
//
//        // 1. Active borrowing'ler oluşturalım - her kullanıcı için maksimum 3
//        createActiveBorrowings(borrowings, patron, patron2, suspendedUser, extraPatron1, extraPatron2, books);
//
//        // 2. Overdue borrowing'ler oluşturalım - her kullanıcı için aktif + overdue toplamı maksimum 3
//        createOverdueBorrowings(borrowings, patron, patron2, suspendedUser, extraPatron3, extraPatron4, books);
//
//        // 3. 30 tane returned (iade edilmiş) borrowing oluşturalım
//        // 3.1. 5 tanesi geç teslim edilmiş ve suspended user'a ait
//        createLateReturnedBorrowings(borrowings, suspendedUser, books);
//
//        // 3.2. 25 tanesi zamanında teslim edilmiş (patronlar arasında dağıtalım)
//        createOnTimeReturnedBorrowings(borrowings, patron, patron2, books);
//
//        if (!borrowings.isEmpty()) {
//            borrowingRepository.saveAll(borrowings);
//            log.info("Created {} test borrowing records", borrowings.size());
//        } else {
//            log.warn("No borrowings were created!");
//        }
//    }
//
//    // 10 tane active borrowing oluştur - her kullanıcı için maksimum 3
//    private void createActiveBorrowings(List<Borrowing> borrowings, User patron, User patron2,
//                                        User suspendedUser, User extraPatron1, User extraPatron2, List<Book> allBooks) {
//        // Patron için 2 active borrowing (daha sonra 1 overdue eklenecek)
//        for (int i = 0; i < 2; i++) {
//            Book book = getRandomAvailableBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(10) + 1);
//            LocalDate dueDate = borrowDate.plusDays(14); // 2 haftalık ödünç verme süresi
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(patron)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//            updateBookQuantity(book);
//        }
//
//        // Patron2 için 2 active borrowing (daha sonra 1 overdue eklenecek)
//        for (int i = 0; i < 2; i++) {
//            Book book = getRandomAvailableBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(7) + 1);
//            LocalDate dueDate = borrowDate.plusDays(14);
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(patron2)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//            updateBookQuantity(book);
//        }
//
//        // Suspended User için 2 active borrowing (daha sonra 1 overdue eklenecek)
//        for (int i = 0; i < 2; i++) {
//            Book book = getRandomAvailableBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(5) + 1);
//            LocalDate dueDate = borrowDate.plusDays(14);
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(suspendedUser)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//            updateBookQuantity(book);
//        }
//
//        // Extra patron1 için 2 active borrowing
//        for (int i = 0; i < 2; i++) {
//            Book book = getRandomAvailableBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(3) + 1);
//            LocalDate dueDate = borrowDate.plusDays(14);
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(extraPatron1)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//            updateBookQuantity(book);
//        }
//
//        // Extra patron2 için 2 active borrowing
//        for (int i = 0; i < 2; i++) {
//            Book book = getRandomAvailableBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(3) + 1);
//            LocalDate dueDate = borrowDate.plusDays(14);
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(extraPatron2)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .status(BorrowingStatus.ACTIVE)
//                    .build();
//
//            borrowings.add(borrowing);
//            updateBookQuantity(book);
//        }
//    }
//
//    // 5 tane overdue borrowing oluştur - kişi başı maksimum toplamda 3 aktif olacak şekilde
//    private void createOverdueBorrowings(List<Borrowing> borrowings, User patron, User patron2,
//                                         User suspendedUser, User extraPatron3, User extraPatron4, List<Book> allBooks) {
//        // 1 tane patron için (toplam 2 active + 1 overdue = 3 olacak)
//        Book book = getRandomAvailableBook(allBooks);
//        LocalDate borrowDate = LocalDate.now().minusDays(20 + random.nextInt(10)); // 20-30 gün önce ödünç alınmış
//        LocalDate dueDate = borrowDate.plusDays(14); // 14 günlük süre (şu an geçmiş durumda)
//
//        Borrowing borrowing = Borrowing.builder()
//                .user(patron)
//                .book(book)
//                .borrowDate(borrowDate)
//                .dueDate(dueDate)
//                .status(BorrowingStatus.OVERDUE)
//                .build();
//
//        borrowings.add(borrowing);
//        updateBookQuantity(book);
//
//        // 1 tane patron2 için (toplam 2 active + 1 overdue = 3 olacak)
//        book = getRandomAvailableBook(allBooks);
//        borrowDate = LocalDate.now().minusDays(15 + random.nextInt(10)); // 15-25 gün önce
//        dueDate = borrowDate.plusDays(14); // Şu an geçmiş durumda
//
//        borrowing = Borrowing.builder()
//                .user(patron2)
//                .book(book)
//                .borrowDate(borrowDate)
//                .dueDate(dueDate)
//                .status(BorrowingStatus.OVERDUE)
//                .build();
//
//        borrowings.add(borrowing);
//        updateBookQuantity(book);
//
//        // 1 tane suspended user için (toplam 2 active + 1 overdue = 3 olacak)
//        book = getRandomAvailableBook(allBooks);
//        borrowDate = LocalDate.now().minusDays(20 + random.nextInt(5));
//        dueDate = borrowDate.plusDays(14);
//
//        borrowing = Borrowing.builder()
//                .user(suspendedUser)
//                .book(book)
//                .borrowDate(borrowDate)
//                .dueDate(dueDate)
//                .status(BorrowingStatus.OVERDUE)
//                .build();
//
//        borrowings.add(borrowing);
//        updateBookQuantity(book);
//
//        // Extra patron3 için 1 overdue
//        book = getRandomAvailableBook(allBooks);
//        borrowDate = LocalDate.now().minusDays(25 + random.nextInt(5));
//        dueDate = borrowDate.plusDays(14);
//
//        borrowing = Borrowing.builder()
//                .user(extraPatron3)
//                .book(book)
//                .borrowDate(borrowDate)
//                .dueDate(dueDate)
//                .status(BorrowingStatus.OVERDUE)
//                .build();
//
//        borrowings.add(borrowing);
//        updateBookQuantity(book);
//
//        // Extra patron4 için 1 overdue
//        book = getRandomAvailableBook(allBooks);
//        borrowDate = LocalDate.now().minusDays(25 + random.nextInt(5));
//        dueDate = borrowDate.plusDays(14);
//
//        borrowing = Borrowing.builder()
//                .user(extraPatron4)
//                .book(book)
//                .borrowDate(borrowDate)
//                .dueDate(dueDate)
//                .status(BorrowingStatus.OVERDUE)
//                .build();
//
//        borrowings.add(borrowing);
//        updateBookQuantity(book);
//    }
//
//    // 5 tane geç iade edilmiş borrowing oluştur (suspended user için)
//    private void createLateReturnedBorrowings(List<Borrowing> borrowings, User suspendedUser, List<Book> allBooks) {
//        for (int i = 0; i < 5; i++) {
//            Book book = getRandomBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(40 + random.nextInt(20)); // 40-60 gün önce
//            LocalDate dueDate = borrowDate.plusDays(14); // 14 günlük süre
//            LocalDate returnDate = dueDate.plusDays(random.nextInt(10) + 1); // 1-10 gün geç
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(suspendedUser)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .returnDate(returnDate)
//                    .status(BorrowingStatus.RETURNED)
//                    .returnedLate(true) // Geç iade edilmiş
//                    .build();
//
//            borrowings.add(borrowing);
//            // Kitap miktarını güncellemeye gerek yok, zaten iade edilmiş
//        }
//    }
//
//    // 25 tane zamanında iade edilmiş borrowing oluştur
//    private void createOnTimeReturnedBorrowings(List<Borrowing> borrowings, User patron, User patron2, List<Book> allBooks) {
//        // 13 tane patron için
//        for (int i = 0; i < 13; i++) {
//            Book book = getRandomBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(30 + random.nextInt(60)); // 30-90 gün önce
//            LocalDate dueDate = borrowDate.plusDays(14);
//            LocalDate returnDate = dueDate.minusDays(random.nextInt((int) (dueDate.toEpochDay() - borrowDate.toEpochDay() + 1))); // Duedate'ten önce
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(patron)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .returnDate(returnDate)
//                    .status(BorrowingStatus.RETURNED)
//                    .returnedLate(false) // Zamanında iade
//                    .build();
//
//            borrowings.add(borrowing);
//        }
//
//        // 12 tane patron2 için
//        for (int i = 0; i < 12; i++) {
//            Book book = getRandomBook(allBooks);
//            LocalDate borrowDate = LocalDate.now().minusDays(30 + random.nextInt(60)); // 30-90 gün önce
//            LocalDate dueDate = borrowDate.plusDays(14);
//            LocalDate returnDate = dueDate.minusDays(random.nextInt((int) (dueDate.toEpochDay() - borrowDate.toEpochDay() + 1))); // Duedate'ten önce
//
//            Borrowing borrowing = Borrowing.builder()
//                    .user(patron2)
//                    .book(book)
//                    .borrowDate(borrowDate)
//                    .dueDate(dueDate)
//                    .returnDate(returnDate)
//                    .status(BorrowingStatus.RETURNED)
//                    .returnedLate(false) // Zamanında iade
//                    .build();
//
//            borrowings.add(borrowing);
//        }
//    }
//
//    // Kitap miktarını azalt
//    private void updateBookQuantity(Book book) {
//        book.setQuantity(book.getQuantity() - 1);
//        if (book.getQuantity() <= 0) {
//            book.setAvailable(false);
//        }
//        bookRepository.save(book);
//    }
//
//    // Rastgele uygun bir kitap al
//    private Book getRandomAvailableBook(List<Book> books) {
//        List<Book> availableBooks = books.stream()
//                .filter(Book::isAvailable)
//                .filter(book -> book.getQuantity() > 0)
//                .toList();
//
//        if (availableBooks.isEmpty()) {
//            // Eğer uygun kitap yoksa bir kitabı uygun hale getir
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
//    // Rastgele herhangi bir kitap al
//    private Book getRandomBook(List<Book> books) {
//        return books.get(random.nextInt(books.size()));
//    }
//
//    // Helper metodları
//    private Genre getRandomGenre() {
//        Genre[] genres = Genre.values();
//        return genres[random.nextInt(genres.length)];
//    }
//
//    private String getRandomAuthorName() {
//        String[] authors = {
//                "John Smith", "Emily Johnson", "Michael Williams", "Sarah Brown", "David Jones",
//                "Jessica Miller", "Christopher Davis", "Jennifer Garcia", "Matthew Rodriguez", "Amanda Martinez",
//                "Robert Taylor", "Elizabeth Thomas", "James Jackson", "Ashley White", "Daniel Harris",
//                "Stephanie Martin", "William Thompson", "Melissa Garcia", "Joseph Martinez", "Nicole Robinson"
//        };
//        return authors[random.nextInt(authors.length)];
//    }
//
//    private String getRandomPublisher() {
//        String[] publishers = {
//                "Penguin Random House", "HarperCollins", "Simon & Schuster", "Hachette Book Group",
//                "Macmillan Publishers", "Oxford University Press", "Cambridge University Press", "Scholastic",
//                "Wiley", "Pearson Education", "McGraw-Hill Education", "Cengage Learning",
//                "Bloomsbury", "Elsevier", "Springer Nature", "Taylor & Francis"
//        };
//        return publishers[random.nextInt(publishers.length)];
//    }
//
//    private String getRandomBookTitle() {
//        String[] titles = {
//                "The Secret Garden", "Brave New World", "The Great Gatsby", "To Kill a Mockingbird",
//                "1984", "Pride and Prejudice", "The Catcher in the Rye", "The Hobbit",
//                "The Lord of the Rings", "Harry Potter", "The Chronicles of Narnia", "The Hunger Games",
//                "A Tale of Two Cities", "The Da Vinci Code", "The Alchemist", "The Little Prince",
//                "The Road Less Traveled", "The Power of Now", "Think and Grow Rich", "How to Win Friends and Influence People",
//                "The 7 Habits of Highly Effective People", "The Art of War", "The Prince", "The Republic",
//                "Crime and Punishment", "War and Peace", "The Brothers Karamazov", "Don Quixote",
//                "Moby Dick", "Ulysses", "The Odyssey", "The Iliad"
//        };
//        return titles[random.nextInt(titles.length)] + " " + (random.nextInt(3) + 1);
//    }
//
//    private String getRandomBookDescription() {
//        String[] descriptions = {
//                "A captivating story that explores themes of identity and belonging.",
//                "An insightful exploration of society and culture.",
//                "A masterpiece that weaves together history and fiction.",
//                "A thought-provoking narrative about human relationships.",
//                "A classic tale of love, loss, and redemption.",
//                "An engaging story set against the backdrop of political change.",
//                "A compelling character study with rich psychological depth.",
//                "A thrilling adventure that will keep you on the edge of your seat.",
//                "A moving account of personal transformation and growth.",
//                "A fascinating examination of the human condition.",
//                "A groundbreaking work that challenges conventional wisdom.",
//                "A heartwarming tale of friendship and courage.",
//                "A disturbing look at the darker aspects of human nature.",
//                "A visionary work that imagines possible futures.",
//                "A meticulous historical account of important events."
//        };
//        return descriptions[random.nextInt(descriptions.length)];
//    }
//}