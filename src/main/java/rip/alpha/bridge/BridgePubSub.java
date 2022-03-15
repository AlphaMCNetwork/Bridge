package rip.alpha.bridge;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import redis.clients.jedis.JedisPubSub;

import java.lang.invoke.MethodHandle;
import java.util.Set;

@RequiredArgsConstructor
public class BridgePubSub extends JedisPubSub {

    private final Bridge instance;

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(this.instance.getChannel())) {
            try {
                String[] split = message.split(";");

                if (split.length <= 1){
                    return;
                }

                String messageId = split[0];
                Set<MethodHandle> methods = this.instance.getListeners().get(messageId);

                if (methods == null){
                    return;
                }

                String json = split[1];
                Document document = Document.parse(json);

                if (document == null){
                    return;
                }

                for (MethodHandle method : methods) {
                    method.invoke(document);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
