package dev.greatseo.api.event;

import java.time.LocalDateTime;

public class Event<K, T> {

    public enum Type {CREATE, DELETE}

    private Event.Type eventType;
    private K key;
    private T value;
    private LocalDateTime eventCreatedTime;

    public Event(){
        this.eventType = null;
        this.key = null;
        this.value = null;
        this.eventCreatedTime = null;
    }

    public Event(Event.Type eventType, K key, T value){
        this.eventType = eventType;
        this.key = key;
        this.value = value;
        this.eventCreatedTime = LocalDateTime.now();
    }

    public Type getEventType(){
        return eventType;
    }
    public K getKey(){
        return key;
    }
    public T getValue(){
        return value;
    }
    public LocalDateTime getEventCreatedTime(){
        return eventCreatedTime;
    }
}
