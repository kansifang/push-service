package common.pushservice;

import java.io.IOException;

public interface PushService {
	void setup();
	boolean allowed(String app, String ip);
	void registerApp(String app, String ips) throws ApplicationExistException;
	void deleteApp(String app);
	void addAppleUser(String app, String userId, String appleToken);
	void addGoogleUser(String app, String userId, String googleToken);
	void push(String app, String userId, PushMessage msg) throws IOException;
	void pushToApple(String app, String token, PushMessage msg);
	void pushToGoogle(String app, String token, PushMessage msg) throws IOException;
}
