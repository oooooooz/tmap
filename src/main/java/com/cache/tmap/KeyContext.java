package com.cache.tmap;

/**
 * User: chenhf
 * Date: 2016/5/31
 * Time: 19:35
 */
public class KeyContext<K,V> {


    private K key;
    private long deadline;
    private V value;

    public KeyContext(K key,long duration,long start,V value){
        this.key = key;
        this.deadline = start + duration;
        this.value = value;
    }


    public V getValue(){
        return this.value;
    }

    public long getDeadline(){
        return this.deadline;
    }

    public K getKey(){
        return this.key;
    }

    public String toString(){
        return "{key=" + key.toString() + ",value=" + value + ",deadline=" + deadline + "}";
    }

}
