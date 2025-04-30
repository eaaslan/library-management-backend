package tr.com.eaaslan.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Year;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_book_isbn", columnList = "isbn", unique = true),
        @Index(name = "idx_book_title", columnList = "title")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Book extends BaseEntity {

    @Column(name = "isbn", unique = true, nullable = false)
//    @Pattern(regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$",
//            message = "ISBN must be a valid 10 or 13-digit number, with optional hyphens")
    @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 characters")
    @NotBlank(message = "ISBN is required")
    private String isbn;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Title is required")
    private String title;

    @Column(name = "author", nullable = false)
    @NotBlank(message = "Author is required")
    private String author;

    @Column(name = "publication_year", nullable = false)
    @NotNull(message = "Publication year is required")
    @Past(message = "Publication year must be in the past")
    private Year publicationYear;

    @Column(name = "publisher", nullable = false)
    @NotBlank(message = "Publisher is required")
    private String publisher;

    @NotNull(message = "Genre is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private Genre genre;

    @Column(name = "image_url")
    private String imageUrl;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return isbn != null && isbn.equals(book.isbn);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + (isbn != null ? isbn.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + getId() +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }


}
