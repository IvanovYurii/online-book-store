package ivanov.springbootintro.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.CategoryMapper;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.category.CategoryRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private CreateCategoryRequestDto createCategoryRequestDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");
        category.setDescription("Fantasy books");

        categoryDto = new CategoryDto(
                1L,
                "Fantasy",
                "Fantasy books");

        createCategoryRequestDto = new CreateCategoryRequestDto(
                "Poem",
                "Poem books");
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    // Create
    @Test
    @DisplayName("""
            Given a valid RequestDto,
            When create is called,
            Then the corresponding CategoryDto should be returned.
            """)
    public void createCategory_WithValidRequestDto_ShouldReturnValidCategory() {
        // Given
        when(categoryMapper.toEntity(createCategoryRequestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        // When
        CategoryDto actualCategory = categoryService.create(createCategoryRequestDto);
        // Then
        assertThat(actualCategory).isEqualTo(categoryDto);
        EqualsBuilder.reflectionEquals(categoryDto, actualCategory);
        verify(categoryRepository, times(1)).save(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    // FindAll
    @Test
    @DisplayName("""
            Given a list of existing categories in the database,
            When the getAll method is called with pagination,
            Then the corresponding list of CategoryDto should be returned.
            """)
    public void getAllCategory_ExistingData_ShouldReturnListCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        // When
        List<CategoryDto> actualListCategory = categoryService.getAll(pageable);
        // Then
        assertThat(actualListCategory).hasSize(1);
        assertEquals(actualListCategory.get(0), categoryDto);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toDto(category);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            When getAll is called with no existing data,
            Then an empty list should be returned.
            """)
    public void getAllCategory_NoExistingData_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findAll(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        // When
        List<CategoryDto> actualListCategory = categoryService.getAll(Pageable.unpaged());
        // Then
        assertTrue(actualListCategory.isEmpty());
        verify(categoryRepository, times(1)).findAll(Pageable.unpaged());
        verifyNoMoreInteractions(categoryRepository);
    }

    // FindById
    @Test
    @DisplayName("""
            Given a valid category ID,
            When getById is called,
            Then the corresponding CategoryDto should be returned.
            """)
    public void getCategoryById_WithValidCategoryId_ShouldReturnValidCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        // When
        CategoryDto actualCategory = categoryService.getById(1L);
        // Then
        assertThat(actualCategory).isEqualTo(categoryDto);
        EqualsBuilder.reflectionEquals(categoryDto, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Given an invalid category ID,
            When getById is called,
            Then throw EntityNotFoundException should be thrown.
            """)
    public void getCategoryById_WithInvalidCategoryId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidCategoryId = 999L;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(invalidCategoryId));
        // Then
        String expected = "Can't find category by id=" + invalidCategoryId;
        assertEquals(expected, actual.getMessage());
        verify(categoryRepository, times(1)).findById(invalidCategoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    // UpdateById
    @Test
    @DisplayName("""
            Given a valid RequestDto,
            When updateById is called,
            Then the corresponding CategoryDto should be returned.
            """)
    public void updateCategoryById_WithValidRequestDto_ShouldReturnValidCategory() {
        // Given
        categoryDto = new CategoryDto(
                1L,
                createCategoryRequestDto.name(),
                createCategoryRequestDto.description());

        category.setId(1L);
        category.setName(createCategoryRequestDto.name());
        category.setDescription(createCategoryRequestDto.description());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        when(categoryMapper.toEntity(createCategoryRequestDto)).thenReturn(category);
        // When
        CategoryDto actualCategory = categoryService.updateById(createCategoryRequestDto, 1L);
        // Then
        assertThat(actualCategory).isEqualTo(categoryDto);
        EqualsBuilder.reflectionEquals(category, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("""
            Given a invalid category ID,
            When updateById is called,
            Then throw EntityNotFoundException should be thrown.
            """)
    public void updateCategoryById_WithInvalidCategoryId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidCategoryId = 999L;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.updateById(createCategoryRequestDto, invalidCategoryId));
        // Then
        String expected = "Can't find category by id=" + invalidCategoryId;
        assertEquals(expected, actual.getMessage());
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).save(any());
        verifyNoMoreInteractions(categoryRepository);
    }

    // DeleteById
    @Test
    @DisplayName("""
            Given valid category ID,
            When deleteById is called,
            Then the category should be deleted
            """)
    public void deleteCategoryById_WithValidBookId_ShouldDeleteCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        // When
        categoryService.deleteById(1L);
        // Then
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Given invalid category ID,
            When deleteById is called,
            Then throw EntityNotFoundException should be thrown
            """)
    public void deleteCategoryById_WithInvalidCategoryId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidCategoryId = 999L;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(invalidCategoryId));
        // Then
        String expected = "Can't find category by id=" + invalidCategoryId;
        assertEquals(expected, actual.getMessage());
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).deleteById(any());
        verifyNoMoreInteractions(categoryRepository);
    }
}
