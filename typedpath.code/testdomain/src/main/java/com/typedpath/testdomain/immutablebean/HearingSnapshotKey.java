package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class HearingSnapshotKey implements Serializable {

    @Column(name ="id", nullable=false)
    private UUID id;

    @Column(name="hearing_id", nullable=false)
    private UUID hearingId;

    public HearingSnapshotKey() {

    }
    public HearingSnapshotKey(final UUID id, final UUID hearingId) {
        this.id=id;
        this.hearingId = hearingId;
    }
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HearingSnapshotKey that = (HearingSnapshotKey) o;

        if (!id.equals(that.id)) return false;
        return hearingId.equals(that.hearingId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + hearingId.hashCode();
        return result;
    }
}
