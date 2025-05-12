package tr.com.eaaslan.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tr.com.eaaslan.library.model.event.BookAvailabilityEvent;
import tr.com.eaaslan.library.service.BookAvailabilityEventService;

@RestController
@RequestMapping("/books/availability")  // Removed /api/v1 prefix
@RequiredArgsConstructor
public class BookAvailabilityController {

    private final BookAvailabilityEventService eventService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookAvailabilityEvent> streamBookAvailability() {
        return eventService.getEventStream();
    }
}