package tr.com.eaaslan.library.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import tr.com.eaaslan.library.model.Borrowing;
import tr.com.eaaslan.library.model.BorrowingStatus;
import tr.com.eaaslan.library.model.dto.borrowing.BorrowingResponse;

import java.time.LocalDate;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BorrowingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userName", expression = "java(borrowing.getUser().getFirstName() + \" \" + borrowing.getUser().getLastName())")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookIsbn", source = "book.isbn")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "returnDate", source = "returnDate")
    @Mapping(target = "borrowDate", source = "borrowDate")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "returnedLate", source = "returnedLate")
    BorrowingResponse toResponse(Borrowing borrowing);

    @Named("statusToString")
    default String statusToString(BorrowingStatus status) {
        return status != null ? status.name() : null;
    }
    
}