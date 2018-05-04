package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.Mapper;
import com.typedpath.beanmapping.TypedListPath;
import com.typedpath.beanmapping.TypedPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * creates TypedPaths using CGLib
 * TODO implement skip
 */
public class Cglib2Path {

    private static ProxyFactory proxyFactory = null;

    static {
        //TODO Mockito 2
        final List<Supplier<ProxyFactory>> proxyFactorySuppliers =
                Arrays.asList(()->new MockitoCglibProxyFactory(), ()->new CglibProxyFactory() );

        List<String> errors = new ArrayList<>();
        //try each proxy supplier
        for (Supplier<ProxyFactory> proxyFactorySupplier : proxyFactorySuppliers) {
            ProxyFactory candidateProxyFactory = proxyFactorySupplier.get();
            String error = candidateProxyFactory.availibilityError();
            if (error==null) {
                proxyFactory = candidateProxyFactory;
            } else {
                errors.add(error);
            }
        }
        if (proxyFactory==null) {
            throw new RuntimeException(errors.stream().collect(Collectors.joining(", ")));
        }

    }

    public static <S> TypedPath<S, ?> root(
            Class<S> theClass, Consumer<S> ...rootConsumers
    ) {
        TypedPath<S, ?> root = new TypedPath<S, Object>(null, "", null, Object.class);
        root.Complex(true);
        Handler handler = proxyFactory.createHandler(root, null, null, Cglib2Path::defaultIsSimple,
                 Cglib2Path::defaultSimpleValueDefaulter
                   );
        S source = proxyFactory.create(theClass, handler);
        for (Consumer<S> rootConsumer : rootConsumers)
            rootConsumer.accept(source);
        return root;
    }

    public static <S, T> Function<S, T> select(T selectParent, Function<T, ?>... lss) {
        for (Function<T, ?> ls : lss) {
            ls.apply(selectParent);
        }
        return  (s) -> selectParent;
    }

    public static <S, T> Function<S, T> check(Function<S, T> ls, Function<Object, T> checker) {
        return  s -> {
            T result = ls.apply(s);
            TypedPath typedPath = Handler.getThreadLocalTypedPath();
            typedPath.check(checker);
            return result;
        };
    }

    public static <S, T> Function<S, T> as(Function<S, T> ls, String as) {
        return s -> {
            T result = ls.apply(s);
            TypedPath typedPath = Handler.getThreadLocalTypedPath();
            typedPath.as(as);
            return result;
        };
    }

    public static <T> T alias(T node, String as) {
        TypedPath typedPath = Handler.getTypedPath4Object(node);
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

        Handler itemhandler =  proxyFactory.createHandler(itemPath, handler, handler.itemType, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        T source = (T) proxyFactory.create(handler.itemType, itemhandler);
        Handler.setHandler4Object(source, itemhandler);

        return source;
    }

    public static <T> T all(List<T> node) {
        return filter(node, o -> true);
    }


    public static <F, T> T map(
            Class<F> fromClass, Class<T> toClass, BiConsumer<F, T> consumer, F source
    ) {
        TypedPath<F, ?> fromPath = new TypedPath<F, Object>(null, "", null, Object.class);
        fromPath.Complex(true);
        Handler fromHandler = proxyFactory.createHandler(fromPath, null, null, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        F fromTemplate = (F) proxyFactory.create(fromClass, fromHandler);

        TypedPath<T, ?> toPath = new TypedPath<T, T>(null, "", null, toClass);
        toPath.Complex(true);
        Handler toHandler = proxyFactory.createHandler(toPath, null, null, Cglib2Path::defaultIsSimple, Cglib2Path::defaultSimpleValueDefaulter);
        T toTemplate = (T) proxyFactory.create(toClass, toHandler);

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
