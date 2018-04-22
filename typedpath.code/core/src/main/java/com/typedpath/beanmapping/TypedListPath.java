package com.typedpath.beanmapping;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

//TODO get rid of this ?
public class TypedListPath<S, T, P extends TypedPath> extends TypedPath<S, T> {


    public TypedListPath(TypedPath parent, String shortName, Function<Object, T> get) {
       super(parent, shortName, get, (Class<T>) List.class);
        this.list=true;
    }

    protected Predicate<Object> filter=null;

    public void Filter(Predicate<Object> filter) {
        this.filter=filter;
    }

    @Override
    public boolean isList() {
        return filter!=null;
    }
   //  just confuses autocomplete - hence moved to template!
    // public abstract P get(int index);

}
