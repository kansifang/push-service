package common.pushservice;

import java.io.IOException;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public class GooglePushService {
	private String apiKey;
	
	public GooglePushService(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public boolean push(String token, PushMessage msg) throws IOException {
		Sender sender = new Sender(apiKey);
		Message message = new Message.Builder()
		    .addData("message", msg.alert)
		    .addData("other-parameter", "some value")
		    .build();
		Result result = sender.send(message, token, 1);
		return result.getMessageId() != null;
	}
}
