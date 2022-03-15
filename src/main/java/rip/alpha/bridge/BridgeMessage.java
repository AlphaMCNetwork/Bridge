package rip.alpha.bridge;

import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class BridgeMessage {

    private final String messageId;
    private final Document document;

    @Override
    public String toString(){
        return this.messageId + ";" + document.toJson();
    }

}
