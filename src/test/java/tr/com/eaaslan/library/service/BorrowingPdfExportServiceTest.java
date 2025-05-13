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

        sampleBorrowings = createSampleBorrowings();
        emptyBorrowings = new ArrayList<>();
    }

    @Test
    @DisplayName("Should export all borrowings to PDF successfully")
    void shouldExportAllBorrowingsToPdfSuccessfully() {

        when(borrowingService.getAllBorrowingsForExport()).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportAllBorrowingsToPdf();

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getAllBorrowingsForExport();
    }

    @Test
    @DisplayName("Should handle empty borrowings list gracefully")
    void shouldHandleEmptyBorrowingsListGracefully() {

        when(borrowingService.getAllBorrowingsForExport()).thenReturn(emptyBorrowings);

        byte[] result = pdfExportService.exportAllBorrowingsToPdf();

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getAllBorrowingsForExport();
    }

    @Test
    @DisplayName("Should export user borrowings to PDF successfully")
    void shouldExportUserBorrowingsToPdf() {

        Long userId = 1L;
        when(borrowingService.getBorrowingsByUserForExport(userId)).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportUserBorrowingsToPdf(userId);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByUserForExport(userId);
    }

    @Test
    @DisplayName("Should export overdue borrowings to PDF successfully")
    void shouldExportOverdueBorrowingsToPdf() {

        when(borrowingService.getOverdueBorrowingsForExport()).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportOverdueBorrowingsToPdf();

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getOverdueBorrowingsForExport();
    }

    @Test
    @DisplayName("Should export book borrowings to PDF successfully")
    void shouldExportBookBorrowingsToPdf() {

        Long bookId = 1L;
        when(borrowingService.getBorrowingsByBookForExport(bookId)).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportBookBorrowingsToPdf(bookId);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByBookForExport(bookId);
    }

    @Test
    @DisplayName("Should export borrowings by date range to PDF successfully")
    void shouldExportBorrowingsByDateRangeToPdf() {

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        when(borrowingService.getBorrowingsByDateRangeForExport(startDate, endDate)).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportBorrowingsByDateRangeToPdf(startDate, endDate);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByDateRangeForExport(startDate, endDate);
    }

    @Test
    @DisplayName("Should export with null dates successfully")
    void shouldExportWithNullDatesSuccessfully() {

        when(borrowingService.getBorrowingsByDateRangeForExport(null, null)).thenReturn(sampleBorrowings);

        byte[] result = pdfExportService.exportBorrowingsByDateRangeToPdf(null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(borrowingService, times(1)).getBorrowingsByDateRangeForExport(null, null);
    }

    @Test
    @DisplayName("Should throw exception when borrowing service throws exception")
    void shouldThrowExceptionWhenServiceThrowsException() {

        when(borrowingService.getAllBorrowingsForExport()).thenThrow(new RuntimeException("Database error"));

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