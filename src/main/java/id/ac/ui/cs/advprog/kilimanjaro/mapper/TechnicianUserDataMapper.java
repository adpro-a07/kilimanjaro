package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class TechnicianUserDataMapper extends AbstractUserDataMapper<Technician> implements UserDataMapper<Technician> {
    @Override
    public UserRole supportsRole() {
        return UserRole.TECHNICIAN;
    }

    protected UserProfile buildProfile(Technician user) {
        return UserProfile.newBuilder()
                .setAddress(user.getAddress())
                .setWorkExperience(user.getExperience())
                .setTotalJobsDone(user.getTotalJobsDone())
                .setTotalIncome(user.getTotalIncome())
                .build();
    }
}
