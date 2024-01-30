package ivanov.springbootintro.dto.category;

import jakarta.validation.constraints.NotEmpty;

public record CreateCategoryRequestDto(
        @NotEmpty String name,
        String description) {
}
