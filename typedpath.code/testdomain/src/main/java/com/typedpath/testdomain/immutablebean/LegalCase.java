package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "a_case")
public class LegalCase {

    @Id
    @Column(name = "id", nullable = false)
    private java.util.UUID id;

    @Column(name = "caseurn")
    private String caseurn;

    public LegalCase(Builder builder) {
        this.id = builder.id;
        this.caseurn = builder.caseurn;
    }

    public LegalCase() {

    }

    public java.util.UUID getId() {
        return id;
    }

    public String getCaseurn() {
        return caseurn;
    }

    public static class Builder {

        private java.util.UUID id;

        private String caseurn;

        public Builder withId(final java.util.UUID id) {
            this.id = id;
            return this;
        }

        public Builder withCaseurn(final String caseurn) {
            this.caseurn = caseurn;
            return this;
        }

        public LegalCase build() {
            return new LegalCase(this);
        }


    }
}
