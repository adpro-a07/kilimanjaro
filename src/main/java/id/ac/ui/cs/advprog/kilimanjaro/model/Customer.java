package id.ac.ui.cs.advprog.kilimanjaro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class Customer extends BaseUser {
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String address;

    private Customer(UserBuilder builder) {
        super(builder);
        this.address = builder.address;
    }

    protected Customer() {
        super(); // Required by JPA
    }

    public static class UserBuilder extends BaseUserBuilder<UserBuilder> {
        private String address;

        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }

        @Override
        public Customer build() {
            return new Customer(this);
        }

        @Override
        protected UserBuilder self() {
            return this;
        }
    }
}
