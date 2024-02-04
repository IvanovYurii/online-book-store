package ivanov.springbootintro.dto.user;

import ivanov.springbootintro.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@Accessors(chain = true)
@FieldMatch.List({
        @FieldMatch(first = "password", second = "repeatPassword",
                message = "Password and repeat password must match")
})
public class UserRegistrationRequestDto {
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Length(min = 8, max = 20)
    private String password;
    @Length(min = 8, max = 20)
    @NotEmpty
    private String repeatPassword;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String shippingAddress;
}
