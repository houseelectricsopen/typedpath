package com.typedpath.beanmapping;

import com.typedpath.beanmapping.fromjsonschema.JsonUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestUtil {

    public static <T> Function<T, Object> is(T expected) {

        return (T actual) -> {
            if (expected==null && actual==null || expected.equals(actual)) {
                  return null;
            } else {
                return String.format("expected: %s::%s actual %s::%s", expected==null?"null":expected.getClass().getName(),
                          expected , actual==null?"null":actual.getClass().getName(),
                          actual  );
            }
        };
    }

    public static <T> Function<T, Object> isOneOf(T... expected) {
        return (T actual) -> {
            for (T val : expected) {
                if (val.equals(actual)) {
                    return null;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("actual: " + actual + " is not one of:");
            for (T val : expected) {
                sb.append(" " + val);
            }
            return sb.toString();
        };
    }


    private static String nameStack2PathString(Stack<String> nameStack) {
        return nameStack.stream().collect(Collectors.joining("/"));
    }

    private static List scriptObjectMirrorAsList(Object source) {
        ScriptObjectMirror l = (ScriptObjectMirror)source;
        List<Object> result = new ArrayList<>();
        for (int done=0; done<10000; done++) {
            String index = Integer.toString(done);
            if (!l.containsKey(index)) {
                break;
            }
            result.add(l.get(""+done));
        }
        return result;
    }

    private static Object scriptObjectMirrorDown(TypedPath path, Object parent) {
        //TODO add translation here e.g string to UUID
        ScriptObjectMirror typedParent = ((ScriptObjectMirror)parent);
        return  typedParent.hasMember(path.ShortName())?typedParent.getMember(path.ShortName()):null;
    }

    public static class CheckError {
        private String description;
        private String pathString;
        private boolean notAvailable=true;

        public Object getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isNotAvailable() {
            return notAvailable;
        }

        public void setNotAvailable(boolean notAvailable) {
            this.notAvailable = notAvailable;
        }

        public String toString() {
            return  ""+pathString + ((notAvailable)?" not available ":"" + (description!=null?" " +description:""));
        }

        public String getPathString() {
            return pathString;
        }

        public void setPathString(String pathString) {
            this.pathString = pathString;
        }
    }

    public static List<CheckError> checkJsonPath(String strJson, TypedPath typedPath) throws Exception{

        ScriptObjectMirror json =  JsonUtil.stringToJson(strJson);
        TypedPath.PathDown down = TestUtil::scriptObjectMirrorDown;
        TypedPath.PathAsList asList = TestUtil::scriptObjectMirrorAsList;
        BiFunction<Object, Class, Object> translateToNativeObject = JsonUtil::ScriptObjectMirrorToNative;
        return checkPath(json, typedPath, down, asList, translateToNativeObject);
    }

    public static List<CheckError> checkPath(Object root, TypedPath typedPath) {
        TypedPath.PathDown down = (downPath, parent) ->
        {
            try {
                return downPath.get.apply(parent);
            } catch (Exception ex) {
                return null;
            }
        };

        return checkPath(root, typedPath, down, null, null);
    }

    private static List<CheckError> checkPath(Object root, TypedPath typedPath, TypedPath.PathDown down, TypedPath.PathAsList asList,
                                              BiFunction<Object, Class, Object> translateToNativeObject) {
        Stack<String> nameStack = new Stack<>();
        LinkedHashMap<String, Function> pathString2Check = new LinkedHashMap<>();
        LinkedHashMap<Function, String> check2pathString = new LinkedHashMap<>();
        TypedPath.Move onMetaMove = (boolean intoOutOf, TypedPath path, Object source, boolean isComplex,
                                     boolean isList, boolean isListItem, Integer propertyIndex) -> {
            if (intoOutOf) {
                nameStack.push(path.ShortName());
                if (path.checker!=null) {
                    String pathString = nameStack2PathString(nameStack);
                    pathString2Check.put(pathString, path.checker);
                    check2pathString.put(path.checker, pathString);
                }
            } else {
                nameStack.pop();
            }
        };
        typedPath.followSelectedPathFromTopMetaOnly(onMetaMove);

        List<CheckError> errors = new ArrayList<>();
        nameStack.clear();
        List<String> pathStringsChecked = new ArrayList<>();
        TypedPath.Move onMove = (boolean intoOutOf, TypedPath path, Object source, boolean isComplex,
                                 boolean isList, boolean isListItem, Integer propertyIndex) -> {
                  if (intoOutOf) {
                      nameStack.push(path.ShortName()==null?"_":path.ShortName());
                      if (path.checker!=null) {
                          Object testSource = translateToNativeObject==null?source:translateToNativeObject.apply(source, path.returnType);
                          Object error = path.checker.apply(testSource);
                          String pathString = nameStack2PathString(nameStack);
                          pathStringsChecked.add(pathString);
                          if (error!=null) {
                              CheckError checkError = new CheckError();
                              checkError.setDescription(""+error);
                              checkError.setPathString(pathString);
                              checkError.setNotAvailable(false);
                              errors.add(checkError);
                          }
                      }
                  } else {
                      nameStack.pop();
                  }
            };
        typedPath.followSelectedPathFromTop(onMove, root, down, asList);

        //check that each path has been checked
        for (String pathString : pathString2Check.keySet()) {
            if (!pathStringsChecked.contains(pathString)) {
                  CheckError checkError = new CheckError();
                  checkError.setPathString(pathString);
                  checkError.setNotAvailable(true);
                  errors.add(checkError);
            }
        }

        return errors;
    }




}
