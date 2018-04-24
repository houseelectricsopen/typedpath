package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.Mapper;
import com.typedpath.beanmapping.TypedListPath;
import com.typedpath.beanmapping.TypedPath;
import net.sf.cglib.proxy.Enhancer;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * creates TypedPaths using CGLib
 * TODO implement skip
 */
public class Cglib2Path {

    public static <S> TypedPath<S, ?> root(
            Class<S> theClass, Consumer<S> ...rootConsumers
    ) {
        UniqueSimpleValueDefaulter.reset();
        TypedPath<S, ?> root = new TypedPath<S, Object>(null, "", null, Object.class);
        root.Complex(true);
        Handler handler = new Handler(root, null, null, Cglib2Path::defaultIsSimple,
                 Cglib2Path::defaultSimpleValueDefaulter
                   );
        S source = (S) Enhancer.create(theClass, handler);
        for (Consumer<S> rootConsumer : rootConsumers)
            rootConsumer.accept(source);
        return root;
    }

    public static <S, T> Function<S, T> select(T selectParent, Function<T, ?>... lss) {
        for (Function<T, ?> ls : lss) {
            ls.apply(selectParent);
        }
        Function<S, T> selectParentFunction = (s) -> selectParent;
        return selectParentFunction;
    }

    public static <S, T> Function<S, T> check(Function<S, T> ls, Function<Object, T> checker) {
        Function<S, T> resultF = s -> {
            T result = ls.apply(s);
            TypedPath typedPath = Handler.getThreadLocalTypedPath();
            typedPath.check(checker);
            return result;
        };
        return resultF;
    }

    public static <S, T> Function<S, T> as(Function<S, T> ls, String as) {
        Function<S, T> resultF = s -> {
            T result = ls.apply(s);
            TypedPath typedPath = Handler.getThreadLocalTypedPath();
            typedPath.as(as);
            return result;
        };
        return resultF;
    }

    public static <T> T alias(T node, String as) {
        TypedPath typedPath = Handler.getTypedPath4Object(node);//    Handler.getThreadLocalTypedPath();
        typedPath.as(as);
        return node;
    }

    public static <T> T filter(List<T> node, Predicate<Object> filterFunction) {
        TypedListPath typedPath = (TypedListPath) Handler.getTypedPath4Object(node);
        Handler handler = Handler.getHandler4Object(node);
        typedPath.Filter(filterFunction);
        TypedPath itemPath = new TypedPath(typedPath, "", null, handler.itemType);
        typedPath.touch(itemPath);
        itemPath.Complex(true);

        //TODO move this into handler
        Handler itemhandler = new Handler(itemPath, handler, handler.itemType, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        T source = (T) Enhancer.create(handler.itemType, itemhandler);
        Handler.setHandler4Object(source, itemhandler);
        Handler handler4Obj = Handler.getHandler4Object(source);

        return source;
    }

    public static <T> T all(List<T> node) {
        return filter(node, o -> true);
    }


    public static <F, T> T map(
            Class<F> fromClass, Class<T> toClass, BiConsumer<F, T> consumer, F source
    ) {
        UniqueSimpleValueDefaulter.reset();
        TypedPath<F, ?> fromPath = new TypedPath<F, Object>(null, "", null, Object.class);
        fromPath.Complex(true);
        Handler fromHandler = new Handler(fromPath, null, null, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        F fromTemplate = (F) Enhancer.create(fromClass, fromHandler);

        TypedPath<T, ?> toPath = new TypedPath<T, T>(null, "", null, toClass);
        toPath.Complex(true);
        Handler toHandler = new Handler(toPath, null, null, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        T toTemplate = (T) Enhancer.create(toClass, toHandler);

        //its considered an error for there not to be a link at the top level
        //TODO review this rule
        fromPath.setLink(toPath);

        consumer.accept(fromTemplate, toTemplate);
        return (new Mapper()).mapSingle(fromPath, toPath, source);
    }

    public static <F, T> void link(F from, T to, BiConsumer<F, T> consumer) {
        TypedPath fromPath = Handler.getTypedPath4Object(from);
        TypedPath toPath = Handler.getTypedPath4Object(to);
        if (fromPath == null) {
            throw new RuntimeException("cant find path for " + from + " latestPath==" + Handler.getThreadLocalTypedPath());
        }
        if (toPath == null) {
            throw new RuntimeException("cant find path for " + to + " latestPath==" + Handler.getThreadLocalTypedPath());
        }
        fromPath.setLink(toPath);
        consumer.accept(from, to);
    }

    public static <F, T> void into(F from, T to) {
        TypedPath fromPath = Handler.getTypedPath4Object(from);
        TypedPath toPath = Handler.getTypedPath4Object(to);
        if (fromPath == null) {
            throw new RuntimeException("cant find path for " + from + " latestPath==" + Handler.getThreadLocalTypedPath());
        }
        if (toPath == null) {
            throw new RuntimeException("cant find path for " + to + " latestPath==" + Handler.getThreadLocalTypedPath());
        }
        fromPath.setInto(toPath);
    }

    private static boolean defaultIsSimple(Class theClass) {
        if (theClass.isPrimitive()) {
            return true;
        }
        else if (theClass == UUID.class) {
            return true;
        }
        else if (theClass.getPackage().getName().startsWith("java.lang")) {
            return true;
        }
        else if (theClass.getPackage().getName().startsWith("java.time")) {
            return true;
        }
        else {
            return false;
        }
    }

    private static Object defaultSimpleValueDefaulter(Class theClass) {
        return UniqueSimpleValueDefaulter.getNextValue(theClass);
    }




}
