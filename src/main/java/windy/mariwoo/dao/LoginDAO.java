package windy.mariwoo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import windy.mariwoo.model.DatabaseModel;
import windy.mariwoo.model.UserModel;


public class LoginDAO {
	private Connection connection = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private DatabaseModel dbModel = new DatabaseModel();
	// DB Driver
  
    String dbDriver = "org.mariadb.jdbc.Driver";
	private String jdbcUrl = dbModel.getJdbcUrl();
    //private String jdbcUrl = "jdbc:mariadb://192.168.0.60:33308/bicycledb";
	private String id = dbModel.getUser();         
	private String password = dbModel.getPassword();
	

	// //////////////////////////////////////////////////
	// - 로그인
	// //////////////////////////////////////////////////
	public UserModel login(UserModel user) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT no, id, pw, tel, email, birth "
					+ "FROM user_info "
					+ "WHERE id=? AND pw=? ");

			pstmt.setString(1, user.getId());
			pstmt.setString(2, user.getPw());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				user.setCheck(true);
				user.setNo(rs.getLong("no"));
				user.setId(rs.getString("id"));
				user.setPw(rs.getString("pw"));
				user.setTel(rs.getString("tel"));
				user.setEmail(rs.getString("email"));
				user.setBirth(rs.getString("birth"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return user;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 비밀번호 변경
	// //////////////////////////////////////////////////
	public boolean updatePw(long no, String pw) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"UPDATE user_info SET pw=? WHERE no=? ");
			
			pstmt.setString(1, pw);
			pstmt.setLong(2, no);
			
			int cnt = pstmt.executeUpdate();
			
			if(cnt>0) {
				check = true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return check;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 유저 정보 수정
	// //////////////////////////////////////////////////
	public boolean updateUser(UserModel modelParam) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"UPDATE user_info "
					+ "SET tel=?, email=?, birth=? "
					+ "WHERE no=? ");
			
			pstmt.setString(1, modelParam.getTel());
			pstmt.setString(2, modelParam.getEmail());
			pstmt.setString(3, modelParam.getBirth());
			pstmt.setLong(4, modelParam.getNo());
			
			int cnt = pstmt.executeUpdate();
			
			if(cnt>0) {
				check = true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return check;				
	}
			
	// //////////////////////////////////////////////////

	
	
	////////////////////////////////////////////////////
	//	- 데이터베이스 관련 객체 정리 -
	public void close(ResultSet rs, PreparedStatement pstmt, Connection conn) {
		
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
