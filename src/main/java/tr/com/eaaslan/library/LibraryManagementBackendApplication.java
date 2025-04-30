package tr.com.eaaslan.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LibraryManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementBackendApplication.class, args);
    }

//    @Bean
//    CommandLineRunner run(CsvDataImporter csvDataImporter) {
//        return args -> {
//            InputStream inputStream = getClass().getResourceAsStream("/ready_books.csv");
//            if (inputStream != null) {
//                csvDataImporter.importBooksFromCsv(inputStream);
//            } else {
//                System.err.println("books.csv not found in resources.");
//            }
//        };
//    }
}
