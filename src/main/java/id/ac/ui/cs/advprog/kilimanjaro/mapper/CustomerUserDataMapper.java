package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class CustomerUserDataMapper extends AbstractUserDataMapper<Customer> implements UserDataMapper<Customer> {
    @Override
    public UserRole supportsRole() {
        return UserRole.CUSTOMER;
    }

    protected UserProfile buildProfile(Customer user) {
        return UserProfile.newBuilder()
                .setAddress(user.getAddress())
                .build();
    }
}
