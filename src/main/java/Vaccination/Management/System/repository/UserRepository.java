package Vaccination.Management.System.repository;

import Vaccination.Management.System.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}
