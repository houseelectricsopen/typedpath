package com.typedpath.testdomain.immutablebean;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ProsecutionAdvocate")
public class ProsecutionAdvocate extends Advocate {

}
