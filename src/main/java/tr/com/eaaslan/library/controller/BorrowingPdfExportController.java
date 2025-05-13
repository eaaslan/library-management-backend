package tr.com.eaaslan.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.com.eaaslan.library.service.BorrowingPdfExportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/export/borrowings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BorrowingPdfExportController {

    private final BorrowingPdfExportService borrowingPdfExportService;

    @GetMapping("/all")
    public ResponseEntity<byte[]> exportAllBorrowings() {
        byte[] pdfBytes = borrowingPdfExportService.exportAllBorrowingsToPdf();
        return createPdfResponse(pdfBytes, "all_borrowings.pdf");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<byte[]> exportUserBorrowings(@PathVariable Long userId) {
        byte[] pdfBytes = borrowingPdfExportService.exportUserBorrowingsToPdf(userId);
        return createPdfResponse(pdfBytes, "user_" + userId + "_borrowings.pdf");
    }

    @GetMapping("/overdue")
    public ResponseEntity<byte[]> exportOverdueBorrowings() {
        byte[] pdfBytes = borrowingPdfExportService.exportOverdueBorrowingsToPdf();
        return createPdfResponse(pdfBytes, "overdue_borrowings.pdf");
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<byte[]> exportBookBorrowings(@PathVariable Long bookId) {
        byte[] pdfBytes = borrowingPdfExportService.exportBookBorrowingsToPdf(bookId);
        return createPdfResponse(pdfBytes, "book_" + bookId + "_borrowings.pdf");
    }

    @GetMapping("/date-range")
    public ResponseEntity<byte[]> exportBorrowingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] pdfBytes = borrowingPdfExportService.exportBorrowingsByDateRangeToPdf(startDate, endDate);
        return createPdfResponse(pdfBytes, "borrowings_date_range.pdf");
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfBytes, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
