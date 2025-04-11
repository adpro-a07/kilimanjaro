package id.ac.ui.cs.advprog.kilimanjaro.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends BaseUser {
    private String address;

    private User(UserBuilder builder) {
        super(builder);
        this.address = builder.address;
    }

    public static class UserBuilder extends BaseUserBuilder<UserBuilder> {
        private String address;

        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }

        @Override
        public User build() {
            return new User(this);
        }

        @Override
        protected UserBuilder self() {
            return this;
        }
    }
}
