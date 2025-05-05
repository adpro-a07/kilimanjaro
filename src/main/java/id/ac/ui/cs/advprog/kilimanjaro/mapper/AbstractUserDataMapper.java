package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;

public abstract class AbstractUserDataMapper<T extends BaseUser> implements UserDataMapper<T> {

    @Override
    public UserData toUserData(T user) {
        UserIdentity userIdentity = UserMapperUtil.extractUserIdentity(user);
        UserProfile userProfile = buildProfile(user);

        return UserData.newBuilder()
                .setIdentity(userIdentity)
                .setProfile(userProfile)
                .build();
    }

    @Override
    public abstract UserRole supportsRole();

    protected abstract UserProfile buildProfile(T user);
}
