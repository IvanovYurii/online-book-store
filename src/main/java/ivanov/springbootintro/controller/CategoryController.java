package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.service.CategoryService;
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

@Tag(name = "Category Management", description = "Endpoints for managing categories. "
        + "These endpoints provide operations related to categories management, including "
        + "retrieving a list of all available categories, finding detailed information about a "
        + "specific category by its ID, creating a new category, updating information about "
        + "a specific category, and deleting a category. "
        + "Certain operations may require administrative privileges.")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Retrieve a list of all available categories. This endpoint is "
                    + "accessible to authenticated users. It returns detailed information "
                    + "about each category, including ID, name and description."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<CategoryDto> getAllCategories(
            @ParameterObject Pageable pageable
    ) {
        return categoryService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find category by id",
            description = "Retrieve detailed information about a specific category by its ID. "
                    + "This endpoint is accessible to authenticated users and returns information"
                    + " such as categoryID, name and description"
    )
    public CategoryDto getCategoryById(
            @PathVariable @Min(1) Long id
    ) {
        return categoryService.getById(id);
    }

    @GetMapping("/{id}/books")
    @Operation(summary = "List books by category",
            description = "Retrieve detailed information about books associated with a specific "
                    + "category by its ID. This endpoint is accessible to authenticated users and "
                    + "returns information such as book ID, title, author, price, description and "
                    + "coverImage."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @PathVariable @Min(1) Long id,
            @ParameterObject Pageable pageable
    ) {
        return categoryService.getBooksByCategoryId(id, pageable);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create a new category",
            description = "This endpoint create a new category with the provided information,  "
                    + "including name, and description. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public CategoryDto createCategory(
            @RequestBody @Valid CreateCategoryRequestDto requestDto
    ) {
        return categoryService.create(requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a specific category",
            description = "This endpoint update information about a specific category by its ID. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public CategoryDto updateCategory(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid CreateCategoryRequestDto requestDto
    ) {
        return categoryService.updateById(requestDto, id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete category by id",
            description = "Delete a specific category by its ID. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public void deleteCategory(
            @PathVariable @Min(1) Long id
    ) {
        categoryService.deleteById(id);
    }
}
