package common.pushservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.notnoop.exceptions.InvalidSSLConfig;

public class PushServiceImpl implements PushService {
	private DataSource dataSource;
	private Map<String, ApplePushService> applePushService = new HashMap<String, ApplePushService>();
	private Map<String, GooglePushService> googlePushService = new HashMap<String, GooglePushService>();
	
	private void addUser(String app, String userId, TokenType type, String token) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		
		String sql = "delete from user_token where user_id=? and type=? and app=?";
		jt.update(sql, userId, type.name(), app);
		
		sql = "insert into user_token(app, user_id, type, token) values (?, ?, ?, ?)";
		jt.update(sql, app, userId, TokenType.Apple.name(), token);
	}
	
	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
	}
	
	public void setup() {
		String sql = "create table if not exists user_token (app varchar(64), user_id varchar(64), type varchar(32), token varchar(1023), badge integer, primary key (app, user_id, type))";
		new JdbcTemplate(dataSource).update(sql);
		
		sql = "create table if not exists configuration (name varchar(128), value varchar(1024), primary key (name))";
		new JdbcTemplate(dataSource).update(sql);
		
		sql = "create table if not exists application (name varchar(128), ip varchar(1024) not null, primary key (name))";
		new JdbcTemplate(dataSource).update(sql);
	}

	public void addAppleUser(String app, String userId, String appleToken) {
		addUser(app, userId, TokenType.Apple, appleToken);
	}

	public void addGoogleUser(String app, String userId, String googleToken) {
		addUser(app, userId, TokenType.Google, googleToken);
	}

	public void push(String app, String userId, PushMessage msg) throws IOException {
		String sql = "select * from user_token where user_id=? and app=?";
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		List<UserToken> tokens = jt.query(sql, new Object[]{userId, app}, new RowMapper<UserToken>(){
			public UserToken mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				UserToken ut = new UserToken();
				ut.userId = rs.getString("user_id");
				ut.type = TokenType.valueOf(rs.getString("type"));
				ut.token = rs.getString("token");
				return ut;
			}});
		for(UserToken ut : tokens) {
			if(TokenType.Apple.equals(ut.type)) {
				getApplePushSerivceForApp(app).push(ut.token, msg);
			}
			else if(TokenType.Google.equals(ut.type)) {
				getGooglePushServiceForApp(app).push(ut.token, msg);
			}
		}
	}

	public void pushToApple(String app, String token, PushMessage msg) {
		try {
			getApplePushSerivceForApp(app).push(token, msg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void pushToGoogle(String app, String token, PushMessage msg) throws IOException {
		getGooglePushServiceForApp(app).push(token, msg);
	}

	private ApplePushService getApplePushSerivceForApp(String app) throws InvalidSSLConfig, FileNotFoundException {
		ApplePushService service = applePushService.get(app);
		if(service == null) {
			String p12FilePath = getSettings(app, "applePushNotificationP12FilePath");
			String password = getSettings(app, "applePushNotificationPassword");
			boolean sandbox = "true".equalsIgnoreCase(getSettings(app, "applePushNotificationSandbox"));
			service = new ApplePushService(p12FilePath, password, sandbox);
			applePushService.put(app, service);
		}
		return service;
	}
	
	private String getSettings(String app, String key) {
		String sql = "select value from configuration where name = ?";
		return new JdbcTemplate(dataSource).queryForObject(sql, new Object[]{app+"."+key}, String.class);
	}
	
	private GooglePushService getGooglePushServiceForApp(String app) {
		GooglePushService service = googlePushService.get(app);
		if(service == null) {
			String apiKey = getSettings(app, "googlePushNotificationApiKey");
			service = new GooglePushService(apiKey );
			googlePushService.put(app, service);
		}
		return service;
	}

	public void deleteApp(String app) {
		String sql = "delete from application where app=?";
		new JdbcTemplate(dataSource).update(sql, app);
	}

	public void registerApp(String app, String ips) throws ApplicationExistException {
		String sql = "select count(*) from application where name = ?";
		int cnt = new JdbcTemplate(dataSource).queryForObject(sql, new Object[]{app}, Integer.class);
		if(cnt > 0)
			throw new ApplicationExistException(app);
		ips = ips.replace('*', '-');
		sql = "insert into applicaton (name, ips) values (?, ?)";
		new JdbcTemplate(dataSource).update(sql, app, ips);
	}

	public boolean allowed(String app, String ip) {
		String sql = "select ips from application where name = ?";
		String allowedIps = new JdbcTemplate(dataSource).queryForObject(sql, new Object[]{app}, String.class);
		return allowedIps != null && allowedIps.contains(ip);
	}
	
	
}
