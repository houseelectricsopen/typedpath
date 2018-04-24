package com.typedpath.beanmapping.fromcglib;

import com.typedpath.beanmapping.TypedListPath;
import com.typedpath.beanmapping.TypedPath;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Handler  implements MethodInterceptor {

    final TypedPath parent;
    final Handler parentHandler;
    final Class itemType;
    final Predicate<Class> isSimple;
    final Function<Class, Object> uniqueSimpleValueDefaulter;
    public Handler(TypedPath parent, Handler parentHandler, Class itemType,
                   Predicate<Class> isSimple, Function<Class, Object> uniqueSimpleValueDefaulter) {
        this.parent = parent;
        this.parentHandler=parentHandler;
        this.itemType=itemType;
        this.isSimple=isSimple;
        this.uniqueSimpleValueDefaulter=uniqueSimpleValueDefaulter;
    }

    private TypedPath createListGetter(int index, Class itemType) {
        String shortName = "" + index;
        Function getter = p -> {
            try {
                List pl = (List)p;
                return pl.size()>index?pl.get(index) : null;
            } catch (Exception ex) {
                throw new RuntimeException("failed to navigate " + shortName + " on " + p, ex);
            }
        };
        return new TypedPath(parent, shortName, getter,itemType);
    }

    private boolean isListGet(Method method) {
        return "get".equals(method.getName()) && method.getDeclaringClass().isAssignableFrom(List.class) ;
    }

    private String methodName2PropertyName(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            return methodName.substring(2);
        } else {
            throw new RuntimeException(String.format("invalid Method %s ", methodName));
        }
    }

    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (   !method.getName().startsWith("get") && !method.getName().startsWith("is")) {
            return methodProxy.invokeSuper(o, args);
        }

        TypedPath child = null;
        Class returnType =null;
        Class itemType = null;
        //is this a list with a generic type ?
        if (List.class.isAssignableFrom(method.getReturnType())) {
            Type genType = method.getGenericReturnType();
            itemType = (Class<?>)((ParameterizedType) genType).getActualTypeArguments()[0];
        }
        if (isListGet(method)) {
            child =  createListGetter((Integer)args[0], this.itemType);
            returnType = this.itemType;
        } else {
            String shortName = methodName2PropertyName(method.getName());
            shortName = shortName.substring(0, 1).toLowerCase() + shortName.substring(1);
            Object[] noArgs = {};
            Function getter = p -> {
                try {
                     return method.invoke(p, noArgs);
                } catch (Exception ex) {
                    throw new RuntimeException(String.format("failed to navigate %s on %s::%s", method.getName(), p.getClass().getSimpleName(),
                            p), ex);
                }
            };
            if (itemType!=null) {
                child = new TypedListPath(parent, shortName, getter);
            } else {
                child = new TypedPath(parent, shortName, getter, method.getReturnType());
            }
            returnType = method.getReturnType();
        }

        parent.touch(child);
        Handler childHandler = new Handler(child, this, itemType, isSimple, uniqueSimpleValueDefaulter);
        latestTypedPath.set(child);
        Object result;
        if (returnType==null) {
            throw new RuntimeException("no returnType for " + method.getName());
        }
        if (isSimple.test(returnType)) {
            //need to create a default value
            result = uniqueSimpleValueDefaulter.apply(returnType);
        } else {
            child.Complex(true);
            result = Enhancer.create(returnType, childHandler);
        }
        getObject2Handler().put(result, childHandler);
        return result;
    }

    // TODO review these mappings
    private static ThreadLocal<TypedPath> latestTypedPath = new ThreadLocal<>();
    // this is for mapping objects based on == not .equals hence type is IdentityHashMap
    private static ThreadLocal<IdentityHashMap<Object, Handler>> object2Handler = new ThreadLocal<>();

    private static IdentityHashMap<Object, Handler>getObject2Handler() {
        if (object2Handler.get()==null) {
            object2Handler.set(new IdentityHashMap<>());
        }
        return object2Handler.get();
    }

    static Handler getHandler4Object(Object o) {
        return object2Handler.get().get(o);
    }

    static Handler setHandler4Object(Object o, Handler handler) {
        return object2Handler.get().put(o, handler);
    }

    static TypedPath getTypedPath4Object(Object o) {
        return getObject2Handler().get(o).parent;
    }

    static TypedPath getThreadLocalTypedPath() {
        return latestTypedPath.get();
    }

}
