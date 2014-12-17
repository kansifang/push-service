package common.pushservice;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class PushServiceImpl implements PushService {
	private DataSource dataSource;
	private ApplePushService applePushService;
	private GooglePushService googlePushService;
	
	private void addUser(String userId, TokenType type, String token) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		
		String sql = "delete from user_token where user_id=? and type=?";
		jt.update(sql, userId, type.name());
		
		sql = "insert into user_token(user_id, type, token) values (?, ?, ?)";
		jt.update(sql, userId, TokenType.Apple.name(), token);
	}
	
	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
	}
	
	public void set(ApplePushService applePushService) {
		this.applePushService = applePushService;
	}
	
	public void set(GooglePushService googlePushService) {
		this.googlePushService = googlePushService;
	}
	
	public void setup() {
		String sql = "create table if not exists user_token (user_id varchar(64), type varchar(32), token varchar(1023), primary key (user_id, type)";
		new JdbcTemplate(dataSource).update(sql);
	}

	public void addAppleUser(String userId, String appleToken) {
		addUser(userId, TokenType.Apple, appleToken);
	}

	public void addGoogleUser(String userId, String googleToken) {
		addUser(userId, TokenType.Google, googleToken);
	}

	public void push(String userId, PushMessage msg) throws IOException {
		String sql = "select * from user_token where user_id=?";
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		List<UserToken> tokens = jt.query(sql, new Object[]{}, new RowMapper<UserToken>(){
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
				applePushService.push(ut.token, msg);
			}
			else if(TokenType.Google.equals(ut.type)) {
				googlePushService.push(ut.token, msg);
			}
		}
	}

	public void pushToApple(String token, PushMessage msg) {
		applePushService.push(token, msg);
	}

	public void pushToGoogle(String token, PushMessage msg) throws IOException {
		googlePushService.push(token, msg);
	}

}
