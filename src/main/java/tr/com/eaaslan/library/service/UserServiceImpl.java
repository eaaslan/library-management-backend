package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tr.com.eaaslan.library.exception.ResourceAlreadyExistException;
import tr.com.eaaslan.library.exception.ResourceNotFoundException;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;
import tr.com.eaaslan.library.model.mapper.UserMapper;
import tr.com.eaaslan.library.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    //todo implement exceptions
    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toEntity(userCreateRequest);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistException("User", "email", user.getEmail());
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new ResourceAlreadyExistException("User", "phone number", user.getPhoneNumber());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse createPatronUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toEntity(userCreateRequest);

        // Explicitly set role and status for public registrations
        user.setRole(UserRole.PATRON);
        user.setStatus(UserStatus.PENDING);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistException("User", "email", user.getEmail());
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new ResourceAlreadyExistException("User", "phone number", user.getPhoneNumber());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        userMapper.updateEntity(userUpdateRequest, user);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        user.setDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());
        user.setDeletedBy("SYSTEM");
        //todo change this after implement security
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    //todo implement auth
    @Override
    public UserResponse updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        user.setStatus(UserStatus.valueOf(status));
        userRepository.save(user);
        return userMapper.toResponse(user);
    }


    @Override
    public Page<UserResponse> searchByName(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchByName(searchTerm, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByRole(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByRole(UserRole.valueOf(role), pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getActiveUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllByStatus(UserStatus.ACTIVE, pageable);
        return userPage.map(userMapper::toResponse);
    }
}
