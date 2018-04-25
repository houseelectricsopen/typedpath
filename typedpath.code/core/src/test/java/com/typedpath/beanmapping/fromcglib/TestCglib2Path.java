package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.JsonReader;
import com.typedpath.beanmapping.TestUtil;
import com.typedpath.beanmapping.TypedPath;
import com.typedpath.testdomain.immutablebean.Address;
import com.typedpath.testdomain.immutablebean.Defendant;
import org.junit.*;
import com.typedpath.testdomain.immutablebean.Ahearing;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static com.typedpath.beanmapping.fromcglib.Cglib2Path.*;
import static com.typedpath.beanmapping.TestUtil.*;

/**
 * TODO
 *    test for skip
 */
public class TestCglib2Path {


    /**
     * this test:
     * (1) creates a path using Cglib2Path
     *           this path includes checks at various points
     * (2) create test data
     * (3) select the path from the test data into json
     * (4) evaluate the checks based on the json
     * Note that List.get(n) maps to a single json object
     * so List.gets in a path overwrite each other in json
     * in this example defendants.get(10) is aliased so it doesnt get overwritten by defendants.get(0) in json
     *
     * @throws Exception
     */
    @Test
    public void testBeanAccess2PathWithListGet() throws Exception {
        //create path
        TypedPath<Ahearing, ?> path = root(
                Ahearing.class,
                h -> {
                    //alias is necessary to stop json mapping being overwritten by defendants.get(0)
                    select(alias(h.getDefendants(), "defendant10").get(10),
                    check(d -> d.getLastName(), is("going get overwritten")));

                    select(h.getDefendants().get(0),
                            d->d.getLastName(),
                            check(d -> d.getLastName(), is("peter")),
                            d -> select(d.getAddress(),
                                    check(a -> a.getAddress1(), is("Elmers End")),
                                    a -> a.getAddress2())

                    );
                }

        );

        System.out.println("created path: " + path);

        //create test data
        Ahearing ahearing = sampleHearing();

        // map object to json based on path
        String strJson = JsonReader.selectAsJson(ahearing, path);
        System.out.println(strJson);

        // carry out checks
        List<CheckError> errors = TestUtil.checkJsonPath(strJson, path);
        errors.forEach(e -> System.out.println(e));
        Assert.assertEquals(errors.size(), 3);
        Assert.assertTrue(errors.get(0).getPathString().equals("/defendants/0/lastName"));
        Assert.assertTrue(errors.get(1).getPathString().equals("/defendants/0/address/address1"));
        //this test had no chance because defendants.0 overwrites defendants.10
        Assert.assertTrue(errors.get(2).getPathString().equals("/defendants/10/lastName"));

    }


    /**
     * this test:
     * (1) creates a path using Cglib2Path
     * this path includes checks at various points
     * (2) create test data
     * (3) select the path from the test data into json
     * (4) evaluate the checks on the json
     * Note that List.get(n) maps to a single json object
     * so List.gets in a path overwrite each other in json
     * in this example defendants.get(10) is aliased so it doesnt get overwritten by defendants.get(0) in json
     *
     * @throws Exception
     */
    @Test
    public void testBeanAccess2PathWithListFilter() throws Exception {
        //filter used in both java and json objects
        Predicate<Object> defendantFilter = d -> {
            if (d instanceof Defendant) {
                return ((Defendant) d).getForeName().startsWith("J");
            } else {
                //accept all json defendants
                return true;
            }
        };

        //create test data
        Ahearing ahearing = sampleHearing();
        Defendant defendant0 = ahearing.getDefendants().get(0);

        //create path
        TypedPath<Ahearing, ?> path = root(
                Ahearing.class,
                h -> {
                    System.out.println("creating the path !");
                    select(h, check(hx->hx.getId(), is(ahearing.getId())));
                    select(
                            filter(h.getDefendants(), defendantFilter),
                            Defendant::getForeName,
                            check(d -> d.getLastName(), is("peter")),
                            check(d->d.getDateOfBirth(), is(defendant0.getDateOfBirth())),
                            d -> select(d.getAddress(),
                                    check(a -> a.getAddress1(), is("Elmers End")),
                                    a -> a.getAddress2())

                    );
                }
        );

        System.out.println("created path: " + path);

        // map object to json based on path
        String strJson = JsonReader.selectAsJson(ahearing, path);
        System.out.println("json:" + System.lineSeparator() + strJson);

        // carry out checks
        List<CheckError> errors = TestUtil.checkJsonPath(strJson, path);
        errors.forEach(e -> System.out.println(e));
        Assert.assertEquals(2, errors.size());
        Assert.assertTrue(errors.get(0).getPathString().equals("/defendants//lastName"));
        Assert.assertFalse(errors.get(0).isNotAvailable());
        Assert.assertTrue(errors.get(1).getPathString().equals("/defendants//address/address1"));
        Assert.assertFalse(errors.get(1).isNotAvailable());

    }

    /**
     * testing mapping
     * (1) create test data
     * (2) map AHearing into a hearing summary
     *          link at defendant level
     *             map Defendant.ForName to DefendantSummary.FirstNAme
     *             map Defendant.LastName to DefendantSummary.LastName
     *             map Defendant,Address.Address1 to DefendantSummary.Address1
     *  (3) create a path to check defendant summaries
     *  (4) execute the checks in this path
     */
    @Test
    public void testMapping() {

        Ahearing sampleHearing = sampleHearing();

        //typesafe mapping
        HearingSummary summary = map(Ahearing.class, HearingSummary.class,
                (h, s) -> {
                    link(all(h.getDefendants()), all(s.getDefendantSummaries()), (d, ds) ->
                    {
                        into(d.getForeName(), ds.getFirstName());
                        into(d.getLastName(), ds.getLastName());
                        into(d.getAddress().getAddress1(), ds.getAddress1());
                    });
                }, sampleHearing);

        Defendant defendant0 = sampleHearing.getDefendants().get(0);
        List<CheckError> errors = TestUtil.checkPath(summary, root(
                HearingSummary.class, h -> {
                    select(h.getDefendantSummaries().get(0),
                            check(ds -> ds.getFirstName(), is(defendant0.getForeName())),
                            check(ds -> ds.getLastName(), is(defendant0.getLastName())),
                            check(ds -> ds.getAddress1(), is(defendant0.getAddress().getAddress1()))
                    );
                }
        ));
        errors.forEach(e -> System.out.println(e));
        Assert.assertTrue(errors.size() == 0);

    }

    private Ahearing sampleHearing() {
        return new Ahearing.Builder()
                .withId(UUID.randomUUID())
                .withDefendants(
                Arrays.asList(
                        new Defendant.Builder()
                                .withForeName("John")
                                .withLastName("Smith")
                                .withDateOfBirth(LocalDate.now())
                                .withAddress(
                                        new Address.Builder()
                                                .withAddress1("123 Sesame Street")
                                                .withAddress2("TellyTubbyLand")
                                                .build()
                                )
                                .build()
                )
        ).build();

    }

    /**
     * test each simpe property type
     *  (0) create sample data
     * (1) create a path including checks against sample data
     * (2) serialize to json based on path
     * (3) execute the checks against the json
      */
  @Test
  public void testSimplePropertyTypes() throws Exception {
        //create test data
        MultiPropertyTypes testData =  new MultiPropertyTypes();
        testData.setBooleanObject(new Boolean(false));
        testData.setPrimitiveBoolean(true);
        testData.setByteObject(new Byte((byte)1));
        testData.setPrimitiveByte((byte)2);
        testData.setShortObject((short)3);
        testData.setPrimitiveShort((short)4);
        testData.setIntegerObject(new Integer(5));
        testData.setPrimitiveInteger(6);
        testData.setLongObject(new Long(7));
        testData.setPrimitiveLong(8);
        testData.setFloatObject(new Float(9));
        testData.setPrimitiveFloat(10);
        testData.setDoubleObject(new Double(11));
        testData.setCharacterObject(new Character('a'));
        testData.setPrimitiveCharacter('b');

       //create a path including checks
        TypedPath<MultiPropertyTypes, ?> path =
              Cglib2Path.root(MultiPropertyTypes.class,
                      m->{
                         select(m,
                                 check(m1->m1.getLongObject(), is(testData.getLongObject())),
                                 check(m1->m1.getPrimitiveLong(), is(testData.getPrimitiveLong())),
                                 check(m1->m1.getIntegerObject(), is(testData.getIntegerObject())),
                                 check(m1->m1.getPrimitiveInteger(), is(testData.getPrimitiveInteger())),
                                 check(m1->m1.getShortObject(), is(testData.getShortObject())),
                                 check(m1->m1.getPrimitiveShort(), is(testData.getPrimitiveShort())),
                                 check(m1->m1.getByteObject(), is(testData.getByteObject())),
                                 check(m1->m1.getPrimitiveByte(), is(testData.getPrimitiveByte())),
                                 check(m1->m1.isPrimitiveBoolean(), is(testData.isPrimitiveBoolean())),
                                 check(m1->m1.getBooleanObject(), is(testData.getBooleanObject())
                                 )
              );

  });

   //serialize to json based on path
   String strJson =    JsonReader.selectAsJson(testData, path);
   System.out.println(strJson);

   //evaluate the checks on the json
   List<CheckError> errors = TestUtil.checkJsonPath(strJson, path);
   errors.forEach(e->System.out.println(e));
   Assert.assertEquals(0, errors.size());
 }

}
