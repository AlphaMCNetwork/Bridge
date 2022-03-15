package rip.alpha.bridge;

import lombok.Getter;
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

    public void registerListener(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.getParameterCount() <= 0){
                continue;
            }

            if (method.getReturnType() != void.class){
                continue;
            }

            if (!Modifier.isStatic(method.getModifiers())){
                continue;
            }

            BridgeListener bridgeListener = method.getDeclaredAnnotation(BridgeListener.class);

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

    public void sendMessage(BridgeMessage bridgeMessage){
        try (Jedis client = jedisPool.getResource()) {
            client.publish(channel, bridgeMessage.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        this.pubSubThread.stop();
    }
}
