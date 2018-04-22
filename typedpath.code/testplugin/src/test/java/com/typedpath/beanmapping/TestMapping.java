package com.typedpath.beanmapping;

import com.typedpath.test.genfromjson.sendingsheetcompleted.Address;
import com.typedpath.test.genfromjson.sendingsheetcompleted.Defendants;
import com.typedpath.test.genfromjson.sendingsheetcompleted.Hearing;
import com.typedpath.test.genfromjson.sendingsheetcompleted.SendingSheetCompleted;
import com.typedpath.testdomain.immutablebean.Ahearing;
import com.typedpath.testdomain.immutablebean.AhearingPath;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.typedpath.beanmapping.TypedPath.into;
import static com.typedpath.beanmapping.TypedPath.link;

public class TestMapping {

    @Test
    public void testInto() {
        SendingSheetCompleted.SendingSheetCompletedPath sourcePath = SendingSheetCompleted.root();
        AhearingPath<Ahearing> destinationPath = AhearingPath.root();
        link(sourcePath, destinationPath,
                (sh, h2) -> {
                    link(sh.hearing().defendants().all(), h2.defendants().all(),
                            (d1, d2) -> {
                                into(d1.firstName(), d2.foreName());
                                into(d1.lastName(), d2.lastName());
                            },
                            (d1, d2) -> {
                                link(d1.address(), d2.address(),
                                        (a1, a2) -> {
                                            into(a1.address1(), a2.address1());
                                            into(a1.address2(), a2.address2());
                                            into(a1.address3(), a2.address3());
                                            into(a1.address4(), a2.address4());
                                        });
                            }
                    );
                }
        );

        SendingSheetCompleted sendingSheetCompleted = sampleSendingSheetCompleted();

        // assumption of output type here !
        // supplying the  destination path although it doesnt affect the result would facilitate
        // typed safety and a check on the forst linked path
        Ahearing transformed = (new Mapper()).mapSingle(sourcePath, destinationPath, sendingSheetCompleted);
        Assert.assertEquals(transformed.getDefendants().size(), sendingSheetCompleted.getHearing().getDefendants().size());
        Assert.assertEquals(transformed.getDefendants().get(0).getForeName(), sendingSheetCompleted.getHearing().getDefendants().get(0).getFirstName());
        Assert.assertEquals(transformed.getDefendants().get(0).getLastName(), sendingSheetCompleted.getHearing().getDefendants().get(0).getLastName());
        Assert.assertEquals(transformed.getDefendants().get(0).getAddress().getAddress1(),
                sendingSheetCompleted.getHearing().getDefendants().get(0).getAddress().getAddress1());

    }

    private SendingSheetCompleted sampleSendingSheetCompleted() {
        return (new SendingSheetCompleted.Builder())
                .withHearing(new Hearing.Builder()
                        .withDefendants(
                                Arrays.asList(
                                        new Defendants.Builder()
                                                .withFirstName("David")
                                                .withLastName("Bowie")
                                                .withAddress(
                                                        new Address.Builder()
                                                                .withAddress1("123 Sesame Street")
                                                                .withAddress2("Orvillington")
                                                                .withPostcode("AL1 1AL")
                                                                .build()
                                                )
                                                .build()
                                )
                        )
                        .build())
                .build();
    }


}
