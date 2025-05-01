package id.ac.ui.cs.advprog.kilimanjaro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "id")
public class Admin extends BaseUser {
    protected Admin() {
        super(); // Required by JPA for reflection
    }

    private Admin(AdminBuilder builder) {
        super(builder);
    }

    public static class AdminBuilder extends BaseUser.BaseUserBuilder<Admin.AdminBuilder> {
        @Override
        public Admin build() {
            return new Admin(this);
        }

        @Override
        protected Admin.AdminBuilder self() {
            return this;
        }
    }
}
