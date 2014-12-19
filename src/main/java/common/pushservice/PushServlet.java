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
		String app = req.getParameter("app");
		pushService.addAppleUser(app, userId, appleToken);
	}
	
	void addGoogleUser(HttpServletRequest req, HttpServletResponse resp) {
		String userId = req.getParameter("userId");
		String googleToken = req.getParameter("googleToken");
		String app = req.getParameter("app");
		pushService.addGoogleUser(app, userId, googleToken);
	}
	
	void pushToApple(HttpServletRequest req, HttpServletResponse resp) {
		String userId = req.getParameter("userId");
		String token = req.getParameter("token");
		String app = req.getParameter("app");
		if(userId != null && userId.trim().length()>0) {
			pushService.addAppleUser(app, userId, token);
		}
		String alert = req.getParameter("alert");
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		pushService.pushToApple(app, token, msg);
	}
	
	void pushToGoogle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userId = req.getParameter("userId");
		String token = req.getParameter("token");
		String app = req.getParameter("app");
		if(userId != null && userId.trim().length()>0) {
			pushService.addGoogleUser(app, userId, token);
		}
		String alert = req.getParameter("alert");
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		pushService.pushToGoogle(app, token, msg);
	}
	
	void pushToUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userId = req.getParameter("userId");
		String alert = req.getParameter("alert");
		String app = req.getParameter("app");
		PushMessage msg = new PushMessage();
		msg.alert = alert;
		pushService.push(app, userId, msg);
	}
	

}
