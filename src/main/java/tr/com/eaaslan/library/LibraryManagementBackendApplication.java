package tr.com.eaaslan.library;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tr.com.eaaslan.library.util.CsvDataImporter;

import java.io.InputStream;


@SpringBootApplication
@EnableScheduling
public class LibraryManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementBackendApplication.class, args);
    }

//    @Bean
//    CommandLineRunner run(CsvDataImporter csvDataImporter) {
//        return args -> {
//            InputStream inputStream = getClass().getResourceAsStream("/first_50_books_corrected.csv");
//            if (inputStream != null) {
//                csvDataImporter.importBooksFromCsv(inputStream);
//            } else {
//                System.err.println("books.csv not found in resources.");
//            }
//        };
//    }
}
