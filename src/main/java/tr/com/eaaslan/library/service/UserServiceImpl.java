package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.*;
import tr.com.eaaslan.library.model.dto.user.*;
import tr.com.eaaslan.library.model.mapper.UserMapper;
import tr.com.eaaslan.library.repository.BookRepository;
import tr.com.eaaslan.library.repository.BorrowingRepository;
import tr.com.eaaslan.library.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, BorrowingRepository borrowingRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public LibrarianCreateResponse createLibrarianUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toEntity(userCreateRequest);

        user.setRole(UserRole.LIBRARIAN);
        user.setStatus(UserStatus.ACTIVE);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistException("User", "email", user.getEmail());
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new ResourceAlreadyExistException("User", "phone number", user.getPhoneNumber());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.toLibrarianResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createPatronUser(UserCreateRequest userCreateRequest) {

        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new ResourceAlreadyExistException("User", "email", userCreateRequest.email());
        }

        if (userRepository.existsByPhoneNumber(userCreateRequest.phoneNumber())) {
            throw new ResourceAlreadyExistException("User", "phone number", userCreateRequest.phoneNumber());
        }
        User user = userMapper.toEntity(userCreateRequest);

        user.setRole(UserRole.PATRON);
        user.setStatus(UserStatus.PENDING);


        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        return userMapper.toResponse(user);
    }


    public Page<UserResponse> getAllActiveUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<User> userPage = userRepository.findByDeletedFalse(pageable);
        return userPage.map(userMapper::toResponse);
    }

    public Page<UserResponse> getAllUsersIncludingDeleted(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserUpdateResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        user.setUpdatedBy(currentUserEmail);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateEntity(userUpdateRequest, user);
        userRepository.save(user);
        return userMapper.toUpdateResponse(user);
    }

    @Override
    @Transactional
    public UserResponse deleteUser(Long id, String userName) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        List<Borrowing> activeBorrowings = borrowingRepository.findByUserIdAndStatus(user.getId(), BorrowingStatus.ACTIVE);
        List<Borrowing> overdueBorrowings = borrowingRepository.findByUserIdAndStatus(user.getId(), BorrowingStatus.OVERDUE);

        if (!activeBorrowings.isEmpty()) {
            processActiveBorrowingsForDeletedUser(activeBorrowings);
        }

        if (!overdueBorrowings.isEmpty()) {
            processOverdueBorrowingsForDeletedUser(overdueBorrowings);
        }

        user.setStatus(UserStatus.DELETED);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(userName);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    private void processActiveBorrowingsForDeletedUser(List<Borrowing> activeBorrowings) {
        for (Borrowing borrowing : activeBorrowings) {
            borrowing.setStatus(BorrowingStatus.RETURNED);
            borrowing.setReturnDate(LocalDate.now());
            borrowing.setReturnedLate(false); // Active borrowings are not late
            borrowingRepository.save(borrowing);

            Book book = borrowing.getBook();
            book.setQuantity(book.getQuantity() + 1);
            if (!book.isAvailable()) {
                book.setAvailable(true);
            }
            bookRepository.save(book);
        }
        borrowingRepository.saveAll(activeBorrowings);
    }

    private void processOverdueBorrowingsForDeletedUser(List<Borrowing> overdueBorrowings) {
        for (Borrowing borrowing : overdueBorrowings) {
            borrowing.setStatus(BorrowingStatus.RETURNED);
            borrowing.setReturnDate(LocalDate.now());
            borrowing.setReturnedLate(true); // Overdue borrowings are returned late
            borrowingRepository.save(borrowing);

            Book book = borrowing.getBook();
            book.setQuantity(book.getQuantity() + 1);
            if (!book.isAvailable()) {
                book.setAvailable(true);
            }
            bookRepository.save(book);
        }
        borrowingRepository.saveAll(overdueBorrowings);
    }

    @Override
    @Transactional
    public UserUpdateResponse updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        user.setStatus(UserStatus.valueOf(status.toUpperCase()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        user.setUpdatedBy(currentUserEmail);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return userMapper.toUpdateResponse(user);
    }

    @Override
    @Transactional
    public UserResponse hardDeleteUser(Long id, String userName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        List<Borrowing> allBorrowings = borrowingRepository.findByUserId(user.getId(), Pageable.unpaged()).getContent();

        if (!allBorrowings.isEmpty()) {
            // First handle book quantities for non-returned borrowings
            for (Borrowing borrowing : allBorrowings) {
                if (borrowing.getStatus() != BorrowingStatus.RETURNED) {
                    Book book = borrowing.getBook();
                    book.setQuantity(book.getQuantity() + 1);
                    if (!book.isAvailable()) {
                        book.setAvailable(true);
                    }
                    bookRepository.save(book);
                }
            }
            
            borrowingRepository.deleteAll(allBorrowings);
        }

        userRepository.delete(user);
        return userMapper.toResponse(user);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchByName(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchByName(searchTerm, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String role, int page, int size) {
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByRole(userRole, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsersByRole(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByRoleAndStatus(UserRole.valueOf(role.toUpperCase()), UserStatus.ACTIVE, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllByStatus(UserStatus.ACTIVE, pageable);
        return userPage.map(userMapper::toResponse);
    }
}
