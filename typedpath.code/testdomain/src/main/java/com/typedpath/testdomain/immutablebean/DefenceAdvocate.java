package com.typedpath.testdomain.immutablebean;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("DefenceAdvocate")
public class DefenceAdvocate extends Advocate {

    public DefenceAdvocate() {

    }
    public DefenceAdvocate(Builder builder) {
        super(builder);
    }

    @ManyToMany(mappedBy = "defenceAdvocates")
    private List<Defendant> defendants = new ArrayList<>();

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public static class Builder extends Advocate.Builder {
        @Override
        public DefenceAdvocate build() {
            return new DefenceAdvocate(this);
        }
    }
}