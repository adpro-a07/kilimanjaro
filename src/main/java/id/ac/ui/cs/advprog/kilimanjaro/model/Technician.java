package id.ac.ui.cs.advprog.kilimanjaro.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Technician extends BaseUser {
    private String address;
    private String experience;
    private int totalJobsDone = 0;
    private long totalIncome = 0;

    public void addJobDone(int count, long income) {
        if (count <= 0 || income < 0) {
            throw new IllegalArgumentException("Count must be positive and income must be non-negative");
        }
        this.totalJobsDone += count;
        this.totalIncome += income;
    }

    private Technician(Technician.TechnicianBuilder builder) {
        super(builder);
        this.address = builder.address;
        this.experience = builder.experience;
    }

    public static class TechnicianBuilder extends BaseUser.BaseUserBuilder<Technician.TechnicianBuilder> {
        private String address;
        private String experience;

        public Technician.TechnicianBuilder address(String address) {
            this.address = address;
            return this;
        }

        public Technician.TechnicianBuilder experience(String experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Technician build() {
            return new Technician(this);
        }

        @Override
        protected Technician.TechnicianBuilder self() {
            return this;
        }
    }
}
