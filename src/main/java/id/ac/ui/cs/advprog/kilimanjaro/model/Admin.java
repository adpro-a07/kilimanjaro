package id.ac.ui.cs.advprog.kilimanjaro.model;

public class Admin extends BaseUser {
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
