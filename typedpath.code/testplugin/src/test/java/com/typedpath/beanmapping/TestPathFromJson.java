package com.typedpath.beanmapping;

import com.typedpath.beanmapping.fromjsonschema.JsonUtil;
import com.typedpath.test.genfromjson.sendingsheetcompleted.Hearing;
import com.typedpath.test.genfromjson.sendingsheetcompleted.SendingSheetCompleted;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.List;

import static com.typedpath.beanmapping.TestUtil.is;
import static com.typedpath.beanmapping.TestUtil.isOneOf;

public class TestPathFromJson {

    @Test
    public void testTypeSafeExtractWithPath() throws Exception {
        TypedPath<SendingSheetCompleted, String> courtCentreNamePath  = SendingSheetCompleted.root().hearing().courtCentreName();
        String json = ResourceUtil.readResource(new FileInputStream("./src/main/resources/fromjsonschema/sending-sheet-completed.data.json"));
        String courtCentreName =  JsonReader.extract(courtCentreNamePath, json);
        Assert.assertEquals(courtCentreName, "Warwick Justice Centre");
    }

    @Test
    public void testMultiChecking() throws Exception {
        Hearing.HearingPath hearingPath = SendingSheetCompleted.root().hearing().select(
                h->h.defendants().all().firstName().check(isOneOf("xxx", "David")),
                h->h.courtCentreId().check(is("2342344")),
                h->h.courtCentreName().check(is("sfsdfsdff")),
                h->h.defendants().get(20).firstName().check(is("cool"))
        );
        String json = ResourceUtil.readResource(new FileInputStream("./src/main/resources/fromjsonschema/sending-sheet-completed.data.json"));
        List<TestUtil.CheckError> errors = TestUtil.checkJsonPath(json, hearingPath);
        errors.forEach(e->System.out.println(e));
    }

    @Test
    public void temp() throws Exception {
        String strJson = ResourceUtil.readResource(new FileInputStream("./src/main/resources/fromjsonschema/sending-sheet-completed.data.json"));
        ScriptObjectMirror json =  JsonUtil.stringToJson(strJson);
        ScriptObjectMirror hearing = (ScriptObjectMirror) json.getMember("hearing");
        //hearing.containsKey()
        Object oDefendants = (ScriptObjectMirror) hearing.getMember("defendants");


    }

}
