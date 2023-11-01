package ivanov.springbootintro.mapper;

import ivanov.springbootintro.config.MapperConfig;
import ivanov.springbootintro.dto.BookDto;
import ivanov.springbootintro.dto.CreateBookRequestDto;
import ivanov.springbootintro.model.Book;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto requestDto);
}
