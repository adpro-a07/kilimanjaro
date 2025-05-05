package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TechnicianUserDataMapperTest {

    @Test
    void testToUserData_shouldMapCorrectly() {
        Technician technician = mock(Technician.class);
        when(technician.getAddress()).thenReturn("Workshop Lane");
        when(technician.getExperience()).thenReturn("5 years");
        when(technician.getTotalJobsDone()).thenReturn(42);
        when(technician.getTotalIncome()).thenReturn(1250000L);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("tech@example.com")
                .setRole(UserRole.TECHNICIAN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(technician))
                    .thenReturn(identity);

            TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
            UserData userData = mapper.toUserData(technician);

            assertNotNull(userData);
            assertEquals("tech@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.TECHNICIAN, userData.getIdentity().getRole());
            assertEquals("Workshop Lane", userData.getProfile().getAddress());
            assertEquals("5 years", userData.getProfile().getWorkExperience());
            assertEquals(42, userData.getProfile().getTotalJobsDone());
            assertEquals(1250000L, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testSupportsRole_shouldReturnTechnicianRole() {
        TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
        assertEquals("TECHNICIAN", mapper.supportsRole().name());
    }
}
