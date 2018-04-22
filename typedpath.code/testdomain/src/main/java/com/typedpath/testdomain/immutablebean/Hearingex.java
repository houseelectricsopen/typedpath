package com.typedpath.testdomain.immutablebean;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hearingex")
public class Hearingex {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing",orphanRemoval = true)
    private List<Defendant> defendants=new ArrayList<>();

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearing_type")
    private String hearingType;

    @Column(name="start_date_time")
    private LocalDateTime startDateTime;

    @Column(name="court_centre_id")
    private UUID courtCentreId;

    @Column(name="court_centre_name")
    private String courtCentreName;

    @Column(name="room_id")
    private UUID roomId;

    @Column(name="room_name")
    private String roomName;

//    @OneToMany
//    @JoinColumn(name = "id", insertable=false, updatable=false, referencedColumnName = "hearing_id")

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing",orphanRemoval = true)
    private List<Attendee> attendees;

    public Hearingex(Builder builder) {
        this.id=id = builder.id;
        this.hearingType = builder.hearingType;
        this.startDateTime = builder.startDateTime;
        this.courtCentreId = builder.courtCentreId;
        this.courtCentreName = builder.courtCentreName;
        this.roomId = builder.roomId;
        this.roomName = builder.roomName;
        this.setDefendants(builder.defendants);
        //TODO do something with judge
    }

    public void setDefendants(List<Defendant> defendants) {
        if (defendants != null) {
            this.defendants = new ArrayList<>(defendants);
           // this.defendants.forEach(defendant -> defendant.setHearing(this));
        }
        else {
            this.defendants = new ArrayList<>();
        }
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public void setAttendees(List<Attendee> attendees) {
        if (attendees != null) {
            this.attendees = new ArrayList<>(attendees);
        }
        else {
            this.attendees = new ArrayList<>();
        }
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public UUID getId() {
        return id;
    }

    public String getHearingType() {
        return hearingType;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public static class Builder {
        private UUID id;
        private List<Defendant> defendants=new ArrayList<>();
        private String hearingType;
        private LocalDateTime startDateTime;
        private UUID courtCentreId;
        private String courtCentreName;
        private UUID roomId;
        private String roomName;
        private Judge.Builder judgeBuilder;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withDefendants(List<Defendant> defendants) {
            this.defendants = defendants;
            return this;
        }

        public Builder withJudge(Judge.Builder builder) {
            this.judgeBuilder = builder;
            return this;
        }


        public Builder withHearingType(String hearingType) {
            this.hearingType = hearingType;
            return this;
        }

        public Builder withStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }
        public Builder withCourtCentreId(UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtCentreName(String courtCentreName) {
            this.courtCentreName = courtCentreName;
            return this;
        }

        public Builder withRoomId(UUID roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder withRoomName(String roomName) {
            this.roomName = roomName;
            return this;
        }

        public Hearingex build() {
            return new Hearingex(this);
        }

    }

}
