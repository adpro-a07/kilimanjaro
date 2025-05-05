package id.ac.ui.cs.advprog.kilimanjaro.repository;

import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByEmail(@NotBlank @Email @Size(max = 100) String email);

    Optional<BaseUser> findById(@NotBlank @Size(max = 100) UUID id);

    Boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);

    Boolean existsById(@NotBlank @Size(max = 100) UUID id);
}
