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
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;

@Getter
public class Bridge {

    private final String channel;
    private final RedissonClient redissonClient;
    private final RTopic redissonTopic;

    private boolean closed = false;

    /**
     * Constructor to initialize bridge
     *
     * @param channel the channel to listen for.
     * @param config  the reddison config to subscribe & send messages with.
     */
    public Bridge(String channel, Config config) {
        this.channel = channel;
        config.setCodec(new BridgeCodec());
        this.redissonClient = Redisson.create(config);
        this.redissonTopic = this.redissonClient.getTopic(this.channel);
    }

    /**
     * Constructor to initialize bridge
     *
     * @param channel the channel to listen for.
     */
    public Bridge(String channel) {
        this.channel = channel;
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        config.setCodec(new BridgeCodec());
        this.redissonClient = Redisson.create(config);
        this.redissonTopic = this.redissonClient.getTopic(this.channel);
    }

    /**
     * A function to register all the bridge listeners in a class
     *
     * @param clazz the class to scan for bridge listeners & register.
     */
    public <M extends BridgeEvent> void registerListener(Class<M> clazz, MessageListener<? extends M> listener) {
        this.redissonTopic.addListener(clazz, listener);
    }

    /**
     * A function to send a message through the jedis channel for bridge
     *
     * @param event the bridge message to send
     */
    public <M extends BridgeEvent> void callEvent(M event) {
        if (this.closed) {
            throw new IllegalStateException("Attempted to call an event while Bridge was closed.");
        }
        this.redissonTopic.publish(event);
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
