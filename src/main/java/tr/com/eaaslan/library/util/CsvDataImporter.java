//package tr.com.eaaslan.library.util;
//
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import tr.com.eaaslan.library.model.Book;
//import tr.com.eaaslan.library.model.Genre;
//import tr.com.eaaslan.library.repository.BookRepository;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.time.Year;
//
//@Component
//public class CsvDataImporter {
//
//    private static final Logger logger = LoggerFactory.getLogger(CsvDataImporter.class);
//    private final BookRepository bookRepository;
//
//    public CsvDataImporter(BookRepository bookRepository) {
//        this.bookRepository = bookRepository;
//    }
//
//    public void importBooksFromCsv(InputStream inputStream) {
//        try (CSVParser csvParser = CSVFormat.DEFAULT
//                .withFirstRecordAsHeader()
//                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
//
//            for (CSVRecord record : csvParser) {
//                try {
//                    String genreStr = record.get("genre").trim().toUpperCase();
//                    Genre genre;
//                    try {
//                        genre = Genre.valueOf(genreStr);
//                    } catch (IllegalArgumentException e) {
//                        logger.warn("Skipping record due to invalid genre: {}", genreStr);
//                        continue;
//                    }
//
//                    Book book = Book.builder()
//                            .isbn(record.get("isbn").trim())
//                            .title(record.get("title").trim())
//                            .author(record.get("author").trim())
//                            .publicationYear(Year.of(Integer.parseInt(record.get("publication_year").trim())))
//                            .publisher(record.get("publisher").trim())
//                            .imageUrl(record.get("image_url").trim())
//                            .description(record.get("description").trim())
//                            .genre(genre)
//                            .available(Boolean.parseBoolean(record.get("available").trim()))
//                            .quantity(Integer.parseInt(record.get("quantity").trim()))
//                            .build();
//
//                    bookRepository.save(book);
//                } catch (Exception e) {
//                    logger.warn("Skipping record due to error: {}", e.getMessage());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Failed to import books from CSV: {}", e.getMessage());
//        }
//    }
//}
