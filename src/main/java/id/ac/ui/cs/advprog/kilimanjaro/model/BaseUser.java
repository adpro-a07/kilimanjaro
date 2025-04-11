package id.ac.ui.cs.advprog.kilimanjaro.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * BaseUser is an abstract class that represents a user in the system.
 * It contains common attributes and methods for all user types.
 */
@Getter
@Setter
public abstract class BaseUser {
    protected final UUID id;
    protected String fullName;
    protected String email;
    protected String phoneNumber;
    protected String password;

    protected BaseUser(BaseUserBuilder<?> builder) {
        if (builder.id == null || builder.fullName == null || builder.email == null
                || builder.phoneNumber == null || builder.password == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }
        this.id = builder.id;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.password = builder.password;
    }

    /**
     * Abstract Builder pattern implementation for BaseUser.
     * @param <T> The type of builder (for subclass builders)
     */
    public static abstract class BaseUserBuilder<T extends BaseUserBuilder<T>> {
        private UUID id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String password;

        public BaseUserBuilder() {
            this.id = UUID.randomUUID(); // Generate a UUID by default
        }

        public T id(UUID id) {
            this.id = id;
            return self();
        }

        public T fullName(String fullName) {
            this.fullName = fullName;
            return self();
        }

        public T email(String email) {
            this.email = email;
            return self();
        }

        public T phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return self();
        }

        public T password(String password) {
            this.password = password;
            return self();
        }

        protected abstract T self();

        public abstract BaseUser build();
    }
}