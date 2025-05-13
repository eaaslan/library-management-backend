package tr.com.eaaslan.library.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingPdfExportService {

    private final BorrowingService borrowingService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public byte[] exportAllBorrowingsToPdf() {
        log.info("Exporting all borrowings to PDF");
        List<BorrowingResponse> borrowings = borrowingService.getAllBorrowingsForExport();
        return generateBorrowingsPdf(borrowings, "All Borrowings Report");
    }

    public byte[] exportUserBorrowingsToPdf(Long userId) {
        log.info("Exporting borrowings for user {} to PDF", userId);
        List<BorrowingResponse> borrowings = borrowingService.getBorrowingsByUserForExport(userId);
        return generateBorrowingsPdf(borrowings, "User Borrowings Report - User ID: " + userId);
    }

    public byte[] exportOverdueBorrowingsToPdf() {
        log.info("Exporting overdue borrowings to PDF");
        List<BorrowingResponse> borrowings = borrowingService.getOverdueBorrowingsForExport();
        return generateBorrowingsPdf(borrowings, "Overdue Borrowings Report");
    }

    public byte[] exportBookBorrowingsToPdf(Long bookId) {
        log.info("Exporting borrowings for book {} to PDF", bookId);
        List<BorrowingResponse> borrowings = borrowingService.getBorrowingsByBookForExport(bookId);
        return generateBorrowingsPdf(borrowings, "Book Borrowings Report - Book ID: " + bookId);
    }

    public byte[] exportBorrowingsByDateRangeToPdf(LocalDate startDate, LocalDate endDate) {
        log.info("Exporting borrowings for date range {} to {} to PDF", startDate, endDate);
        List<BorrowingResponse> borrowings = borrowingService.getBorrowingsByDateRangeForExport(startDate, endDate);
        String dateRange = formatDateRange(startDate, endDate);
        return generateBorrowingsPdf(borrowings, "Borrowings Report - " + dateRange);
    }

    private byte[] generateBorrowingsPdf(List<BorrowingResponse> borrowings, String title) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(title)
                    .setFontSize(18)
                    .setMarginBottom(20));

            document.add(new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setMarginBottom(10));

            document.add(new Paragraph("Total Records: " + borrowings.size())
                    .setFontSize(10)
                    .setMarginBottom(20));

            if (!borrowings.isEmpty()) {
                createBorrowingsTable(document, borrowings);
            } else {
                document.add(new Paragraph("No borrowings found for the selected criteria.")
                        .setFontSize(12)
                        .setMarginBottom(20));

            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void createBorrowingsTable(Document document, List<BorrowingResponse> borrowings) {
        // Create table with 7 columns
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 15, 10, 10, 10, 10}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell("ID");
        table.addHeaderCell("User");
        table.addHeaderCell("Book");
        table.addHeaderCell("Borrow Date");
        table.addHeaderCell("Due Date");
        table.addHeaderCell("Return Date");
        table.addHeaderCell("Status");

        for (BorrowingResponse borrowing : borrowings) {
            table.addCell(String.valueOf(borrowing.id()));
            table.addCell(borrowing.userName());
            table.addCell(borrowing.bookTitle());
            table.addCell(borrowing.borrowDate().format(DATE_FORMATTER)).setFontSize(8);
            table.addCell(borrowing.dueDate().format(DATE_FORMATTER));
            table.addCell(borrowing.returnDate() != null ?
                    borrowing.returnDate().format(DATE_FORMATTER) : "Not returned");
            table.addCell(borrowing.status());
        }

        document.add(table);
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "All dates";
        } else if (startDate == null) {
            return "Up to " + endDate.format(DATE_FORMATTER);
        } else if (endDate == null) {
            return "From " + startDate.format(DATE_FORMATTER);
        } else {
            return startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
        }
    }
}