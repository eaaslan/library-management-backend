package tr.com.eaaslan.library.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingCreateRequest;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingReturnRequest;
import tr.com.eaaslan.library.service.BorrowingService;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping
    public ResponseEntity<BorrowingResponse> borrowBook(
            @Valid @RequestBody BorrowingCreateRequest borrowingCreateRequest,
            Principal principal) {
        // For patrons, they can only borrow books for themselves
        BorrowingResponse response = borrowingService.borrowBook(borrowingCreateRequest, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(
            @PathVariable Long id,
            @Valid @RequestBody BorrowingReturnRequest returnRequest,
            Principal principal) {
        BorrowingResponse response = borrowingService.returnBook(id, returnRequest, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Page<BorrowingResponse>> getAllBorrowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "borrowDate") String sortBy) {
        return ResponseEntity.ok(borrowingService.getAllBorrowings(page, size, sortBy));
    }

    @GetMapping("/my-borrowings")
    public ResponseEntity<Page<BorrowingResponse>> getMyBorrowings(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByCurrentUser(principal.getName(), page, size));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Page<BorrowingResponse>> getBorrowingsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByUser(userId, page, size));
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Page<BorrowingResponse>> getBorrowingsByBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowingService.getBorrowingsByBook(bookId, page, size));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Page<BorrowingResponse>> getOverdueBorrowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowingService.getOverdueBorrowings(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or @securityService.canAccessBorrowing(#id)")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowingService.getBorrowingById(id));
    }
}