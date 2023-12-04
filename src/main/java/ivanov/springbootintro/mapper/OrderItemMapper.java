package ivanov.springbootintro.mapper;

import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "book.id", target = "bookId")
    OrderItemDto orderItemToDto(OrderItem orderItem);
}

