package com.typedpath.beanmapping;

import com.typedpath.beanmapping.fromjsonschema.JsonUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class JsonReader {

    public static  <T>  T extract( TypedPath<?, T> path, String strJson ) throws Exception {
        List<TypedPath> paths = path.asListTopLast();
        ScriptObjectMirror json =  JsonUtil.stringToJson(strJson);
        Object child=null;
        for (int index=paths.size()-2; index>=0; index--) {
            TypedPath currentPath = paths.get(index);
            child = json.get(currentPath.ShortName());
            if (child instanceof ScriptObjectMirror) json = (ScriptObjectMirror) child;
        }
        //TODO get the type from path
        return (T) child;
    }

    private static void expandList(Object o) {
        ScriptObjectMirror js = (ScriptObjectMirror)o;

    }

    /**
     * get from a path into the specified json - make a callback if the path is not available
     * this supercedes extract  above
     * **/
    public static <T> T get(TypedPath<?, T>  path, ScriptObjectMirror json, TypedPath.IncompletePathConsumer onIncompletePath) {
        TypedPath.PathDown pathDown = (downPath, parent) -> {
            ScriptObjectMirror  jsonParent = (ScriptObjectMirror) parent;
            if (jsonParent.containsKey(downPath.ShortName())) {
                return jsonParent.get(downPath.ShortName());
            } else {
                return null;
            }
        };
        Object oResult = path.get(json, onIncompletePath, pathDown);
            oResult = transform(oResult, path.returnType);

        return (T) oResult;
    }

    //TODO expand this
    //TODO make this open
    private static Object transform(Object o, Class theClass) {
        if (theClass==null || o==null) return o;
        if (theClass== UUID.class) {
            o = UUID.fromString(o.toString());
        }
        return o;
    }

    /**
     * //TODO fix case where path level has a first property but it doesnt exists creates invalid json
     * serializes an object based on path
     * Benefits over standard serializer library:
     *    its only 50 lines of code
     *    supports selection
     *    doesnt use reflection
     *    easy to modify to support advanced features such as FGAC
     * @param root
     * @param selectionPath
     * @return
     */
    public static String selectAsJson(Object root, TypedPath selectionPath) {
        StringBuilder stringBuilder = new StringBuilder();
        final List<String> indentStack= new ArrayList<>();
        indentStack.add("");
        int[] stackDepth= {0};
        Function<Integer, String> getIndent =(i)->
        {
            final String indentIncrement="    ";
            for (int index=indentStack.size()-1; indentStack.size()<(i+1); indentStack.add(indentStack.get(index)+indentIncrement)) {
            }
            return indentStack.get(i);
        };

        TypedPath.Move move = (boolean intoOutOf, TypedPath path, Object source, boolean isComplex, boolean isList, boolean isListItem, Integer propertyIndex) -> {
            if (intoOutOf) {
                String indent = getIndent.apply(stackDepth[0]);
                stackDepth[0]++;
                if (propertyIndex!=null && propertyIndex>0 ) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(indent);
                if (!isListItem) {
                    String strP = path.getAlias()!=null?path.getAlias():path.ShortName();
                    stringBuilder.append(String.format("\"%s\":", strP));
                }
                if (isComplex && !isList) {
                    stringBuilder.append("{");
                }
                if (isList) {
                    stringBuilder.append("[");
                }
                if (!isComplex) {
                    //TODO this will fail for non strings !
                    stringBuilder.append(String.format("\"%s\"", source));
                }
            } else {
                stackDepth[0]--;
                String indent = getIndent.apply(stackDepth[0]);

                if (isComplex && !isList) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(indent);
                    stringBuilder.append("}");
                }
                if (isList) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(indent);
                    stringBuilder.append("]");
                }
            }
        };
        selectionPath.followSelectedPathFromTop(move, root);

        return stringBuilder.toString();
    }



}
