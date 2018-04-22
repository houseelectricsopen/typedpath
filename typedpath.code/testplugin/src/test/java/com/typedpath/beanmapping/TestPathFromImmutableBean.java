package com.typedpath.beanmapping;

import com.typedpath.testdomain.immutablebean.AdvocatePath;
import com.typedpath.testdomain.immutablebean.Ahearing;
import com.typedpath.testdomain.immutablebean.AhearingPath;
import com.typedpath.testdomain.immutablebean.Attendee;
import com.typedpath.testdomain.immutablebean.DefenceAdvocate;
import com.typedpath.testdomain.immutablebean.DefenceAdvocatePath;
import com.typedpath.testdomain.immutablebean.Defendant;
import com.typedpath.testdomain.immutablebean.Judge;
import com.typedpath.testdomain.immutablebean.JudgePath;
import com.typedpath.testdomain.immutablebean.Offence;
import com.typedpath.testdomain.immutablebean.ProsecutionAdvocate;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.typedpath.beanmapping.TestUtil.checkPath;
import static com.typedpath.beanmapping.TestUtil.is;

public class TestPathFromImmutableBean {
    @Test
    public void demo() {
        Ahearing hearing = TestAhearingDataSource.sampleAhearing();
        TypedPath<Ahearing, LocalDateTime> dateTimePath = AhearingPath.root().startDateTime();
        System.out.println(dateTimePath.get(hearing, null));
        TypedPath<Ahearing, String> timePath = AhearingPath.root().startDateTime().map("startTime", ldt->ldt.format(DateTimeFormatter.ofPattern("HH:mm")), String.class);
        System.out.println(timePath.get(hearing, null));
        TypedPath<Ahearing, String> defendantLastName = AhearingPath.root().defendants().all().lastName();
        List<String> names = defendantLastName.getNodesAssumeSinglePath(hearing);
        System.out.println(names);


        AhearingPath selectPath = AhearingPath.root().select(
                h->h.defendants().alias("baddies").all().select(
                        d->d.lastName(),
                        d->d.foreName(),
                        d->d.address().skip().select(
                                a->a.address1(),
                                a->a.address2()
                        )
                )
        );
        System.out.println(JsonReader.selectAsJson(hearing, selectPath));

        /*Function<Defendant, String> defendantLocalNess = d->d.getAddress()!=null&&"Croydon".equals(d.getAddress().getAddress2())?"Local":"NonLocal";
        TypedPath<Ahearing, String> groupKeyPath =
                AhearingPath.root().defendants().all().map("isLocal", defendantLocalNess, String.class);
        //System.out.println(JsonReader.selectAsJson(hearing, groupKeyPath));
        System.out.println(groupKeyPath.getNodesAssumeSinglePath(hearing));
        Function<String, TypedPath> groupValuePathFunction = (isLocal) -> AhearingPath.root().select(
                ah->ah.defendants().filter(d->isLocal.equals(defendantLocalNess.apply(d))).select(
                        dp->dp.foreName(),
                        dp->dp.lastName(),
                        dp->dp.address().skip().address1(),
                        dp->dp.address().skip().address2()
                )
        );

        selectPath = AhearingPath.root();
        selectPath.insertGroupBy("byLocal", groupKeyPath, groupValuePathFunction);

        System.out.println(JsonReader.selectAsJson(hearing, selectPath));
*/

    }

    @Test
    public void testGetHearingV2PathToJson() {
        TypedPath path;
        Ahearing hearingEntity = TestAhearingDataSource.sampleAhearing();
        List<UUID> caseIds = new ArrayList<>();
        hearingEntity.getDefendants().forEach(d -> d.getOffences().forEach(o -> caseIds.add(o.getCaseId())));
        System.out.println("caseIds:" + caseIds);

        //create a filtered path for each case
        AhearingPath<Ahearing>[] caseFilterPaths = new AhearingPath[caseIds.size()];
        for (int done = 0; done < caseIds.size(); done++) {
            final UUID caseId = caseIds.get(done);
            Predicate<Offence> offenceFilter = (o) -> caseId.equals(o.getCaseId());
            Predicate<Defendant> defendantFilter = d -> d.getOffences().stream().anyMatch(o -> offenceFilter.test(o));

            AhearingPath<Ahearing> caseSelection = AhearingPath.root().select(
                    hss -> hss.selectConstant("caseId", caseId, UUID.class),
                    hss -> hss.defendants().filter(defendantFilter).select(
                            d -> d.foreName(),
                            d -> d.lastName(),
                            d -> d.id().skip().id().as("defendantId"),
                            d -> d.offences().filter(offenceFilter).select(
                                    o -> o.caseId(),
                                    o -> o.wording()
                            ))
            );
            caseFilterPaths[done] = caseSelection;
        }

        Predicate<Attendee> isDefence = a -> a instanceof DefenceAdvocate;
        Predicate<Attendee> isProsecution = a -> a instanceof ProsecutionAdvocate;
        Predicate<Attendee> isJudge = a -> a instanceof Judge;

        //create a path for each attendee type
        AhearingPath attendeePaths[] = {
                AhearingPath.root().select(h -> h.attendees().alias("prosecutionCouncels").filter(isProsecution).select(
                        a -> a.id().skip().id().as("attendeeId"),
                        a -> a.forName().as("firstName"),
                        a -> a.lastName().as("lastName")
                )),
                AhearingPath.root().select(h -> h.attendees().alias("defenceCounsels").filter(isDefence).select(
                        a -> a.id().skip().id().as("attendeeId"),
                        a -> a.forName().as("firstName"),
                        a -> a.lastName().as("lastName")
                )
                        .selectCast(AdvocatePath.root(), adv -> adv.status())
                        .selectCast(DefenceAdvocatePath.root(), ap -> ap.defendants().alias("defendantIds").all().skip().id().skip().id()))
        };

        path = AhearingPath.root().select(
                h -> h.id().as("hearingId"),
                h -> h.startDateTime().map("startDate", (s) -> s.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), String.class),
                h -> h.startDateTime().map("startTime", (s) -> s.format(DateTimeFormatter.ofPattern("HH:mm")), String.class),
                h -> h.roomName(),
                h -> h.hearingType(),
                h -> h.courtCentreName().as("courtCentreName"),
                h -> h.courtCentreId(),
                h -> h.attendees().alias("judge").filter(isJudge).select(
                        a -> a.forName(),
                        a -> a.lastName()
                ).selectCast(JudgePath.root(), j -> j.title())
        )
                .insertUnion("attendees", attendeePaths)
                .insertUnion("cases", caseFilterPaths);

        String strJson = JsonReader.selectAsJson(hearingEntity, path);
        System.out.println(strJson);
        //System.out.println(path);
    }

    @Test
    public void testGroupBy() {

        //TODO eliminate need for all these skips
        TypedPath<Ahearing, UUID> keyPath = AhearingPath.root().skip().defendants().skip().all().skip().offences().skip().all().skip().caseId();

        Function<UUID, TypedPath<Ahearing, ?>> valuePathFunction = (caseId) ->
        {
            Predicate<Offence> offenceFilter = (o) -> caseId.equals(o.getCaseId());
            Predicate<Defendant> defendantFilter = d -> d.getOffences().stream().anyMatch(o -> offenceFilter.test(o));

            return AhearingPath.root().skip().select(
                    hss -> hss.defendants().filter(defendantFilter).select(
                            d -> d.foreName(),
                            d -> d.lastName(),
                            d -> d.id().skip().id().as("defendantId"),
                            d -> d.offences().filter(offenceFilter).select(
                                    o -> o.caseId(),
                                    o -> o.wording()
                            ))
            );
        };

        AhearingPath path = AhearingPath.root().insertGroupBy("cases", keyPath, valuePathFunction);

        Ahearing hearingEntity = TestAhearingDataSource.sampleAhearing();
        String strJson = JsonReader.selectAsJson(hearingEntity, path);
        System.out.println(strJson);

        //test out the key path
        /*List<UUID> lCaseIds = new ArrayList<>();
        keyPath.getNodesAssumeSinglePath(hearingEntity, lCaseIds);
        Set<String> caseIds = new HashSet(lCaseIds);
        System.out.println("groupKeys:" + caseIds);*/

    }

    @Test
    public void multilevelAssertion() {
        AhearingPath path = AhearingPath.root().select(
                h->h.defendants()
                .get(0).select(
                d->d.foreName().check(is("David")),
                d->d.lastName().check(is("Bowie")),
                d->d.address().address2().check(is("Croydon"))
                        ),
                h->h.defendants().get(20).select(
                        d->d.lastName().check(is("Mr Tickle"))
                )
        );
        Ahearing hearing = TestAhearingDataSource.sampleAhearing();
        List<TestUtil.CheckError> errors = checkPath(hearing, path);
        errors.forEach(error->System.out.println(error));
        Assert.assertTrue(errors.size()==4);
        Assert.assertTrue(errors.get(0).getPathString().startsWith("/defendants/0/foreName"));
        Assert.assertTrue(errors.get(1).getPathString().startsWith("/defendants/0/lastName"));
        Assert.assertTrue(errors.get(2).getPathString().startsWith("/defendants/0/address/address2"));
        Assert.assertTrue(errors.get(3).getPathString().startsWith("/defendants/20/lastName"));
    }


}
