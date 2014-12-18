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
		
		try {
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(getSettings("dbClassName"));
			ds.setUsername(getSettings("userName"));
			ds.setPassword(getSettings("password"));
			ds.setUrl(getSettings("url"));
			pushService.setDataSource(ds);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		String apiKey = getSettings("googlePushNotificationApiKey");
		GooglePushService googlePushService = new GooglePushService(apiKey );
		pushService.set(googlePushService);
		
		String p12FilePath = getSettings("applePushNotificationP12FilePath");
		String password = getSettings("applePushNotificationPassword");
		boolean sandbox = "true".equalsIgnoreCase(getSettings("applePushNotificationSandbox"));
		try {
			ApplePushService applePushService = new ApplePushService(p12FilePath, password, sandbox);
			pushService.set(applePushService);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		pushService.setup();
	}
	
	private String getSettings(String key) {
		String value = getServletContext().getInitParameter(key);
		if(value == null || value.trim().length()==0)
			value = System.getenv(key);
		System.out.println(key + " = " + value);
		return value;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uri = req.getRequestURI();
		System.out.println("received request " + uri);
		if(uri.indexOf("add-apple-user")!=-1)
			addAppleUser(req, resp);
		else if(uri.indexOf("add-google-user")!=-1)
			addAppleUser(req, resp);
		else if(uri.indexOf("push-to-apple")!=-1)
			pushToApple(req, resp);
		else if(uri.indexOf("push-to-google")!=-1)
			pushToGoogle(req, resp);
		else if(uri.indexOf("push-to-user")!=-1)
			pushToUser(req, resp);
		resp.getWriter().write("done");
	}

	void addAppleUser(HttpServletRequest req, HttpServletResponse resp) {
		String userId = req.getParameter("userId");
		String appleToken = req.getParameter("appleToken");
		pushService.addAppleUser(userId, appleToken);
	}
	
	void addGoogleUser(HttpServletRequest req, HttpServletResponse resp) {
		String userId = req.getParameter("userId");
		String googleToken = req.getParameter("googleToken");
		pushService.addGoogleUser(userId, googleToken);
	}
	
	void pushToApple(HttpServletRequest req, HttpServletResponse resp) {
		String userId = req.getParameter("userId");
		String token = req.getParameter("token");
		if(userId != null && userId.trim().length()>0) {
			pushService.addAppleUser(userId, token);
		}
		String badge = req.getParameter("badge");
		String alert = req.getParameter("alert");
		int badgeNumber = 0;
		try {
			badgeNumber = Integer.parseInt(badge);
		} catch (Exception e) {
		}
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		msg.badge = badgeNumber;
		pushService.pushToApple(token, msg);
	}
	
	void pushToGoogle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userId = req.getParameter("userId");
		String token = req.getParameter("token");
		if(userId != null && userId.trim().length()>0) {
			pushService.addGoogleUser(userId, token);
		}
		String badge = req.getParameter("badge");
		String alert = req.getParameter("alert");
		int badgeNumber = 0;
		try {
			badgeNumber = Integer.parseInt(badge);
		} catch (Exception e) {
		}
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		msg.badge = badgeNumber;
		pushService.pushToGoogle(token, msg);
	}
	
	void pushToUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userId = req.getParameter("userId");
		String badge = req.getParameter("badge");
		String alert = req.getParameter("alert");
		int badgeNumber = 0;
		try {
			badgeNumber = Integer.parseInt(badge);
		} catch (Exception e) {
		}
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		msg.badge = badgeNumber;
		pushService.push(userId, msg);
	}
	

}
