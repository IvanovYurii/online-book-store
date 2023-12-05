package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.BookMapper;
import ivanov.springbootintro.mapper.CategoryMapper;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.category.CategoryRepository;
import ivanov.springbootintro.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public CategoryDto create(CreateCategoryRequestDto requestDto) {
        Category category = categoryMapper.toEntity(requestDto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id=" + id));
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto updateById(CreateCategoryRequestDto requestDto, Long id) {
        categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id=" + id));
        Category category = categoryMapper.toEntity(requestDto);
        category.setId(id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id=" + id));
        categoryRepository.deleteById(id);
    }

    @Override
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id, Pageable pageable) {
        return bookRepository.findAllByCategoriesId(id).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }
}
