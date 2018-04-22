package com.typedpath.testdomain.immutablebean;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Judge")
/**
 * WARNING judge is not an Attendee in a business sense
 */
public class Judge extends Attendee {
    public Judge() {
        super();
    }

    @Column(name="title")
    private String title;

    public String getTitle() {
        return title;
    }

    public Judge(Builder builder)
    {
        super(builder);
        this.title = builder.title;
    }

    public static class Builder extends Attendee.Builder {
        private String title;

        public Builder withTitle(String title) {
            this.title=title;
            return this;
        }
         public Judge build() {
             return new Judge(this);
         }

    }
}