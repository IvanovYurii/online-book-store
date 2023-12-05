package ivanov.springbootintro.mapper;

import ivanov.springbootintro.dto.order.OrderDto;
import ivanov.springbootintro.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    OrderDto orderToOrderDto(Order order);
}
