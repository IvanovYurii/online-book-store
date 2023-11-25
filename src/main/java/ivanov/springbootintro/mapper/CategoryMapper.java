package ivanov.springbootintro.mapper;

import ivanov.springbootintro.config.MapperConfig;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.model.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    Category toEntity(CreateCategoryRequestDto requestDto);
}
