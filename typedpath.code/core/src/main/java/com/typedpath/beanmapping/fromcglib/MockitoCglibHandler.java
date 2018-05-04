package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.TypedPath;
import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

public class MockitoCglibHandler extends Handler implements MethodInterceptor {
    public MockitoCglibHandler(TypedPath parent, Handler parentHandler, Class itemType,
                               Predicate<Class> isSimple, Function<Class, Object> uniqueSimpleValueDefaulter) {

        super(parent, parentHandler, itemType, isSimple, uniqueSimpleValueDefaulter);
    }

    protected <C> C create(Class<C> theClass, Handler handler) {
        return (C) Enhancer.create(theClass, (MockitoCglibHandler) handler);
    }

    protected Handler createChildHandler(TypedPath parent, Handler parentHandler, Class itemType,
                                         Predicate<Class> isSimple, Function<Class, Object> uniqueSimpleValueDefaulter) {
        return new MockitoCglibHandler(parent, this, itemType, isSimple, uniqueSimpleValueDefaulter);

    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (!isInterceptable(method)) {
            return methodProxy.invokeSuper(o, args);
        } else {
            return intercept(o, method, args);
        }
    }

}