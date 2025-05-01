package id.ac.ui.cs.advprog.kilimanjaro.repository;

import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByFullName(@NotBlank @Size(max = 100) String fullName);

    Optional<BaseUser> findByEmail(@NotBlank @Email @Size(max = 100) String email);

    Boolean existsByFullName(@NotBlank @Size(max = 100) String fullName);

    Boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);
}
