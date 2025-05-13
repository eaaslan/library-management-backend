package tr.com.eaaslan.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tr.com.eaaslan.library.model.event.BookAvailabilityEvent;
import tr.com.eaaslan.library.service.BookAvailabilityEventService;

@RestController
@RequestMapping("/books/availability")
@RequiredArgsConstructor
@Tag(
        name = "Book Availability Streaming",
        description = "Real-time streaming endpoints for book availability changes. " +
                "Uses Server-Sent Events (SSE) to provide live updates when books are borrowed or returned."
)
public class BookAvailabilityController {

    private final BookAvailabilityEventService eventService;


    @Operation(
            summary = "Stream book availability changes",
            description = "Provides a real-time stream of book availability events using Server-Sent Events (SSE). " +
                    "Clients will receive notifications whenever a book is borrowed or returned, " +
                    "allowing for real-time updates in user interfaces. " +
                    "The stream remains open and continuously sends events as they occur."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stream successfully established. Events will be sent as they occur.",
                    content = @Content(
                            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                            schema = @Schema(implementation = BookAvailabilityEvent.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error. Stream could not be established."
            )
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookAvailabilityEvent> streamBookAvailability() {
        return eventService.getEventStream();
    }
}