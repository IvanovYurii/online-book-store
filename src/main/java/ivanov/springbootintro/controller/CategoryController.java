package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.service.CategoryService;
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

@Tag(name = "Category management", description = "Endpoints for managing categories")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories",
            description = "Get a list of all available categories")
    public List<CategoryDto> getAll(Authentication authentication, Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find category by id",
            description = "Retrieve a specific category by its ID")
    public CategoryDto getCategoryById(Authentication authentication,
                                       @PathVariable @Valid Long id) {
        return categoryService.getById(id);
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "List books by category",
            description = "Retrieve books by a specific category")
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Authentication authentication,
                                                                @PathVariable @Valid Long id) {
        return categoryService.getBooksByCategoryId(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new category", description = "Create a new category")
    public CategoryDto createCategory(Authentication authentication,
                                      @RequestBody @Valid CreateCategoryRequestDto requestDto) {
        return categoryService.save(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a specific category",
            description = "Update category by available id")
    public CategoryDto updateCategory(Authentication authentication,
                                      @PathVariable @Valid Long id,
                                      @RequestBody @Valid CreateCategoryRequestDto requestDto
    ) {
        return categoryService.updateById(requestDto, id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category by id", description = "Delete category by available id")
    public void deleteCategory(Authentication authentication, @PathVariable @Valid Long id) {
        categoryService.deleteById(id);
    }
}
