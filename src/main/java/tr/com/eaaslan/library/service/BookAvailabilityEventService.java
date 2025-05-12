package tr.com.eaaslan.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.event.BookAvailabilityEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookAvailabilityEventService {

    private final Sinks.Many<BookAvailabilityEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publishBookAvailabilityChange(Book book) {
        BookAvailabilityEvent event = new BookAvailabilityEvent(
                book.getId(),
                book.getTitle(),
                book.isAvailable(),
                book.getQuantity(),
                LocalDateTime.now()
        );
        sink.tryEmitNext(event);
    }

    public Flux<BookAvailabilityEvent> getEventStream() {
        return sink.asFlux();
    }
}