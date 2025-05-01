package id.ac.ui.cs.advprog.kilimanjaro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Technician is a type of user that performs jobs and earns income.
 * It extends the BaseUser class with additional fields for address, experience, job tracking and income.
 */
@Entity
@Table(name = "technicians")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class Technician extends BaseUser {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String address;

    @Size(max = 500)
    @Column(length = 500)
    private String experience;

    @Column(nullable = false)
    private int totalJobsDone = 0;

    @Column(nullable = false)
    private long totalIncome = 0;

    /**
     * Increases the number of jobs done and total income earned.
     *
     * @param count  The number of jobs to add
     * @param income The income amount to add
     * @throws IllegalArgumentException if count is not positive or income is negative
     */
    public void addJobDone(int count, long income) {
        if (count <= 0 || income < 0) {
            throw new IllegalArgumentException("Count must be positive and income must be non-negative");
        }
        this.totalJobsDone += count;
        this.totalIncome += income;
    }

    // Required by JPA
    protected Technician() {
        super();
    }

    private Technician(TechnicianBuilder builder) {
        super(builder);
        this.address = builder.address;
        this.experience = builder.experience;
    }

    /**
     * Builder class for creating Technician instances.
     */
    public static class TechnicianBuilder extends BaseUserBuilder<TechnicianBuilder> {
        private String address;
        private String experience;

        public TechnicianBuilder address(String address) {
            this.address = address;
            return this;
        }

        public TechnicianBuilder experience(String experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Technician build() {
            if (address == null || address.isBlank()) {
                throw new IllegalArgumentException("Address must not be blank");
            }
            return new Technician(this);
        }

        @Override
        protected TechnicianBuilder self() {
            return this;
        }
    }
}
