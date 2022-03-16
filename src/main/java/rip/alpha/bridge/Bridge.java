// This file was designed and is an original Plugin for AlphaMC
// Copyright (C) 2021 Foxtrot LLC
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package rip.alpha.bridge;

import lombok.Getter;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class Bridge {

    private final String channel;
    private final JedisPool jedisPool;
    private final Map<String, Set<MethodHandle>> listeners;
    private final MethodHandles.Lookup methodLookup;
    private final Thread pubSubThread;

    private boolean closed = false;

    /**
     * Constructor to initialize bridge
     * @param channel the channel to listen for
     * @param jedisPool the jedis pool to subscribe & send messages with.
     */
    public Bridge(String channel, JedisPool jedisPool){
        this.channel = channel;
        this.jedisPool = jedisPool;
        this.listeners = new HashMap<>();
        this.methodLookup = MethodHandles.lookup();

        this.pubSubThread = new Thread(() -> {
            try (Jedis client = jedisPool.getResource()) {
                client.subscribe(new BridgePubSub(this), channel);
            }
        });

        this.pubSubThread.start();
    }

    /**
     * A function to register all the bridge listeners in a class
     * @param clazz the class to scan for bridge listeners & register.
     */
    public void registerListener(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            //set the method accessible for private methods
            method.setAccessible(true);

            //Check if the methods return type is void
            if (method.getReturnType() != void.class){
                continue;
            }

            //Check if the method is static
            if (!Modifier.isStatic(method.getModifiers())){
                continue;
            }


            //Check if the method has a parameter
            if (method.getParameterCount() <= 0){
                continue;
            }

            //Check if the parameter is a document
            if (method.getParameterTypes()[0] != Document.class){
                continue;
            }

            BridgeListener bridgeListener = method.getDeclaredAnnotation(BridgeListener.class);

            //Check if the annotation is there
            if (bridgeListener == null){
                continue;
            }

            String messageId = bridgeListener.value();
            Set<MethodHandle> methods = listeners.computeIfAbsent(messageId, key -> new HashSet<>());

            try {
                methods.add(this.methodLookup.unreflect(method));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A function to send a message through the jedis channel for bridge
     * @param bridgeMessage the bridge message to send
     */
    public void sendMessage(BridgeMessage bridgeMessage){
        if (this.closed){
            throw new IllegalStateException("Attempted to send a message while Bridge was closed.");
        }

        try (Jedis client = jedisPool.getResource()) {
            client.publish(channel, bridgeMessage.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A function to check if Bridge has been closed
     * @return a boolean if bridge has been closed.
     */
    public boolean isClosed(){
        return this.closed;
    }

    /**
     * A function to close the jedis pubsub thread
     */
    public void close(){
        if (this.closed){
            throw new IllegalStateException("Bridge is already closed");
        }
        this.pubSubThread.stop();
        this.closed = true;
    }
}
