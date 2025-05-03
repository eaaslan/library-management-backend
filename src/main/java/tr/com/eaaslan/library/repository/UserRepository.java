package tr.com.eaaslan.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;
import tr.com.eaaslan.library.model.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);
}