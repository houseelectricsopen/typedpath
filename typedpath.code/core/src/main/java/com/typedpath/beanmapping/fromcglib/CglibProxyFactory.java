package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.TypedPath;
import net.sf.cglib.proxy.Enhancer;

import java.util.function.Function;
import java.util.function.Predicate;

public class CglibProxyFactory implements ProxyFactory {

    @Override
    public Handler createHandler(TypedPath parent, Handler parentHandler, Class itemType, Predicate<Class> isSimple, Function<Class, Object> uniqueSimpleValueDefaulter) {
        Handler handler = new CglibHandler(parent, parentHandler, itemType, isSimple,uniqueSimpleValueDefaulter);
        return handler;
    }

    @Override
    public <S> S create(Class<S> theClass, Handler handler) {
        S source = (S) Enhancer.create(theClass, (CglibHandler) handler);
        return source;
    }

    @Override
    public String description() {
        return "Cglib based proxies";
    }

    @Override
    public String availibilityError() {
        String className="net.sf.cglib.proxy.Enhancer";
        try {
            Class.forName(className);
            return null;
        }
        catch (Exception ex) {
             return "cant load class " + className + " " + ex;
        }
    }

}
