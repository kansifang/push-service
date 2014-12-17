package common.pushservice;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp2.BasicDataSource;

public class PushServlet extends HttpServlet {
	private PushServiceImpl pushService;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		pushService = new PushServiceImpl();
		
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(config.getInitParameter("dbClassName"));
		ds.setUsername(config.getInitParameter("userName"));
		ds.setPassword(config.getInitParameter("password"));
		ds.setUrl(config.getInitParameter("url"));
		pushService.setDataSource(ds);
		
		String apiKey = config.getInitParameter("googlePushNotificationApiKey");
		GooglePushService googlePushService = new GooglePushService(apiKey );
		pushService.set(googlePushService);
		
		String p12FilePath = config.getInitParameter("applePushNotificationP12FilePath");
		String password = config.getInitParameter("applePushNotificationPassword");
		boolean sandbox = "true".equalsIgnoreCase(config.getInitParameter("applePushNotificationSandbox"));
		try {
			ApplePushService applePushService = new ApplePushService(p12FilePath, password, sandbox);
			pushService.set(applePushService);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		pushService.setup();
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.service(req, resp);
	}
}
