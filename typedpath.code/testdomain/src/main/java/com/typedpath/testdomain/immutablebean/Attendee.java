package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="a_attendee")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        discriminatorType = DiscriminatorType.STRING,
        name = "type"
)
public class Attendee {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable=false, updatable=false)
    private Ahearing hearing;

    @Column(name = "person_id")
    private java.util.UUID personId;

    @Column(name = "for_name")
    private String forName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "title")
    private String title;

    public Attendee() {

    }

    public Attendee(Builder builder) {
        this.id=builder.id;
        this.personId=builder.personId;
        this.forName=builder.forName;
        this.lastName=builder.lastName;
        this.title=builder.title;
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Ahearing getHearing() {
        return hearing;
    }

    public java.util.UUID getPersonId() {
        return personId;
    }

    public String getForName() {
        return forName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public static class Builder {

        protected    HearingSnapshotKey id;

        private    java.util.UUID personId;

        private    String forName;

        private    String lastName;

        private    String title;

        public Builder withId(HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withPersonId(final java.util.UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withForName(final String forName) {
            this.forName = forName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }
        public Attendee build() {
            return new Attendee(this);
        }
    }



}
