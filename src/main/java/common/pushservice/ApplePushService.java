package common.pushservice;

import java.io.FileNotFoundException;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.exceptions.InvalidSSLConfig;

public class ApplePushService {
	private ApnsService service = null;
	
	public ApplePushService(String p12FilePath, String password, boolean sandbox)
			throws InvalidSSLConfig, FileNotFoundException {
		ApnsServiceBuilder builder = APNS.newService()
			    .withCert(p12FilePath, password);
		if(sandbox)
		    builder = builder.withSandboxDestination();
			   
	    service = builder.build();
	}
	
	public void push(String token, PushMessage msg) {
		String payload = APNS.newPayload().badge(msg.badge).alertBody(msg.alert).build();
		service.push(token, payload );
	}
}
