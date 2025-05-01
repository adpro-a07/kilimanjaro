package id.ac.ui.cs.advprog.kilimanjaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
public class Customer extends BaseUser {
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String address;

    private Customer(CustomerBuilder builder) {
        super(builder);
        this.address = builder.address;
    }

    protected Customer() {
        super(); // Required by JPA
    }

    public static class CustomerBuilder extends BaseUserBuilder<CustomerBuilder> {
        private String address;

        public CustomerBuilder address(String address) {
            this.address = address;
            return this;
        }

        @Override
        public Customer build() {
            return new Customer(this);
        }

        @Override
        protected CustomerBuilder self() {
            return this;
        }
    }
}
