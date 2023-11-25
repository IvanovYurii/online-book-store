package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book management", description = "Endpoints for managing books")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/books")
public class BookStoreController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books", description = "Get a list of all available books")
    public List<BookDto> findAll(Authentication authentication, Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find book by id", description = "Find book by available id")
    public BookDto findById(Authentication authentication, @PathVariable @Valid Long id) {
        return bookService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new book", description = "Create a new book")
    public BookDto create(Authentication authentication,
                          @RequestBody @Valid CreateBookRequestDto requestDto) {
        return bookService.save(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update book by id", description = "Update book by available id")
    public BookDto updateById(Authentication authentication,
                              @RequestBody @Valid CreateBookRequestDto requestDto,
                              @PathVariable @Valid Long id) {
        return bookService.updateById(requestDto, id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book by id", description = "Delete book by available id")
    public void deleteById(Authentication authentication, @PathVariable @Valid Long id) {
        bookService.deleteById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search book by parameters",
            description = "Search book by parameters title and/or authors")
    public List<BookDto> search(Authentication authentication,
                                BookSearchParameters searchParameters) {
        return bookService.search(searchParameters);
    }
}
