package com.cache.tmap;


import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: chenhf
 * Date: 2016/5/31
 * Time: 17:50
 */
public class TimeoutMapContext{


    private final AtomicLong ai = new AtomicLong(0);

    private static TimeoutMapContext context = null;

    private final static Map<String,Boolean> names = new ConcurrentHashMap<String, Boolean>(4);

    synchronized static TimeoutMapContext contextInstance(){

        if(context != null){
            return context;
        }

        context = new TimeoutMapContext();

        return context;

    }


    private volatile AbstractTimeoutMap head;

    private volatile AbstractTimeoutMap tail;

    //a closed loop
    private TimeoutMapContext(){
        head = new DefaultContext();
        tail = new DefaultContext();
        head.next = tail;
        tail.pre = head;
        tail.next = head;
    }


     final synchronized AbstractTimeoutMap  getHead(){
         synchronized (this) {
             return this.head;
         }
    }


    //remove empty map
    final void remove(AbstractTimeoutMap emptyMap){

        if(emptyMap == null || emptyMap == this.head || emptyMap == this.tail){
            return;
        }

        synchronized (this){
            AbstractTimeoutMap next = emptyMap.next;
            if(emptyMap.pre != null){
                emptyMap.pre.next = next;
            }
            if(emptyMap.next != null){
                emptyMap.next.pre = emptyMap.pre;
            }

            names.remove(emptyMap.getName());

        }

    }


    final synchronized Map<String,Boolean> getNames(){
        return names;
    }

     final  AbstractTimeoutMap getTail(){
         synchronized (this) {
             return this.tail;
         }
     }


     String addLast(AbstractTimeoutMap newMap){

         String name = null;

        synchronized (this) {

            name = generateName(newMap.getClass());

            if(names.containsKey(name)){
                throw new IllegalStateException("Duplicate map name :" + name);
            }

            AbstractTimeoutMap pre = tail.pre;
            newMap.next = tail;
            newMap.pre = pre;
            pre.next = newMap;
            tail.pre = newMap;

            names.put(name,true);
        }

        return name;
    }



    private String generateName(Class<?> clazz){
        String name = generateName0(clazz);
        name += ai.getAndIncrement();//get by 0
        return name;
    }

    private String generateName0(Class<?> mapType){
        return ObjectUtil.simpleClassName(mapType) + "#";
    }

    long currentTime(){
        return System.nanoTime();
    }


    private static final class DefaultContext extends AbstractTimeoutMap{
        @Override
        protected String getName() {
            return null;
        }

        @Override
        protected void expire(Object key) {
            //
        }

        public Set<AbstractMap.Entry> entrySet(){
            return null;
        }

        public boolean isEmpty(){
            return true;
        }

    }


}
