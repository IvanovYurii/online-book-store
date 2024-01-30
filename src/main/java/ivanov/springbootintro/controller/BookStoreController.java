package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.dto.book.UpdateBookRequestDto;
import ivanov.springbootintro.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book management", description = "Endpoints for managing books. "
        + "These endpoints provide operations related to book management, including "
        + "retrieving a list of all available books, finding detailed information about a "
        + "specific book by its ID, creating a new book, updating information about "
        + "a specific book, and deleting a book. "
        + "Certain operations may require administrative privileges.")

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/books")
public class BookStoreController {
    private final BookService bookService;

    @GetMapping
    @Operation(
            summary = "Get all books",
            description = "Retrieve a list of all available books. This endpoint is accessible"
                    + " to authenticated users and returns detailed information about each book, "
                    + "including book ID, title, author, price, description, coverImage and "
                    + "category IDs."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<BookDto> findAllBooks(
            @ParameterObject Pageable pageable
    ) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find book by id",
            description = "Retrieve detailed information about a specific book by its ID. "
                    + "This endpoint is accessible to authenticated users and returns information "
                    + "such as book ID, title, author, price, description, coverImage and "
                    + "category IDs."
    )
    public BookDto findBookById(
            @PathVariable @Valid @Min(1) Long id
    ) {
        return bookService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a new book",
            description = "Create a new book with the provided information "
                    + "such as book ID, title, author, price, description, coverImage and "
                    + "category IDs. This operation requires the user to have the role ADMIN."
    )
    public BookDto createBook(
            @RequestBody @Valid CreateBookRequestDto requestDto
    ) {
        return bookService.create(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Update book by id",
            description = "Update information about a specific book by its ID. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public BookDto updateBookById(
            @RequestBody UpdateBookRequestDto requestDto,
            @PathVariable @Min(1) Long id
    ) {
        return bookService.updateById(requestDto, id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete book by id",
            description = "Delete a specific book by its ID. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public void deleteBookById(
            @PathVariable @Min(1) Long id
    ) {
        bookService.deleteById(id);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search book by parameters",
            description = "Search for books based on specified parameters, such as title and/or "
                    + "authors. This endpoint is accessible to all users and returns a list of "
                    + "matching books with detailed information."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<BookDto> search(
            BookSearchParameters searchParameters,
            @ParameterObject Pageable pageable
    ) {
        return bookService.search(searchParameters, pageable);
    }
}
