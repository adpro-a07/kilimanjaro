package id.ac.ui.cs.advprog.kilimanjaro.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testUserBuilderCreatesUserSuccessfully() {
        Customer customer = new Customer.UserBuilder()
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("08123456789")
                .password("securePassword")
                .address("Jl. Merdeka No. 1")
                .build();

        assertNotNull(customer.getId());
        assertEquals("John Doe", customer.getFullName());
        assertEquals("john@example.com", customer.getEmail());
        assertEquals("08123456789", customer.getPhoneNumber());
        assertEquals("securePassword", customer.getPassword());
        assertEquals("Jl. Merdeka No. 1", customer.getAddress());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenFullNameIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.UserBuilder()
                        .email("john@example.com")
                        .phoneNumber("08123456789")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenEmailIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.UserBuilder()
                        .fullName("John Doe")
                        .phoneNumber("08123456789")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenPhoneNumberIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.UserBuilder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenPasswordIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.UserBuilder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .phoneNumber("08123456789")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderCanSetCustomId() {
        UUID customId = UUID.randomUUID();
        Customer customer = new Customer.UserBuilder()
                .id(customId)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("08987654321")
                .password("anotherPassword")
                .address("Jl. Sudirman No. 2")
                .build();

        assertEquals(customId, customer.getId());
    }
}
