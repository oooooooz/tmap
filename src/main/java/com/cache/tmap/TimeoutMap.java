package com.cache.tmap;

import java.util.AbstractMap;
import java.util.concurrent.TimeUnit;

/**
 * User: chenhf
 * Date: 2016/5/31
 * Time: 17:03
 */
public class TimeoutMap<T extends AbstractMap> extends AbstractTimeoutMap<TimeoutMap<T>,T> {

    private long timeout;
    private T map;
    private TimeUnit timeUnit = TimeUnit.NANOSECONDS; //default mills
    private String name;


    public TimeoutMap(long timeout,Class<T> mapClazz,TimeUnit timeUnit){
        this.validate(timeout);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.map = this.newTimeoutMap(mapClazz);
        name = TimeoutMapContext.contextInstance().addLast(this);
    }

    private void validate(long timeout){
        if(timeout <= 0 || timeout >= Long.MAX_VALUE){
            throw new IllegalArgumentException("timeout illegal : " + timeout);
        }
    }


    public TimeoutMap(long timeout,Class<T> mapClazz){
        this.validate(timeout);
        this.timeout = timeout;
        this.map = this.newTimeoutMap(mapClazz);
        name = TimeoutMapContext.contextInstance().addLast(this);
    }


    /**
     * put to map
     * @param key {@code key}
     * @param value {@code value}
     */
    public void put(Object key ,Object value){

        if(key == null){
            throw new  UnsupportedOperationException("null key is not permit");
        }

        TimeoutMapContext context = TimeoutMapContext.contextInstance();

       synchronized (this){
           KeyContext<Object,Object> keyContext =
                   new KeyContext<Object,Object>(key,timeUnit.toNanos(timeout),
                           context.currentTime(),value);

           this.map.put(key,keyContext);

       }

        //may be had been removed
        if(!context.getNames().containsKey(this.name)){
            //add to Last
            context.addLast(this);
        }
    }


    public Object get(Object key){
        if(!map.containsKey(key)){
            return null;
        }
        return ((KeyContext) map.get(key)).getValue();
    }


    @Override
    protected synchronized void expire(Object key) {
        this.map.remove(key);
    }


    public String toString(){
        return "TimeoutMap[" + this.name + "]";
    }

    public String getName(){
        return this.name;
    }



}
