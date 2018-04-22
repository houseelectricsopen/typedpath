package com.typedpath.beanmapping;

import com.typedpath.beanmapping.TypedPath;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class Mapper {

    private boolean verbose = false;

    public void Verbose(boolean verbose) {
        this.verbose = verbose;
    }

    public interface PropertySetter {
        public void set(Object parent, String propertyName, Object value);
    }

    private Object instantiatePathUp(TypedPath parentPath, Object parent, TypedPath typedPath,
                                     Function<Class, Object> objectFactory, PropertySetter propertySetter) {

        TypedPath.IncompletePathConsumer onIncompletePath = null;
        TypedPath.PathDown pathDown = (downPath, theParent) -> {
            Object result = null;
            if (theParent instanceof List && downPath.getGet() == null) {
                result = objectFactory.apply(downPath.returnType);
                ((List) theParent).add(result);
            } else {
                if (downPath.getGet() == null) {
                    throw new RuntimeException("pathDown: no get found for downPath " + downPath);
                }
                result = downPath.getGet().apply(parent);
            }

            if (result == null) {
                result = objectFactory.apply(downPath.returnType);
                propertySetter.set(theParent, downPath.ShortName(), result);
            }
            return result;
        };

        //TODO fix this - should be going to the top and coming down
        Object result = typedPath.getWithUpperlimit(parent, onIncompletePath, pathDown, parentPath);
        return result;
    }

    private void instantiatePathUpAndSet(TypedPath contextPath, Object context, TypedPath path, Object value,
                                         Function<Class, Object> objectFactory, PropertySetter propertySetter) {
        Object parent;// =  path.Parent().get(con);//this.instantiatePathUp(contextPath, context,  path,  objectFactory, propertySetter);
        if (path.Parent() != contextPath) {
            throw new RuntimeException(String.format("TODO instantiatePathUpAndSet complex subpath %s in %s", path, contextPath));
        }
        propertySetter.set(context, path.ShortName(), value);

    }

    public <S, U> U mapSingle(TypedPath<S, ?> from, TypedPath<U, ?> to, S source) {
        Function<Class, Object> objectFactory = (theClass) -> {
            try {
                if (theClass == List.class) {
                    theClass = ArrayList.class;
                }
                return theClass.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("cant create a new " + theClass.getName(), ex);
            }
        };
        PropertySetter propertySetter = (theParent, propertyName, value) -> {
            try {
                Field declaredField = theParent.getClass().getDeclaredField(propertyName);
                declaredField.setAccessible(true);
                declaredField.set(theParent, value);
            } catch (Exception ex) {
                throw new RuntimeException(String.format("cant set %s in %s with value %s)", propertyName, theParent, value), ex);
            }
        };
        return mapSingle(from, to, source, objectFactory, propertySetter);
    }

    public <S, U> U mapSingle(TypedPath<S, ?> from, TypedPath<U, ?> to, S source,
                              Function<Class, Object> objectFactory, PropertySetter propertySetter
    ) {
        Stack<Object> toStack = new Stack<>();
        Stack<TypedPath> toPathStack = new Stack();
        List<Object> roots = new ArrayList<>();
        TypedPath.Move move = (intoOutOf, path, aSource, isComplex, isList, isListItem, propertyIndex) -> {
            if (intoOutOf) {
                //if this is an linked path make sure path exists in destination
                if (verbose) System.out.println("move in " + path.ShortName());
                if (path.getLink() != null) {
                    if (verbose)
                        System.out.println("in " + toStack.size() + " " + path.ShortName() + "->" + path.getLink().ShortName());
                    Object context = null;
                    if (toStack.size() == 0) {
                        //check that the top link is the to
                        if (path.getLink() != to) {
                            throw new RuntimeException(
                                    String.format("specified top link %s does not match first link %s", to, path.getLink())
                            );
                        }
                        context = objectFactory.apply(path.getLink().returnType);
                        roots.add(context);
                    } else {
                        Object parent = toStack.peek();
                        TypedPath parentPath = toPathStack.peek();
                        context = instantiatePathUp(parentPath, parent, path.getLink(), objectFactory, propertySetter);
                        if (context instanceof List) {
                            //TODO add this to stack
                            Object item = objectFactory.apply(path.getLink().returnType);
                            ((List) context).add(item);
                            //System.out.println("context path== " + contextPath.ShortName());
                            context = item;
                        }
                    }
                    toStack.push(context);
                    toPathStack.push(path.getLink());
                } else if (path.getInto() != null) {
                    Object context = toStack.peek();
                    TypedPath contextPath = toPathStack.peek();
                    if (verbose)
                        System.out.println("into " + toStack.size() + " " + path.ShortName() + "->" + path.getInto().ShortName());
                    instantiatePathUpAndSet(contextPath, context, path.getInto(), aSource, objectFactory, propertySetter);
                }
            } else {
                if (verbose) System.out.println("move out " + path.ShortName());
                if (path.getLink() != null) {
                    toPathStack.pop();
                    toStack.pop();
                    if (verbose)
                        System.out.println("out " + toStack.size() + " " + path.ShortName() + "->" + path.getLink().ShortName());
                }
            }
        };

        from.followSelectedPathFromTop(move, source);
        if (roots.size() != 1) {
            throw new RuntimeException("found " + roots.size() + " expected 1");
        }
        if (toStack.size() != 0) {
            throw new RuntimeException("unbalanced stack: " + toStack.size() + " unclosed levels");
        }
        return (U) roots.get(0);
    }

}
