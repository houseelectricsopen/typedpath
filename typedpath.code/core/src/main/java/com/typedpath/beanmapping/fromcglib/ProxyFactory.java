package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.TypedPath;

import java.util.function.Function;
import java.util.function.Predicate;

interface ProxyFactory {

    //TODO - conisder merging these 2 and combine with Handler registration
    Handler createHandler(TypedPath parent, Handler parentHandler, Class itemType,
                          Predicate<Class> isSimple, Function<Class, Object> uniqueSimpleValueDefaulter);
    <S> S create(Class<S> theClass, Handler handler);

    String description();

    String availibilityError();

}
