package com.cache.tmap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Set;

/**
 * User: chenhf
 * Date: 2016/5/31
 * Time: 15:41
 */
public abstract class AbstractTimeoutMap<B extends AbstractTimeoutMap<B,M> ,M extends AbstractMap> {

    volatile AbstractTimeoutMap next;

    volatile AbstractTimeoutMap pre;

    private volatile MapFactory<M> mapFactory;

    private M m ;



    private B timeoutMap(Class<M> mapClass){

        if(mapClass == null){
            throw new NullPointerException("mapClass");
        }

         return mapFactory(new DefaultTimeoutMapFactory<M>(mapClass));


    }

    final MapFactory<M> mapFactory() {
        return mapFactory;
    }

    protected final M newTimeoutMap(Class<M> mapClazz){
        return this.timeoutMap(mapClazz).mapFactory().newTimeoutMap();
    }


    private B mapFactory(MapFactory<M> mapFactory){

        if(mapFactory == null){
            throw new NullPointerException("mapFactory");
        }

        if(this.mapFactory != null){
            throw new IllegalStateException("mapFactory set already");
        }

        this.mapFactory = mapFactory;

        return (B) this;
    }



    private final class DefaultTimeoutMapFactory<T extends AbstractMap> implements MapFactory<T>{

        private final Class<? extends T> clazz;

        DefaultTimeoutMapFactory(Class<? extends T> clazz){
            this.clazz = clazz;
        }

        @Override
        public T newTimeoutMap() {
            try{
                T t = clazz.newInstance();
                AbstractTimeoutMap.this.m = (M) t;
                return t;
            }catch (Throwable t){
                throw new RuntimeException("Unable to create timeoutMap from class " + clazz,t);
            }
        }


        public String toString(){
            return ObjectUtil.simpleClassName(clazz) + ".class";
        }

    }

    protected abstract String getName();

    public boolean isEmpty(){
        return m.isEmpty();
    }

    public Set<M.Entry> entrySet(){
        return m.entrySet();
    }

    public boolean containsKey(Object key){
        return m.containsKey(key);
    }

    public boolean containsValue(Object value){
        return m.containsValue(value);
    }

    public Object remove(Object key){
        return m.remove(key);
    }

    public void clear(){
        m.clear();
    }

    public Set keySet(){
        return m.keySet();
    }

    public Collection values(){
        return m.values();
    }

    protected  abstract  void expire(Object key);


}
