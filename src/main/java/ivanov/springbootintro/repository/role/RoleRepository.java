package ivanov.springbootintro.repository.role;

import ivanov.springbootintro.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoleRepository extends JpaRepository<Role, Long>,
        JpaSpecificationExecutor<Role> {
    Optional<Role> getByName(Role.RoleName name);
}
