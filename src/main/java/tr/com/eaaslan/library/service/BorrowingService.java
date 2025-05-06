package tr.com.eaaslan.library.service;

import org.springframework.data.domain.Page;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;

public interface BorrowingService {

    BorrowingResponse borrowBook(Long id, BorrowingCreateRequest borrowingCreateRequest, String currentUserEmail);

    BorrowingResponse returnBook(Long id, BorrowingReturnRequest returnRequest, String currentUserEmail);

    Page<BorrowingResponse> getAllBorrowings(int page, int size, String sortBy);

    Page<BorrowingResponse> getBorrowingsByUser(Long userId, int page, int size);

    Page<BorrowingResponse> getBorrowingsByCurrentUser(String userEmail, int page, int size);

    Page<BorrowingResponse> getBorrowingsByBook(Long bookId, int page, int size);

    Page<BorrowingResponse> getOverdueBorrowings(int page, int size);

    BorrowingResponse getBorrowingById(Long id);
}