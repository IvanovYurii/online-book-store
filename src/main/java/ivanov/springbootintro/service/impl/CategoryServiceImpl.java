package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.CategoryMapper;
import ivanov.springbootintro.model.Category;
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
        Category category = assertCategoryExistsById(id);
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto updateById(CreateCategoryRequestDto requestDto, Long id) {
        assertCategoryExistsById(id);
        Category category = categoryMapper.toEntity(requestDto);
        category.setId(id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        assertCategoryExistsById(id);
        categoryRepository.deleteById(id);
    }

    private Category assertCategoryExistsById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id=" + id));
    }
}
