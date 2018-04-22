package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;


@Entity
@Table(name = "a_offence")
public class Offence {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne
    @JoinColumns( {
            @JoinColumn(name = "defendant_id", insertable=false, updatable=false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable=false, updatable=false, referencedColumnName = "hearing_id")})
    private Defendant defendant;

    @Column(name = "code")
    private String code;

    @Column(name = "count")
    private Integer count;

    @Column(name = "wording")
    private String wording;

    @Column(name = "start_date")
    private java.time.LocalDateTime startDate;

    @Column(name = "end_date")
    private java.time.LocalDateTime endDate;

    @Column(name = "conviction_date")
    private java.time.LocalDateTime convictionDate;

    @Column(name = "verdict_id")
    private UUID verdictId;

    @Column(name = "verdict_code")
    private String verdictCode;

    @Column(name = "verdict_category")
    private String verdictCategory;

    @Column(name = "verdict_description")
    private String verdictDescription;

    @Column(name = "verdict_date")
    private java.time.LocalDateTime verdictDate;

    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "defendant_id")
    private UUID defendantId;

    public Offence() {

    }

    public Offence(Builder builder) {
        this.id = builder.id;
        this.defendant = builder.defendant;
        if (defendant!=null) {
            this.defendantId = builder.defendant.getId().getId();
        }
        this.code = builder.code;
        this.count = builder.count;
        this.wording = builder.wording;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.convictionDate = builder.convictionDate;
        this.verdictId = builder.verdictId;
        this.verdictCode = builder.verdictCode;
        this.verdictCategory = builder.verdictCategory;
        this.verdictDescription = builder.verdictDescription;
        this.verdictDate = builder.verdictDate;
        this.caseId = builder.caseId;
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public String getCode() {
        return code;
    }

    public Integer getCount() {
        return count;
    }

    public String getWording() {
        return wording;
    }

    public java.time.LocalDateTime getStartDate() {
        return startDate;
    }

    public java.time.LocalDateTime getEndDate() {
        return endDate;
    }

    public java.time.LocalDateTime getConvictionDate() {
        return convictionDate;
    }

    public UUID getVerdictId() {
        return verdictId;
    }

    public String getVerdictCode() {
        return verdictCode;
    }

    public String getVerdictCategory() {
        return verdictCategory;
    }

    public String getVerdictDescription() {
        return verdictDescription;
    }

    public java.time.LocalDateTime getVerdictDate() {
        return verdictDate;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public static class Builder {

        private HearingSnapshotKey id;

        private UUID caseId;

        private String code;

        private Integer count;

        private String wording;

        private java.time.LocalDateTime startDate;

        private java.time.LocalDateTime endDate;

        private java.time.LocalDateTime convictionDate;

        private UUID verdictId;

        private String verdictCode;

        private String verdictCategory;

        private String verdictDescription;

        private java.time.LocalDateTime verdictDate;

        private Defendant defendant;

        public Builder withId(final HearingSnapshotKey id) {
            this.id = id;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withDefendant(final Defendant defendant) {
            this.defendant = defendant;
            return this;
        }

        public Builder withCode(final String code) {
            this.code = code;
            return this;
        }

        public Builder withCount(final Integer count) {
            this.count = count;
            return this;
        }

        public Builder withWording(final String wording) {
            this.wording = wording;
            return this;
        }

        public Builder withStartDate(final java.time.LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(final java.time.LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withConvictionDate(final java.time.LocalDateTime convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withVerdictId(final UUID verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public Builder withVerdictCode(final String verdictCode) {
            this.verdictCode = verdictCode;
            return this;
        }

        public Builder withVerdictCategory(final String verdictCategory) {
            this.verdictCategory = verdictCategory;
            return this;
        }

        public Builder withVerdictDescription(final String verdictDescription) {
            this.verdictDescription = verdictDescription;
            return this;
        }

        public Builder withVerdictDate(final java.time.LocalDateTime verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }

        public Offence build() {
            return new Offence(this);
        }
    }


}
