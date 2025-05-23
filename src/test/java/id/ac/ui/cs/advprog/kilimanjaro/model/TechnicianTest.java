package id.ac.ui.cs.advprog.kilimanjaro.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicianTest {

    @Test
    void testTechnicianBuilderCreatesTechnicianSuccessfully() {
        Technician technician = new Technician.TechnicianBuilder()
                .fullName("Tech John")
                .email("tech.john@example.com")
                .phoneNumber("0877123456")
                .password("techStrong")
                .address("Jl. Teknisi No. 10")
                .experience("5 years in electronics")
                .build();

        assertNotNull(technician.getId());
        assertEquals("Tech John", technician.getFullName());
        assertEquals("tech.john@example.com", technician.getEmail());
        assertEquals("0877123456", technician.getPhoneNumber());
        assertEquals("techStrong", technician.getPassword());
    }

    @Test
    void testTechnicianBuilderThrowsExceptionWhenAnyBaseFieldIsMissing() {
        assertThrows(IllegalArgumentException.class, () ->
                new Technician.TechnicianBuilder()
                        .email("tech.john@example.com")
                        .phoneNumber("0877123456")
                        .password("techStrong")
                        .address("Jl. Teknisi No. 10")
                        .experience("5 years in electronics")
                        .build()
        );
    }

    @Test
    void testTechnicianInitialJobStatsAreZero() {
        Technician technician = new Technician.TechnicianBuilder()
                .fullName("Fresh Tech")
                .email("fresh@example.com")
                .phoneNumber("0811111111")
                .password("password123")
                .address("Jl. Baru No. 1")
                .experience("Entry level")
                .build();

        assertEquals(0, technician.getTotalJobsDone());
        assertEquals(0L, technician.getTotalIncome());
    }

    @Test
    void testAddJobDoneAccumulatesCorrectly() {
        Technician technician = new Technician.TechnicianBuilder()
                .fullName("Experienced Tech")
                .email("exp@example.com")
                .phoneNumber("0822333444")
                .password("secure123")
                .address("Jl. Veteran No. 3")
                .experience("10 years in HVAC")
                .build();

        technician.addJobDone(3, 1500000L);
        technician.addJobDone(2, 800000L);

        assertEquals(5, technician.getTotalJobsDone());
        assertEquals(2300000L, technician.getTotalIncome());
    }

    @Test
    void testAddJobDoneThrowsExceptionForInvalidInput() {
        Technician technician = new Technician.TechnicianBuilder()
                .fullName("Careful Tech")
                .email("careful@example.com")
                .phoneNumber("0809090909")
                .password("secure")
                .address("Jl. Aman No. 5")
                .experience("3 years in automotive")
                .build();

        assertThrows(IllegalArgumentException.class, () -> technician.addJobDone(0, 100000));
        assertThrows(IllegalArgumentException.class, () -> technician.addJobDone(1, -50000));
    }

    @Test
    void testTechnicianBuilderAllowsCustomUUID() {
        UUID id = UUID.randomUUID();
        Technician technician = new Technician.TechnicianBuilder()
                .id(id)
                .fullName("Tech Custom ID")
                .email("custom@example.com")
                .phoneNumber("0811223344")
                .password("pass")
                .address("Jl. Custom No. 1")
                .experience("Custom experience")
                .build();

        assertEquals(id, technician.getId());
    }
}

