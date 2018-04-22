package com.typedpath.testdomain.immutablebean;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO action this:
 * Tim Cooke [12:25 PM]
 I think we need to add this to defendant - "defenceSolicitorFirm": "xyz solicitor",
 rather it is required for resulting and therefore should be part of the shared-results event
 */
@Entity
@Table(name = "a_defendant")
public class Defendant {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable=false, updatable=false)
    private Ahearing hearing;

    @ManyToMany
    @JoinTable(name = "a_attendee_defendant",
         joinColumns =
                 {@JoinColumn(name = "defendant_id", referencedColumnName = "id"),
                 @JoinColumn(name = "hearing_id", referencedColumnName = "hearing_id")},
            inverseJoinColumns =  {
                    @JoinColumn(name = "attendee_id", referencedColumnName = "id"),
                    @JoinColumn(name = "attendee_hearing_id", referencedColumnName = "hearing_id")}
    )
    private  List<DefenceAdvocate> defenceAdvocates = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "defendant",orphanRemoval = true)
    private List<Offence> offences=new ArrayList<>();

    @Column(name = "person_id")
    private java.util.UUID personId;

    @Column(name = "fore_name")
    private String foreName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "gender")
    private String gender;

    @Column(name = "work_telephone")
    private String workTelephone;

    @Column(name = "home_telephone")
    private String homeTelephone;

    @Column(name = "mobile_telephone")
    private String mobileTelephone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "bail_status")
    private String bailStatus;

    @Column(name = "custody_time_limit_date")
    private java.time.LocalDate custodyTimeLimitDate;

    @Column(name = "defence_solicitor_firm")
    private String defenceSolicitorFirm;

    @Column(name = "interpreter_name")
    private String interpreterName;

    @Column(name = "interpreter_language")
    private String interpreterLanguage;

    @Embedded
    private Address address;


    public Defendant() {

    }

    public Defendant(Builder builder) {
        this.id = builder.id;
        this.hearing = builder.hearing;
        this.personId = builder.personId;
        this.foreName = builder.foreName;
        this.lastName = builder.lastName;
        this.dateOfBirth = builder.dateOfBirth;
        this.nationality = builder.nationality;
        this.gender = builder.gender;
        this.address = builder.address;
        this.workTelephone = builder.workTelephone;
        this.homeTelephone = builder.homeTelephone;
        this.mobileTelephone = builder.mobileTelephone;
        this.fax = builder.fax;
        this.bailStatus = builder.bailStatus;
        this.custodyTimeLimitDate = builder.custodyTimeLimitDate;
        this.defenceSolicitorFirm = builder.defenceSolicitorFirm;
        this.interpreterName = builder.interpreterName;
        this.interpreterLanguage = builder.interpreterLanguage;
        this.offences = builder.offences;
        this.defenceAdvocates = builder.defenceAdvocates;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public void setOffences(List<Offence> offences) {
        this.offences=offences;
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

    public String getForeName() {
        return foreName;
    }

    public String getLastName() {
        return lastName;
    }

    public java.time.LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public String getGender() {
        return gender;
    }

    public Address getAddress() {
        return address;
    }

    public String getWorkTelephone() {
        return workTelephone;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public String getMobileTelephone() {
        return mobileTelephone;
    }

    public String getFax() {
        return fax;
    }

    public String getBailStatus() {
        return bailStatus;
    }

    public java.time.LocalDate getCustodyTimeLimitDate() {
        return custodyTimeLimitDate;
    }

    public String getDefenceSolicitorFirm() {
        return defenceSolicitorFirm;
    }

    public String getInterpreterLanguage() {
        return interpreterLanguage;
    }

    /**
     * there appears to be a bug in jpa preventing a column in the join table being used in both sides og the realtionship
     * hence the redundant column attendee_hearing_id which holds the same value as hearing_id
     */
    public List<DefenceAdvocate> getDefenceAdvocates() {
        return defenceAdvocates;
    }

    public String getInterpreterName() {
        return interpreterName;
    }

    public void setInterpreterName(String interpreterName) {
        this.interpreterName = interpreterName;
    }

    public static class Builder {

        private HearingSnapshotKey id;

        private Ahearing hearing;

        private java.util.UUID personId;

        private String foreName;

        private String lastName;

        private java.time.LocalDate dateOfBirth;

        private String nationality;

        private String gender;

        private Address address;

        private String workTelephone;

        private String homeTelephone;

        private String mobileTelephone;

        private String fax;

        private String bailStatus;

        private java.time.LocalDate custodyTimeLimitDate;

        private String defenceSolicitorFirm;

        private String interpreterName;

        private String interpreterLanguage;

        private List<Offence> offences;

        private List<DefenceAdvocate> defenceAdvocates=new ArrayList<>();

        public Builder withId(final HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withDefenceAdvocates(final List<DefenceAdvocate> defenceAdvocates) {
            this.defenceAdvocates = defenceAdvocates;
            return this;
        }

        public Builder withHearing(final Ahearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withPersonId(final java.util.UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withForeName(final String foreName) {
            this.foreName = foreName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withDateOfBirth(final java.time.LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withNationality(final String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder withGender(final String gender) {
            this.gender = gender;
            return this;
        }

        public Builder withAddress(final Address address) {
            this.address = address;
            return this;
        }

        public Builder withWorkTelephone(final String workTelephone) {
            this.workTelephone = workTelephone;
            return this;
        }

        public Builder withHomeTelephone(final String homeTelephone) {
            this.homeTelephone = homeTelephone;
            return this;
        }

        public Builder withMobileTelephone(final String mobileTelephone) {
            this.mobileTelephone = mobileTelephone;
            return this;
        }

        public Builder withFax(final String fax) {
            this.fax = fax;
            return this;
        }

        public Builder withBailStatus(final String bailStatus) {
            this.bailStatus = bailStatus;
            return this;
        }

        public Builder withCustodyTimeLimitDate(final java.time.LocalDate custodyTimeLimitDate) {
            this.custodyTimeLimitDate = custodyTimeLimitDate;
            return this;
        }

        public Builder withDefenceSolicitorFirm(final String defenceSolicitorFirm) {
            this.defenceSolicitorFirm = defenceSolicitorFirm;
            return this;
        }

        public Builder withInterpreterName(final String interpreterName) {
            this.interpreterName = interpreterName;
            return this;
        }

        public Builder withInterpreterLanguage(final String interpreterLanguage) {
            this.interpreterLanguage = interpreterLanguage;
            return this;
        }

        public Builder withOffences(final List<Offence> offences) {
            this.offences = offences;
            return this;
        }

        public Defendant build() {
            return new Defendant(this);
        }
    }


}
