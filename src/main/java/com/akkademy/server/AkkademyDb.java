package com.akkademy.server;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.akkademy.exceptions.InvalidValueException;
import com.akkademy.exceptions.KeyNotFoundException;
import com.akkademy.exceptions.UnknowMessageException;
import com.akkademy.messages.GetRequest;
import com.akkademy.messages.RemoveRequest;
import com.akkademy.messages.SetIfNotExistsRequest;
import com.akkademy.messages.SetRequest;

import java.util.HashMap;
import java.util.Map;

public class AkkademyDb extends AbstractActor {

    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);

    protected final Map<String, Object> map = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetRequest.class, message -> {

                    log.info("Received set request – {}", message);

                    if(message.value == null){
                        sender().tell(new Status.Failure(new InvalidValueException(message.key)), self());
                    }else{
                        map.put(message.key, message.value);
                        sender().tell(new Status.Success(message.key), self());
                    }
                })
                .match(SetIfNotExistsRequest.class, message -> {

                    log.info("Received set if not exists request – {}", message);

                    if(message.value == null) {
                        sender().tell(new Status.Failure(new InvalidValueException(message.key)), self());
                    }else {
                        map.putIfAbsent(message.key, message.value) ;

                        sender().tell(new Status.Success(message.key), self());
                    }
                })
                .match(RemoveRequest.class, message -> {

                    log.info("Received remove request – {}", message);

                    map.remove(message.key);

                    sender().tell(new Status.Success(message.key), self());
                })
                .match(GetRequest.class, message -> {

                    log.info("Received get request – {}", message);

                    Object value = this.map.get(message.key);

                    if(value == null){
                        sender().tell(new Status.Failure(new KeyNotFoundException(message.key)), self());
                    }else{
                        sender().tell(value, self());
                    }
                })
                .matchAny(message -> {
                    log.info("Received unknown message – {}", message);

                    sender().tell(new Status.Failure(new UnknowMessageException(message)), self());
                })
                .build();
    }
}