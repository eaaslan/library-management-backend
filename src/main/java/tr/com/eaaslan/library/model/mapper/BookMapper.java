package tr.com.eaaslan.library.model.mapper;

import org.mapstruct.*;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;
import tr.com.eaaslan.library.model.dto.Book.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.Book.BookResponse;
import tr.com.eaaslan.library.model.dto.Book.BookUpdateRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    @Mapping(target = "publicationYear", expression = "java(book.getPublicationYear().getValue())")
    @Mapping(target = "genre", source = "genre", qualifiedByName = "genreToString")
    BookResponse toResponse(Book book);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "publicationYear", expression = "java(java.time.Year.of(bookCreateRequest.publicationYear()))")
    @Mapping(target = "genre", source = "genre", qualifiedByName = "stringToGenre")
    Book toEntity(BookCreateRequest bookCreateRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "publicationYear", expression = "java(bookUpdateRequest.publicationYear() != null ? java.time.Year.of(bookUpdateRequest.publicationYear()) : book.getPublicationYear())")
    @Mapping(target = "genre", expression = "java(bookUpdateRequest.genre() != null ? stringToGenre(bookUpdateRequest.genre()) : book.getGenre())")
    @Mapping(target = "isbn", ignore = true)
        // ISBN should not be updated
    void updateEntity(BookUpdateRequest bookUpdateRequest, @MappingTarget Book book);

    @Named("stringToGenre")
    default Genre stringToGenre(String genre) {
        if (genre == null) {
            return null;
        }
        try {
            return Genre.valueOf(genre);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid genre: " + genre);
        }
    }

    @Named("genreToString")
    default String genreToString(Genre genre) {
        if (genre == null) {
            return null;
        }
        return genre.name();
    }

    // Add a debugging method to inspect the input
    default void logBookCreateRequest(BookCreateRequest request) {
        System.out.println("Received BookCreateRequest:");
        System.out.println("ISBN: " + request.isbn());
        System.out.println("Title: " + request.title());
        System.out.println("Author: " + request.author());
        System.out.println("Publication Year: " + request.publicationYear());
        System.out.println("Publisher: " + request.publisher());
        System.out.println("Genre: " + request.genre());
    }
}