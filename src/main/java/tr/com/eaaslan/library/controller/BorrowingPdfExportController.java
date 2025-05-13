package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST Controller for exporting borrowing reports in PDF format.
 * <p>
 * This controller provides various endpoints to export borrowing data as PDF files:
 * - Complete borrowing reports (admin only)
 * - User-specific borrowing reports (admin or own data)
 * - Overdue borrowing reports (admin/librarian only)
 * - Book-specific borrowing history (admin/librarian only)
 * - Date range-based borrowing reports (admin/librarian only)
 * <p>
 * All endpoints return PDF documents as binary data with appropriate HTTP headers
 * for file download functionality.
 */
@RestController
@RequestMapping("/export/borrowings")
@RequiredArgsConstructor
@Tag(
        name = "Borrowing PDF Export",
        description = "APIs for exporting borrowing reports to PDF format. " +
                "Different endpoints have different access levels based on user roles."
)
public class BorrowingPdfExportController {

    private final BorrowingPdfExportService borrowingPdfExportService;

    /**
     * Exports all borrowing records to PDF format.
     * Only administrators can access this endpoint as it contains all users' data.
     */
    @Operation(
            summary = "Export all borrowings to PDF",
            description = "Generates a comprehensive PDF report containing all borrowing records in the system. " +
                    "This is a sensitive operation that exposes all users' borrowing data, " +
                    "therefore it's restricted to administrators only."

    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF report generated successfully. Returns binary PDF data as attachment."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. Only administrators can access this endpoint."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error occurred during PDF generation."
            )
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAllBorrowings() {
        byte[] pdfBytes = borrowingPdfExportService.exportAllBorrowingsToPdf();
        return createPdfResponse(pdfBytes, "all_borrowings.pdf");
    }

    /**
     * Exports borrowing records for a specific user.
     * Users can export their own data, while admins can export any user's data.
     */
    @Operation(
            summary = "Export borrowings for a specific user",
            description = "Generates a PDF report containing all borrowing records for a specific user. " +
                    "Access control: Users can only export their own borrowing history, " +
                    "while administrators can export any user's data."

    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User borrowings PDF generated successfully."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. Users can only access their own data, admins can access any user's data."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found with the specified ID."
            )
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<byte[]> exportUserBorrowings(
            @Parameter(
                    description = "ID of the user whose borrowing history will be exported",
                    required = true,
                    example = "3"
            )
            @PathVariable Long userId) {
        byte[] pdfBytes = borrowingPdfExportService.exportUserBorrowingsToPdf(userId);
        return createPdfResponse(pdfBytes, "user_" + userId + "_borrowings.pdf");
    }

    /**
     * Exports all overdue borrowing records.
     * Limited to library staff for identifying and managing overdue items.
     */
    @Operation(
            summary = "Export overdue borrowings",
            description = "Generates a PDF report containing all borrowing records that are past their due date. " +
                    "This report helps library staff identify and follow up on overdue items. " +
                    "Access is restricted to librarians and administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Overdue borrowings PDF generated successfully."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. Only librarians and administrators can access this endpoint."
            )
    })
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportOverdueBorrowings() {
        byte[] pdfBytes = borrowingPdfExportService.exportOverdueBorrowingsToPdf();
        return createPdfResponse(pdfBytes, "overdue_borrowings.pdf");
    }

    /**
     * Exports borrowing history for a specific book.
     * Provides insights into a book's circulation history for library management.
     */
    @Operation(
            summary = "Export borrowing history for a specific book",
            description = "Generates a PDF report showing the complete borrowing history of a specific book. " +
                    "This helps track how often a book is borrowed, by whom, and when. " +
                    "Useful for collection management and understanding book popularity."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book borrowing history PDF generated successfully."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. Only librarians and administrators can access this endpoint."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found with the specified ID."
            )
    })
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportBookBorrowings(
            @Parameter(
                    description = "ID of the book whose borrowing history will be exported",
                    required = true,
                    example = "1"
            )
            @PathVariable Long bookId) {
        byte[] pdfBytes = borrowingPdfExportService.exportBookBorrowingsToPdf(bookId);
        return createPdfResponse(pdfBytes, "book_" + bookId + "_borrowings.pdf");
    }

    /**
     * Exports borrowing records within a specified date range.
     * Useful for generating periodic reports and analyzing borrowing patterns.
     */
    @Operation(
            summary = "Export borrowings within a date range",
            description = "Generates a PDF report containing all borrowing records within the specified date range. " +
                    "This is useful for generating monthly, quarterly, or yearly reports. " +
                    "Both startDate and endDate are required and must be in ISO date format (YYYY-MM-DD)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Date range borrowings PDF generated successfully."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request. Invalid date format or missing required parameters."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. Only librarians and administrators can access this endpoint."
            )
    })
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportBorrowingsByDateRange(
            @Parameter(
                    description = "Start date for the report range (inclusive)",
                    required = true,
                    example = "2023-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    description = "End date for the report range (inclusive)",
                    required = true,
                    example = "2023-12-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        byte[] pdfBytes = borrowingPdfExportService.exportBorrowingsByDateRangeToPdf(startDate, endDate);
        return createPdfResponse(pdfBytes, "borrowings_date_range.pdf");
    }

    /**
     * Helper method to create standardized PDF response with appropriate headers.
     * <p>
     * Sets the following HTTP headers:
     * - Content-Type: application/pdf
     * - Content-Disposition: attachment (triggers download)
     * - Content-Length: size of PDF file
     *
     * @param pdfBytes The PDF content as byte array
     * @param fileName The filename for the downloaded PDF
     * @return ResponseEntity with PDF content and appropriate headers
     */
    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfBytes, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(pdfBytes.length);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}