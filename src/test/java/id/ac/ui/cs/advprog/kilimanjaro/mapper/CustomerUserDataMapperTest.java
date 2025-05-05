package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerUserDataMapperTest {

    @Test
    void testToUserData_shouldMapCorrectly() {
        Customer customer = mock(Customer.class);
        when(customer.getAddress()).thenReturn("Jl. Testing 123");

        UserIdentity fakeIdentity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(fakeIdentity);

            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer);

            assertNotNull(userData);
            assertEquals("customer@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.CUSTOMER, userData.getIdentity().getRole());
            assertEquals("Jl. Testing 123", userData.getProfile().getAddress());
        }
    }

    @Test
    void testToUserData_shouldThrowNullPointerExceptionIfAddressIsNull() {
        Customer customer = mock(Customer.class);
        // Simulate that getAddress() returns null.
        when(customer.getAddress()).thenReturn(null);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(identity);

            // Since getAddress() returns null, we expect a NullPointerException
            assertThrows(NullPointerException.class, () -> {
                new CustomerUserDataMapper().toUserData(customer);
            });
        }
    }

    @Test
    void testToUserData_shouldNotAllowNullAddress() {
        Customer customer = mock(Customer.class);
        // Simulate an empty address instead of null.
        when(customer.getAddress()).thenReturn(""); // Address is not null but empty.

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(identity);

            // Here, we are checking that the address is not null, even if it is empty.
            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer);

            // Address should not be null; however, it could be empty or handled by your code.
            assertNotNull(userData.getProfile().getAddress());
            assertTrue(userData.getProfile().getAddress().isEmpty()); // It can be empty but not null.
        }
    }


    @Test
    void testSupportsRole_shouldReturnCustomerRole() {
        CustomerUserDataMapper mapper = new CustomerUserDataMapper();
        assertEquals("CUSTOMER", mapper.supportsRole().name());
    }
}
