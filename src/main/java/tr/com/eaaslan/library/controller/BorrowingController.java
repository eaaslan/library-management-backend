package tr.com.eaaslan.library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.com.eaaslan.library.repository.BorrowingRepository;

@RestController
@RequestMapping("/api/borrowing")
public class BorrowingController {

    private final BorrowingRepository borrowingRepository;

    public BorrowingController(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

    @GetMapping
}
