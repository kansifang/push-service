package common.pushservice;

import java.io.IOException;

public interface PushService {
	void setup();
	void addAppleUser(String userId, String appleToken);
	void addGoogleUser(String userId, String googleToken);
	void push(String userId, PushMessage msg) throws IOException;
	void pushToApple(String token, PushMessage msg);
	void pushToGoogle(String token, PushMessage msg) throws IOException;
}
