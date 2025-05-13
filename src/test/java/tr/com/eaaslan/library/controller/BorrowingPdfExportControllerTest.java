package tr.com.eaaslan.library.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.eaaslan.library.service.BorrowingPdfExportService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowingPdfExportController.class)
class BorrowingPdfExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BorrowingPdfExportService pdfExportService;

    private byte[] samplePdfContent;

    @BeforeEach
    void setUp() {
        // Create sample PDF content for testing
        samplePdfContent = "Sample PDF Content".getBytes();
    }

    @Test
    @DisplayName("Should export all borrowings to PDF successfully")
    void shouldExportAllBorrowingsSuccessfully() throws Exception {
        // Given
        when(pdfExportService.exportAllBorrowingsToPdf()).thenReturn(samplePdfContent);

        // When & Then
        mockMvc.perform(get("/export/borrowings/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=all_borrowings.pdf"))
                .andExpect(header().string("Content-Length", String.valueOf(samplePdfContent.length)))
                .andExpect(content().bytes(samplePdfContent));
    }

    @Test
    @DisplayName("Should export user borrowings to PDF successfully")
    void shouldExportUserBorrowingsSuccessfully() throws Exception {
        // Given
        Long userId = 1L;
        when(pdfExportService.exportUserBorrowingsToPdf(userId)).thenReturn(samplePdfContent);

        // When & Then
        mockMvc.perform(get("/export/borrowings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=user_1_borrowings.pdf"))
                .andExpect(content().bytes(samplePdfContent));
    }

    @Test
    @DisplayName("Should export overdue borrowings to PDF successfully")
    void shouldExportOverdueBorrowingsSuccessfully() throws Exception {
        // Given
        when(pdfExportService.exportOverdueBorrowingsToPdf()).thenReturn(samplePdfContent);

        // When & Then
        mockMvc.perform(get("/export/borrowings/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=overdue_borrowings.pdf"))
                .andExpect(content().bytes(samplePdfContent));
    }

    @Test
    @DisplayName("Should export book borrowings to PDF successfully")
    void shouldExportBookBorrowingsSuccessfully() throws Exception {
        // Given
        Long bookId = 1L;
        when(pdfExportService.exportBookBorrowingsToPdf(bookId)).thenReturn(samplePdfContent);

        // When & Then
        mockMvc.perform(get("/export/borrowings/book/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=book_1_borrowings.pdf"))
                .andExpect(content().bytes(samplePdfContent));
    }

    @Test
    @DisplayName("Should export borrowings by date range to PDF successfully")
    void shouldExportBorrowingsByDateRangeSuccessfully() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        when(pdfExportService.exportBorrowingsByDateRangeToPdf(startDate, endDate)).thenReturn(samplePdfContent);

        // When & Then
        mockMvc.perform(get("/export/borrowings/date-range")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=borrowings_date_range.pdf"))
                .andExpect(content().bytes(samplePdfContent));
    }

    @Test
    @DisplayName("Should return bad request for invalid date format")
    void shouldReturnBadRequestForInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/export/borrowings/date-range")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when startDate is missing")
    void shouldReturnBadRequestWhenStartDateIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/export/borrowings/date-range")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when endDate is missing")
    void shouldReturnBadRequestWhenEndDateIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/export/borrowings/date-range")
                        .param("startDate", "2023-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when both parameters are missing")
    void shouldReturnBadRequestWhenBothParametersAreMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/export/borrowings/date-range"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void shouldHandleServiceExceptionGracefully() throws Exception {
        // Given
        when(pdfExportService.exportAllBorrowingsToPdf()).thenThrow(new RuntimeException("PDF generation failed"));

        // When & Then
        mockMvc.perform(get("/export/borrowings/all"))
                .andExpect(status().isInternalServerError());
    }
}