package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ADVOCATE")
/**
 * Advocates may be linked as defence counsel or linked as
 */
public class Advocate extends Attendee {
    public Advocate() {
        super();
    }

    public Advocate(Builder builder) {
        super(builder);
        this.status = builder.status;
    }

    public String getStatus() {
        return status;
    }

    @Column(name = "status")
    private String status;

    public static class Builder extends Attendee.Builder{

        private String status;

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Advocate build() {
             return new Advocate(this);
         }

    }
}