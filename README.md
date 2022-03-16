# Bridge

A simple redis pub sub implementation that allows you to send and handle messages in a very convenient manner

## How to use

### <ins>Creating an instance</ins>

```java
        // Create a jedis pool instance to subscribe with
        JedisPool jedisPool = new JedisPool();
        Bridge bridge = new Bridge("channel", jedisPool, "password");
```

### <ins>Registering Listeners</ins>

```java
public class IncomingMessageListener {
    @BridgeListener("messageId")
    public static void handleIncomingMessage(Document document){
        // TODO handle incoming
    }
}
```

```java
        // Register the incoming channel using the bridge instance
        this.bridge.registerListener(IncomingMessageListener.class);
```

### <ins>Sending a message</ins>

```java
        Document document = new Document();
        document.put("data", "test");
        BridgeMessage bridgeMessage = new BridgeMessage("messageId", document);
        this.bridge.sendMessage(bridgeMessage);
```
