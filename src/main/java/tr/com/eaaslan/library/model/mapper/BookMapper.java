package tr.com.eaaslan.library.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.dto.BookCreateRequest;
import tr.com.eaaslan.library.model.dto.BookResponse;
import tr.com.eaaslan.library.model.dto.BookUpdateRequest;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponse toResponse(Book book);

    Book toEntity(BookCreateRequest bookCreateRequest);

    void updateEntity(BookUpdateRequest BookUpdateRequest, @MappingTarget Book book);
}
