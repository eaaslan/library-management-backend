package tr.com.eaaslan.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.eaaslan.library.model.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    
}
