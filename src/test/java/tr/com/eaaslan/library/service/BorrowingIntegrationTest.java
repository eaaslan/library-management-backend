package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.exception.UserSuspendedException;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BorrowingIntegrationTest {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Should prevent suspended users from borrowing books")
    void shouldPreventSuspendedUsersFromBorrowingBooks() {
        // Benzersiz bir telefon numarası oluştur
        String uniquePhoneNumber = "05524323465";

        // Askıya alınmış bir kullanıcı oluştur
        User suspendedUser = User.builder()
                .email("suspended-test@example.com")
                .password("password")
                .firstName("Suspended")
                .lastName("User")
                .phoneNumber(uniquePhoneNumber)  // Benzersiz telefon numarası kullan
                .role(UserRole.PATRON)
                .status(UserStatus.SUSPENDED)
                .suspensionEndDate(LocalDate.now().plusDays(7))
                .build();
        userRepository.save(suspendedUser);

        // Benzersiz ISBN ile kitap oluştur
        Book book = Book.builder()
                .isbn("TEST" + UUID.randomUUID().toString().substring(0, 8))
                .title("Test Book")
                .author("Test Author")
                .publicationYear(java.time.Year.of(2020))
                .publisher("Test Publisher")
                .genre(Genre.FICTION)
                .available(true)
                .quantity(1)
                .build();
        bookRepository.save(book);

        // Ödünç alma isteği oluştur
        BorrowingCreateRequest request = new BorrowingCreateRequest(book.getId(), null, null);

        // Askıya alınmış kullanıcının kitap ödünç alamadığını doğrula
        assertThrows(UserSuspendedException.class, () -> {
            borrowingService.borrowBook(request, suspendedUser.getEmail());
        });
    }
}