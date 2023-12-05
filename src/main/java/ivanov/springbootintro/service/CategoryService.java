package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto create(CreateCategoryRequestDto requestDto);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long id);

    CategoryDto updateById(CreateCategoryRequestDto requestDto, Long id);

    void deleteById(Long id);

    List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id, Pageable pageable);
}
