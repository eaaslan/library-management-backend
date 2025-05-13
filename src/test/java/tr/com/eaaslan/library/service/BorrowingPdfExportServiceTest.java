package tr.com.eaaslan.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.com.eaaslan.library.model.BorrowingStatus;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingPdfExportServiceTest {

    @Mock
    private BorrowingService borrowingService;

    @InjectMocks
    private BorrowingPdfExportService pdfExportService;

    private List<BorrowingResponse> sampleBorrowings;
    private List<BorrowingResponse> emptyBorrowings;

    @BeforeEach
    void setUp() {
        // Create sample borrowing data for testing
        sampleBorrowings = createSampleBorrowings();
        emptyBorrowings = new ArrayList<>();
    }

    @Test
    @DisplayName("Should export all borrowings to PDF successfully")
    void shouldExportAllBorrowingsToPdfSuccessfully() {
        // Given
        when(borrowingService.getAllBorrowingsForExport()).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportAllBorrowingsToPdf();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getAllBorrowingsForExport();
    }

    @Test
    @DisplayName("Should handle empty borrowings list gracefully")
    void shouldHandleEmptyBorrowingsListGracefully() {
        // Given
        when(borrowingService.getAllBorrowingsForExport()).thenReturn(emptyBorrowings);

        // When
        byte[] result = pdfExportService.exportAllBorrowingsToPdf();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getAllBorrowingsForExport();
    }

    @Test
    @DisplayName("Should export user borrowings to PDF successfully")
    void shouldExportUserBorrowingsToPdf() {
        // Given
        Long userId = 1L;
        when(borrowingService.getBorrowingsByUserForExport(userId)).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportUserBorrowingsToPdf(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByUserForExport(userId);
    }

    @Test
    @DisplayName("Should export overdue borrowings to PDF successfully")
    void shouldExportOverdueBorrowingsToPdf() {
        // Given
        when(borrowingService.getOverdueBorrowingsForExport()).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportOverdueBorrowingsToPdf();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getOverdueBorrowingsForExport();
    }

    @Test
    @DisplayName("Should export book borrowings to PDF successfully")
    void shouldExportBookBorrowingsToPdf() {
        // Given
        Long bookId = 1L;
        when(borrowingService.getBorrowingsByBookForExport(bookId)).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportBookBorrowingsToPdf(bookId);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByBookForExport(bookId);
    }

    @Test
    @DisplayName("Should export borrowings by date range to PDF successfully")
    void shouldExportBorrowingsByDateRangeToPdf() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        when(borrowingService.getBorrowingsByDateRangeForExport(startDate, endDate)).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportBorrowingsByDateRangeToPdf(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByDateRangeForExport(startDate, endDate);
    }

    @Test
    @DisplayName("Should export with null dates successfully")
    void shouldExportWithNullDatesSuccessfully() {
        // Given
        when(borrowingService.getBorrowingsByDateRangeForExport(null, null)).thenReturn(sampleBorrowings);

        // When
        byte[] result = pdfExportService.exportBorrowingsByDateRangeToPdf(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByDateRangeForExport(null, null);
    }

    @Test
    @DisplayName("Should throw exception when borrowing service throws exception")
    void shouldThrowExceptionWhenServiceThrowsException() {
        // Given
        when(borrowingService.getAllBorrowingsForExport()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> pdfExportService.exportAllBorrowingsToPdf());
        verify(borrowingService, times(1)).getAllBorrowingsForExport();
    }

    private List<BorrowingResponse> createSampleBorrowings() {
        return List.of(
                new BorrowingResponse(
                        1L, 1L, "user@test.com", "John Doe",
                        1L, "Test Book", "123456789",
                        LocalDate.now().minusDays(7),
                        LocalDate.now().plusDays(7),
                        null,
                        BorrowingStatus.ACTIVE.name(),
                        false,
                        LocalDateTime.now(),
                        "system"
                ),
                new BorrowingResponse(
                        2L, 2L, "user2@test.com", "Jane Smith",
                        2L, "Another Book", "987654321",
                        LocalDate.now().minusDays(15),
                        LocalDate.now().minusDays(1),
                        LocalDate.now(),
                        BorrowingStatus.RETURNED.name(),
                        true,
                        LocalDateTime.now(),
                        "system"
                )
        );
    }
}