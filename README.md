# Bridge

A simple redis pub sub implementation that allows you to send and handle messages in a very convenient manner

## How to use

### <ins>Creating an instance</ins>

```java
        // Create a redisson config instance to initialize bridge with
        Config redissonConfig = new Config();
        redissonConfig.useSingleServer().setAddress("redis://127.0.0.1:6379");
        Bridge bridge = new Bridge("channel", redissonConfig);
```

### <ins>Making events and listeners for events</ins>

```java
@NoArgsConstructor
@AllArgsConstructor
public class BridgeTestEvent implements BridgeEvent {

    public int numberId;
    public String message;

    @Override
    public String toString(){
        return numberId + ";" + message;
    }
}
```

```java
        // Register the incoming channel using the bridge instance
        this.bridge.registerListener(BridgeTestEvent.class, (charSequence, event) -> {
            System.out.println(event);
        });
```

### <ins>Calling an event</ins>

```java
         this.bridge.callEvent(new BridgeTestEvent(0, "helloWorld"));
```
