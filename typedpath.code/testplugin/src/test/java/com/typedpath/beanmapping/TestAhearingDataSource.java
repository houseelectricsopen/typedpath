package com.typedpath.beanmapping;

import com.typedpath.testdomain.immutablebean.Address;
import com.typedpath.testdomain.immutablebean.Ahearing;
import com.typedpath.testdomain.immutablebean.DefenceAdvocate;
import com.typedpath.testdomain.immutablebean.Defendant;
import com.typedpath.testdomain.immutablebean.HearingSnapshotKey;
import com.typedpath.testdomain.immutablebean.Judge;
import com.typedpath.testdomain.immutablebean.Offence;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

public class TestAhearingDataSource {
    public static Ahearing sampleAhearing() {
        Judge.Builder judgeBuilder = (Judge.Builder) new Judge.Builder().withTitle("Justice").withForName("John")
                .withLastName("Smith");
        DefenceAdvocate.Builder da = (DefenceAdvocate.Builder) new DefenceAdvocate.Builder().withStatus("Barrister").withLastName("Save").withForName("Dave");
        DefenceAdvocate.Builder da2 = (DefenceAdvocate.Builder) new DefenceAdvocate.Builder().withStatus("Solicitor").withLastName("Save").withForName("Peter");
        UUID hearingId = UUID.randomUUID();

        Ahearing hearing = new Ahearing.Builder()
                .withId(hearingId)
                .withStartDateTime(LocalDateTime.now())
                .withRoomName("Court3")
                .withHearingType("TRIAL")
                .withCourtCentreName("liverpool")
                .withCourtCentreId(UUID.randomUUID())
                .withJudge(judgeBuilder).withDefendants(
                        Arrays.asList(
                                new Defendant.Builder()
                                        .withForeName("Thorgier")
                                        .withLastName("Thunder")
                                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearingId))
                                        .withAddress(
                                                new Address.Builder().withAddress2("Dark Town").withAddress1("Detonation Boulevard")
                                                        .build())
                                        .withOffences(Arrays.asList(
                                                new Offence.Builder().withCaseId(UUID.randomUUID())
                                                        .withWording("GBH")
                                                        .build()
                                        ))
                                        .build(),
                                new Defendant.Builder()
                                        .withForeName("Peter")
                                        .withLastName("Murphy")
                                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearingId))
                                        .withAddress(
                                                new Address.Builder().withAddress2("St Albans").withAddress1("Sesame Street")
                                                        .build()).withOffences(Arrays.asList(
                                        new Offence.Builder().withCaseId(UUID.randomUUID())
                                                .withWording("Murder")
                                                .build()
                                )).build(),
                                new Defendant.Builder()
                                        .withForeName("Andrew")
                                        .withLastName("Eldritch")
                                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearingId))
                                        .withAddress(
                                                new Address.Builder().withAddress2("Croydon").withAddress1("Iron Road")
                                                        .build())
                                        .withOffences(Arrays.asList(
                                                new Offence.Builder().withCaseId(UUID.randomUUID())
                                                        .withWording("Manslaughter")
                                                        .build()
                                        ))
                                        .build(),
                                new Defendant.Builder()
                                        .withForeName("Robert")
                                        .withLastName("Smith")
                                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearingId))
                                        .withAddress(
                                                new Address.Builder().withAddress2("Croydon").withAddress1("Bronze Avenue")
                                                        .build())
                                        .withOffences(Arrays.asList(
                                                new Offence.Builder().withCaseId(UUID.randomUUID())
                                                        .withWording("Public Indecency")
                                                        .build()
                                        ))
                                        .build()
                        )
                ).build();
        DefenceAdvocate daa1 = da.build();
        DefenceAdvocate daa2 = da2.build();
        hearing.getAttendees().add(daa1);
        hearing.getAttendees().add(daa2);
        hearing.getDefendants().get(0).getDefenceAdvocates().add(daa1);
        hearing.getDefendants().get(1).getDefenceAdvocates().add(daa2);
        hearing.getDefendants().get(2).getDefenceAdvocates().add(daa1);
        daa1.getDefendants().add(hearing.getDefendants().get(0));
        daa1.getDefendants().add(hearing.getDefendants().get(1));
        daa2.getDefendants().add(hearing.getDefendants().get(2));

        return hearing;
    }

}
