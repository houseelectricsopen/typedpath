package com.typedpath.template;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * implements typesafe templating by convention
 * TODO fix buildwatermark for override templates
 * Created by Robert Todd
 */
public class Templater {
    private final static String DEFAULT_LIST_JOIN = System.lineSeparator();

    public interface TemplateValues {
        default String inlineTemplate() {
            return null;
        }
        default String getListJoin() {
            return DEFAULT_LIST_JOIN;
        }
    }
    private final static String TEMPLATE_TOKEN_DELIMITER="##";
    private final static char EMBEDDED_TEMPLATE_TOKEN_DELIMITER='^';
    private final static String BUILD_WATERMARK_TOKEN =TEMPLATE_TOKEN_DELIMITER + "#BuildWatermark#" + TEMPLATE_TOKEN_DELIMITER;
    /**
     * TODO deprecate in favour of applyCondensedTemplateInferTemplateFromValuesType
     * templates by convention
     * example:
     * if the supplied class class is com.mydomain.MyValues
     * template is loaded relative to MyValues class with name MyValues.template
     * every public declared getter in the supplied object is assumed to have a peer in the template file
     * if the template has a token ##Property1## then the result of myValues.getProperty1() is substituted in
     * if the value happens to be a list then subtemplates are applied to individual values
     *
     * @param values
     * @return
     * @throws Exception
     */
    public String applyTemplateInferTemplateFromValuesType(TemplateValues values) throws Exception{
        String strTemplate=getTemplate(values, null);

        strTemplate = strTemplate.replaceAll(BUILD_WATERMARK_TOKEN, "built by " + getClass().getName() + " on " + new Date() + " from " + values.getClass().getName());

        Map<String, Function<Object, Object>> propertyName2Getter = (new PropertyAnalyser()).extractReadableProperties(values);

        for (Map.Entry<String, Function<Object, Object>> entry : propertyName2Getter.entrySet()) {
                  String propertyName = entry.getKey();
                  Object value = entry.getValue().apply(values);
                  String strTokenName = propertyName2TokenName(propertyName);
                  if  (value==null) {
                      throw new RuntimeException("no value specified for " + values.getClass().getName() + "." + propertyName + " => " + strTokenName);
                  }
                  String strValue=null;
                  //if its a list recurse !
                  if (List.class.isAssignableFrom(value.getClass())) {
                      List<String> strValues = new ArrayList<>();
                      Collector<CharSequence, ?, String> collector = null;
                      List lValue = (List) value;
                      for (Object subValue : lValue) {
                             if (!(subValue instanceof TemplateValues)) {
                                 throw new RuntimeException("property " + value.getClass().getName() + "." + strTokenName + " is not a list of TemplateValue");
                             }
                             TemplateValues templateSubValues = (TemplateValues) subValue;
                             String strSubValue = applyTemplateInferTemplateFromValuesType(templateSubValues);
                             strValues.add(strSubValue);
                             collector = Collectors.joining(templateSubValues.getListJoin());
                      }
                      if (collector==null)
                      {
                          collector = Collectors.joining(" ");
                      }

                      strValue= strValues.stream().collect(collector);
                  } else {
                      strValue = value.toString();
                  }

                  strTemplate = strTemplate.replaceAll(strTokenName, strValue);

        }
        if (strTemplate.contains(TEMPLATE_TOKEN_DELIMITER)) {
            throw new RuntimeException("unbound variable in template: " + values.getClass().getName() + " : " +  strTemplate.substring(strTemplate.indexOf("##")) +
            " \r\nnote property names start with upper case");
        }
        return strTemplate;
    }

    private String processBuildWaterMark(String strTemplate, Object ovalues) {
        return (strTemplate==null||ovalues==null)?null:strTemplate.replaceAll(BUILD_WATERMARK_TOKEN, "built by " + getClass().getName() + " on " + new Date()  + " from " + ovalues.getClass().getName());
    }

    private String getTemplate(Object ovalues, String overrideTemplate) throws Exception {
        String strTemplate = overrideTemplate;
        if (overrideTemplate==null && ovalues instanceof  TemplateValues) {
            TemplateValues values = (TemplateValues) ovalues ;
            if (values.inlineTemplate() != null) {
                strTemplate = values.inlineTemplate();
            } else {
                String templateResourceName = typeToResourceName(values.getClass());
                strTemplate = getTemplateByResourceName(values.getClass(), templateResourceName);
            }
        }
        return processBuildWaterMark(strTemplate, ovalues);
    }

    /***
     * TODO simplify
     * TODO update description bolow to encompass embedded templates
     templates by convention
     * example:
     * if the supplied class class is com.mydomain.MyValues
     * template is loaded relative to MyValues class with name MyValues.template
     * every public declared getter in the supplied object is assumed to have a peer in the template file
     * if the template has a token ##Property1## then the result of myValues.getProperty1() is substituted in
     * if the value happens to be a list then subtemplates are applied to individual values
     * the properties are templated by querying the property however this can be overriddedn in the template
     *     like this ##PropertyName##^##overriding template##^joinText^
     * @param values
     * @return
     * @throws Exception
     */
    public String applyCondensedTemplateInferTemplateFromValuesType(Object values, String overrideTemplate) throws Exception {
        String strTemplate;
        strTemplate = getTemplate(values, overrideTemplate);
        if (strTemplate==null) {
            throw new Exception("no template found for " + values.getClass().getName());
        }

        StringBuilder sbResult = new StringBuilder();
        int delimiterLength = TEMPLATE_TOKEN_DELIMITER.length();
        String listJoiner = null;
        Map<String, Function<Object, Object>> propertyNameToGetters = (new PropertyAnalyser()).extractReadableProperties(values);

        for (int pos=0; pos<strTemplate.length(); ) {
             int openingDelimiterIndex = strTemplate.indexOf("##", pos);
             if (openingDelimiterIndex==-1) {
                 sbResult.append(strTemplate.substring(pos));
                 break;
             }
             //write out test up to delimiter
             sbResult.append(strTemplate.substring(pos, openingDelimiterIndex));
             int closingDelimiterIndex  = strTemplate.indexOf("##" , openingDelimiterIndex+1);
             //delimiter is unterminated so just flush everything out
             if (closingDelimiterIndex==-1) {
                 sbResult.append(strTemplate.substring(openingDelimiterIndex));
                 break;
             }

             String embeddedTemplateText=null;


            // extract the propertyName
            String propertyName;
            try {
                propertyName = strTemplate.substring(openingDelimiterIndex + delimiterLength, closingDelimiterIndex);
            } catch (Exception ex) {
                String message = String.format("cant create proeprtyNmeopeningDelimiterIndex=%s delimiterLength=%s  closingDelimiterIndex==%s",
                        openingDelimiterIndex, delimiterLength, closingDelimiterIndex);
                throw new RuntimeException(message, ex);
            }
            pos = closingDelimiterIndex+delimiterLength;
            int embeddedTemplateStartIndex= closingDelimiterIndex+delimiterLength;
            //process an embedded template if available
            if (embeddedTemplateStartIndex<strTemplate.length() && strTemplate.charAt(embeddedTemplateStartIndex)==EMBEDDED_TEMPLATE_TOKEN_DELIMITER) {
                  //extract embedded token
                  embeddedTemplateStartIndex++;
                 //assume that embedded templates dont have embedded
                 int embeddedTemplateEndIndex = strTemplate.indexOf(EMBEDDED_TEMPLATE_TOKEN_DELIMITER, embeddedTemplateStartIndex);
                 if (embeddedTemplateEndIndex==-1) {
                     throw new Exception("unterminated embedded template");
                 }
                 int embeddedTemplateListJoinerEndIndex = strTemplate.indexOf(EMBEDDED_TEMPLATE_TOKEN_DELIMITER, embeddedTemplateEndIndex+1);
                 if (-1==embeddedTemplateListJoinerEndIndex) {
                     throw new Exception("unjoined embedded template - embdedded template started but not finished");
                 }
                 // extract the template text
                 embeddedTemplateText = strTemplate.substring(embeddedTemplateStartIndex, embeddedTemplateEndIndex);
                 // extract the joiner
                 listJoiner = strTemplate.substring(embeddedTemplateEndIndex+1, embeddedTemplateListJoinerEndIndex);
                 if (listJoiner.length()==0) {
                     listJoiner = DEFAULT_LIST_JOIN;
                 }
                 pos = embeddedTemplateListJoinerEndIndex+1;
             }

             if (!propertyNameToGetters.containsKey(propertyName)) {
                throw new Exception("cant find property " + propertyName + " in " + values.getClass().getName());
             }
             // get the value
            Object value = propertyNameToGetters.get(propertyName).apply(values);
            //if it is a Template values recurse
            String strValue=null;
            //if its a list recurse !
            if (List.class.isAssignableFrom(value.getClass())) {
                List<String> strValues = new ArrayList<>();
                Collector<CharSequence, ?, String> collector = null;
                List lValue = (List) value;
                for (Object subValue : lValue) {
                    if (!(subValue instanceof TemplateValues)) {
                        throw new RuntimeException("property " + value.getClass().getName() + "." + propertyName + " is not a list of TemplateValue");
                    }
                    TemplateValues templateSubValues = (TemplateValues) subValue;
                    String strSubValue = applyCondensedTemplateInferTemplateFromValuesType(templateSubValues, embeddedTemplateText);
                    strValues.add(strSubValue);
                    if (listJoiner==null) {
                        listJoiner = templateSubValues.getListJoin();
                    }
                    collector = Collectors.joining(listJoiner);
                }

                strValue= strValues==null || strValues.size()==0?"":strValues.stream().collect(collector);
            } else {
                strValue = value.toString();
            }
            sbResult.append(strValue);
         }
         return sbResult.toString();
    }


    private String typeToResourceName(Class theClass) {
        return theClass.getSimpleName() + ".template";
    }

    private String propertyName2TokenName(String propertyName) {
        return TEMPLATE_TOKEN_DELIMITER + propertyName + TEMPLATE_TOKEN_DELIMITER;
    }

    private String getTemplateByResourceName(Class theValuesClass, String resourceName) throws Exception {
        InputStream is = theValuesClass.getResourceAsStream(resourceName);
        if (is==null) {
                throw new RuntimeException("unable to find template " + theValuesClass.getName() + ".class/" + resourceName);
        }
        byte data[] = new byte[is.available()];
        is.read(data);
        return new String(data);
    }
}
