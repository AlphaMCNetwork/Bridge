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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
public class Bridge {

    private static final Gson defaultGson = new GsonBuilder().disableHtmlEscaping().create();

    @Setter
    private static Supplier<Gson> gsonSupplier = () -> defaultGson;

    private final String channel;
    private final RedissonClient redissonClient;
    private final String mainTopicName;
    private final Map<String, RTopic> topicMap = new ConcurrentHashMap<>();

    private boolean closed = false;

    /**
     * Constructor to initialize bridge
     *
     * @param channel the channel you would like to register the topic under
     * @param client  the client you would like to fetch the topic from
     */
    public Bridge(String channel, RedissonClient client) {
        this.channel = channel;
        this.redissonClient = client;
        this.mainTopicName = channel;
    }

    /**
     * A function to register all the bridge listeners in a class
     *
     * @param clazz the class to scan for bridge listeners & register.
     */
    public <M extends BridgeEvent> void registerListener(Class<M> clazz, MessageListener<? extends M> listener) {
        this.registerListener(this.mainTopicName, clazz, listener);
    }

    public <M extends BridgeEvent> void registerListener(String topicName, Class<M> clazz, MessageListener<? extends M> listener) {
        this.topicMap.computeIfAbsent(topicName, this.redissonClient::getTopic).addListener(clazz, listener);
    }

    /**
     * A function to send a message through the jedis channel for bridge
     *
     * @param event the bridge message to send
     */
    public <M extends BridgeEvent> void callEvent(M event) {
        this.callEvent(this.mainTopicName, event);
    }

    public <M extends BridgeEvent> void callEvent(String topicName, M event) {
        if (this.closed) {
            throw new IllegalStateException("Attempted to call an event while Bridge was closed.");
        }
        this.topicMap.computeIfAbsent(topicName, this.redissonClient::getTopic).publish(event);
    }

    /**
     * A function to check if Bridge has been closed
     *
     * @return a boolean if bridge has been closed.
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * A function to close the jedis pubsub thread
     */
    public void close() {
        if (this.closed) {
            throw new IllegalStateException("Bridge is already closed");
        }

        this.closed = true;
        this.redissonClient.shutdown();
    }
}
