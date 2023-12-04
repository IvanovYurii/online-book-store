package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto createCategory(CreateCategoryRequestDto requestDto);

    List<CategoryDto> getAllCategories(Pageable pageable);

    CategoryDto getCategoryById(Long id);

    CategoryDto updateCategoryById(CreateCategoryRequestDto requestDto, Long id);

    void deleteCategoryById(Long id);

    List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id, Pageable pageable);
}
