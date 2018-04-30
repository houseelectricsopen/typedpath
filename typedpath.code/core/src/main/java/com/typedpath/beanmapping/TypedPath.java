package com.typedpath.beanmapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TODO resolve: all fluent methods (used in queries) such as "check" should start lower case
 *               all getters/ setters should start with capital e.g ShortName()
 *
 * @param <S>
 * @param <T>
 */
public class TypedPath<S, T> {
    public String ShortName() {return shortName;}
    private TypedPath parent;
    /**i am complex and i have issues**/
    protected boolean complex = false;
    protected boolean list = false;
    protected String shortName;
    protected String alias;
    protected boolean skipOnTraverse;
    protected Function<T, Object> checker=null;

    public Function<T, Object> Checker() {
        return checker;
    }
    public TypedPath Parent() {
        return parent;
    }
    public void addUnionPaths(String name, List<TypedPath> paths) {
        unionPathSets.put(name, paths);
    }
    //attaches a check method
    public TypedPath<S, T> check(Function<T, Object> checker) {
        this.checker=checker;
        return this;
    }

    private LinkedHashMap<String, List<TypedPath>> unionPathSets = new LinkedHashMap<>();
    static class GroupBy{
        TypedPath groupKeyGetter;
        Function<Object, TypedPath> groupPathGetter;
        GroupBy(TypedPath groupKeyGetter, Function<Object, TypedPath> groupPathGetter) {
            this.groupKeyGetter=groupKeyGetter;
            this.groupPathGetter=groupPathGetter;
        }
    }
    private LinkedHashMap<String, GroupBy> groupBys = new LinkedHashMap<>();

    protected  <G> void  addGroupBy(String name, TypedPath<S, G> groupKeyGetter,
                                Function<G, TypedPath<S, ?>> groupPathGetter) {
        Function<Object, TypedPath> groupPathGetterIn =
                (o)->groupPathGetter.apply((G)o);
        this.groupBys.put(name, new GroupBy( groupKeyGetter, groupPathGetterIn));
    }


    public final Class<T> returnType;
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public boolean isSkipOnTraverse() {
        return skipOnTraverse;
    }

    public TypedPath<S, T> as(String alias) {
        this.alias = alias;
        return this;
    }

    public <M> TypedPath<S, M> map(String as, Function<T, M>  mapper, Class<M> returnType) {
        Function<Object, M> mappedGet = o->{
          return o==null?null:mapper.apply((T)o);
        };
        TypedPath<S, M> result = new TypedPath<S, M>(this, as, mappedGet, returnType);
        this.touched.add(result);
        this.skipOnTraverse=true;
        return result;
    }

    public void Complex(boolean complex) {
        this.complex=complex;
    }

    public boolean isComplex() {
        return complex;
    }

    public void List(boolean list) {
        this.list=list;
    }

    public boolean isList() {
        return list;
    }

    protected List<TypedPath> touched = new ArrayList<>();
    public List<TypedPath> getTouched() {
        return touched;
    }
    protected static TypedPath star = new TypedPath(null, "*");

    protected List<TypedPath> asListTopLast() {
        List<TypedPath> result = new ArrayList<>();
        for (TypedPath tp = this; tp!=null; result.add(tp), tp=tp.parent ) {
        }
        return result;
    }

    //TODO remove this
    public TypedPath(TypedPath parent, String shortName) {
        this.parent = parent;this.shortName=shortName;
        this.get=null;
        this.returnType=null;
    }

    public TypedPath(TypedPath parent, String shortName, Function<Object, T> get, Class<T> returnType) {
        this.parent = parent;this.shortName=shortName;
        this.get=get;
        this.returnType=returnType;
    }


    protected final Function<Object, T> get;
    public Function<Object, T> getGet() {
        return get;
    }

    private String VisitedAsString(String linePrefix) {
        return touched.stream().map(tp->tp.toString(linePrefix)).collect(Collectors.joining(","));
    }

    public void touch(TypedPath leaf) {
        touched.add(leaf);
    }

    public <T> void selectConstant(final String name, final T value, Class<T> theClass) {
        touched.add(new TypedPath(this,
                name, (p)->value, theClass));
    }

    public TypedPath _() {
        return this.parent;
    }

    //TODO rename this ? should it be publlc ?
    public TypedPath _top() {
        TypedPath top = this;
        for ( ; top.parent!=null ; top = top.parent ) {
        }
        return top;
    }

    public interface IncompletePathConsumer {
        public void consume(List<TypedPath> downStack, Integer pathEndIndex);
    }

    public interface PathDown {
        public Object down(TypedPath downPath, Object parent);
    }

    public interface PathAsList {
        public List asList(Object source);
    }


    //TODO consider renaming as getSingle
    public T get(Object o, IncompletePathConsumer onIncompletePath) {
        return get(o, onIncompletePath, (downPath, oo) -> downPath.get.apply(oo));

    }

    /**
     * note the top of the down stack is not used
     * @param upperLimit
     * @return
     */
    private List<TypedPath> buildDownStack(TypedPath upperLimit) {
        final List<TypedPath> downStack = new ArrayList<>();
        TypedPath here = this;
        for (;here!=null; downStack.add(here), here=here.parent ) {
            if (upperLimit!=null && here == upperLimit) {
                downStack.add(upperLimit);
                break;
            }
        }
        return downStack;
    }

    //TODO reimplemnt using generic navigator
    //TODO deal gracefully with non single path
    //TODO consider renaming as getSingle
    public T get(Object o, IncompletePathConsumer onIncompletePath, PathDown pathDown) {
        return this.getWithUpperlimit(o, onIncompletePath, pathDown, null);
    }

    public T getWithUpperlimit(Object o, IncompletePathConsumer onIncompletePath, PathDown pathDown, TypedPath upperLimitPath) {
        final List<TypedPath> downStack = buildDownStack(upperLimitPath);
        for (int done=downStack.size()-2; done>=0 ; done--) {
            o = pathDown.down(downStack.get(done), o);
            if (o==null && done!=0) {
                if (onIncompletePath!=null) {
                    onIncompletePath.consume(downStack, done);
                }
                break;
            }
        }
        return (T) o;
    }



    public String toString() {
        return toString("\r\n");
    }

    public String toString(String linePrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(linePrefix);
        sb.append(ShortName());
        if (alias!=null) {
            sb.append("(" + alias + ")");
        }
        String visitedAsString = VisitedAsString(linePrefix + " ");
        if (visitedAsString.trim().length()>0) sb.append("(" +  visitedAsString + linePrefix + ")");
        return sb.toString();
    }

    public interface Move {
        //TODO implement veto !
        //TODO listIndex->propertyIndex, add isListItem
        void move(boolean intoOutOf, TypedPath path, Object source, boolean isComplex, boolean isList, boolean isListItem, Integer propertyIndex);
    }

    public void followSelectedPathFromTop(Move onMove, Object source) {
        this._top().followSelectedPath(onMove, source, true, null, false );
    }

    public void followSelectedPathFromTop(Move onMove, Object source, PathDown down, PathAsList asList) {
        this._top().followSelectedPath(onMove, source, true, null, false, down, asList );
    }


    public void followSelectedPathFromTopMetaOnly(Move onMove) {
        this._top().followSelectedPath(onMove, null, true, null, true );
    }


    public List<T> getNodesAssumeSinglePath(S parent) {
        List<T> results = new ArrayList<>();
        Move onMove = (intoOutOf, path,  source, isComplex, isList,  isListItem,  propertyIndex) ->
        {
           if (intoOutOf && !isComplex) {
               results.add((T)source);
           }
        };
        _top().followSelectedPath(onMove, parent, true, 0, false);
        return results;
    }

    public void followSelectedPath(Move onMove, Object source, boolean isListItem, Integer propertyIndex, boolean metaPath) {
        PathDown down = (downPath, parentIn) -> downPath.get.apply(parentIn);
        PathAsList asList = (o)->(List)o;
        followSelectedPath(onMove, source, isListItem, propertyIndex, metaPath,  down, asList);
    }

    public void followSelectedPath(Move onMove, Object source, boolean isListItem, Integer propertyIndex, boolean metaPath, PathDown down, PathAsList asList) {
        if (!skipOnTraverse) onMove.move(true, this, source, this.isComplex(), this.isList(), isListItem, propertyIndex);
        if (this.isList()) {
            //TODO do this list assignabiity test in a non java specific way - this blows up for java objects
            //if (!metaPath && !List.class.isAssignableFrom(source.getClass()))
            //    throw new RuntimeException(String.format("item at path %s is not a list its a %s ", this.ShortName(), this.getClass().getName()));
            int index = 0;
            Predicate<Object> filter = ((TypedListPath) this).filter;
            TypedPath itemPath = (TypedPath) ((TypedListPath) this).touched.get(0);
            if (metaPath) {
                itemPath.followSelectedPath(onMove, null, true, index++, metaPath);
            } else {
                for (Object item : asList.asList(source)) {
                    if (filter.test(item)) {
                        itemPath.followSelectedPath(onMove, item, true, index++, metaPath, down, asList);
                    }
                }
            }

            if (!this.skipOnTraverse) onMove.move(false, this, source, true, true, isListItem, propertyIndex);
            return;
        }

        int subpropertyIndex = 0;
        for (;  subpropertyIndex < this.touched.size(); subpropertyIndex++) {
            TypedPath subpath = this.touched.get(subpropertyIndex);
            if (metaPath) {
                subpath.followSelectedPath(onMove, null, false, subpropertyIndex, metaPath);
            } else {
                Object subobject = null;
                try {
                    subobject =  down.down(subpath, source);
                } catch (Exception ex) {
                    throw new RuntimeException("failed to traverse " + source + "." + subpath.ShortName(), ex);
                }
                if (subobject != null) {
                    subpath.followSelectedPath(onMove, subobject, false, subpropertyIndex, metaPath, down, asList);
                }
            }
        }
        //union processing
        for (Map.Entry<String, List<TypedPath>> entry : unionPathSets.entrySet()) {
            TypedPath path = new TypedPath(this, entry.getKey(), null, List.class);
            //synthesise property
            onMove.move(true, path, source, true, true, false, subpropertyIndex);
            for (int index = 0; index<entry.getValue().size(); index++ ) {
                TypedPath itemPath = entry.getValue().get(index);
                //union paths are relative to current object
                //itemPath.followSelectedPathFromTop(onMove, source);
                itemPath._top() .followSelectedPath(onMove, source, true, index, metaPath, down, asList);
            }
            onMove.move(false, path, source, true, true, false, subpropertyIndex);
        }
        //TODO make metaPath exploration work with groupBy
        if (!metaPath) {
            //group by processing
            for (Map.Entry<String, GroupBy> entry : groupBys.entrySet()) {
                TypedPath path = new TypedPath(this, entry.getKey(), null, List.class);
                //synthesise property
                onMove.move(true, path, source, true, true, false, subpropertyIndex);
                TypedPath groupKeyGetter = entry.getValue().groupKeyGetter;
                //TODO provide navigator in case of json navigation
                final List lGroupKeys = groupKeyGetter.getNodesAssumeSinglePath(source);
                final Set groupKeys = new LinkedHashSet(lGroupKeys);
                String keyPropertyName = groupKeyGetter.ShortName();
                if (groupKeyGetter.alias != null) {
                    keyPropertyName = groupKeyGetter.alias;
                }
                TypedPath keyPath = new TypedPath(null, keyPropertyName, null, null);
                TypedPath groupPath = new TypedPath(null, "", null, null);
                int groupIndex = 0;
                for (Object key : groupKeys) {
                    onMove.move(true, groupPath, key, true, false, true, groupIndex);
                    //explore the key
                    onMove.move(true, keyPath, key, false, false, false, 0);
                    //explore the value
                    onMove.move(false, keyPath, key, false, false, false, 0);
                    TypedPath pathForKey = entry.getValue().groupPathGetter.apply((Object) key);
                    pathForKey.followSelectedPath(onMove, source, false, 1, metaPath, down, asList);
                    onMove.move(false, groupPath, key, true, false, true, groupIndex++);

                }

                onMove.move(false, path, source, true, true, false, subpropertyIndex);

            }
        }
        if (!skipOnTraverse) onMove.move(false, this, source, this.isComplex(), this.isList(),  isListItem, propertyIndex);
    }



    public <C extends TypedPath> TypedPath<S, T> selectCast(C target, Consumer<C>... ls) {
        //TODO check if the cast is valid here!
        for (Consumer<C> l :  ls ) {
            l.accept(target);
        }
        for (int tdone=0; tdone<target.getTouched().size();
             this.touch((TypedPath) (target.getTouched().get(tdone))),
                     tdone++) { }
        return this;
    }

    public static <X extends TypedPath, Y extends TypedPath> void link(X from, Y to, BiConsumer<X, Y> ...linkers ) {
        // TODO clone from, to here ?
        // or just work off intos ?
        from.link=to;
           for (BiConsumer<X, Y> linker : linkers ) {
               linker.accept(from, to);
           }
    }

    public static <Z> void into(TypedPath<?, Z> from, TypedPath<?, Z> to) {
         from.into=to;
    }

    //link to other paths
    protected TypedPath link =  null;

    //TODO rename this to Link as per convention
    public void setLink(TypedPath link) {
        this.link=link;
    }

    //TODO rename this to Link as per convention
    public TypedPath getLink() {
        return link;
    }

    //copy into links
    protected TypedPath into = null;
    public TypedPath getInto() {
        return into;
    }
    public void setInto(TypedPath into) {
        this.into=into;
    }


}
