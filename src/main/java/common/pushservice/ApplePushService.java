package common.pushservice;

import java.io.FileNotFoundException;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
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
		else
			builder = builder.withProductionDestination();
			   
	    service = builder.build();
	}
	
	public ApnsNotification push(String token, PushMessage msg) {
		String payload = APNS.newPayload().badge(msg.badge).alertBody(msg.alert).build();
		ApnsNotification noti = service.push(token, payload );
		System.out.println("apns: " + noti.getIdentifier() + ", " + new String(noti.getDeviceToken()) + ", " + new String(noti.getPayload()) + ", " + token);
		return noti;
	}
}
