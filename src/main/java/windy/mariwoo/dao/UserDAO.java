package windy.mariwoo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import windy.mariwoo.model.DatabaseModel;
import windy.mariwoo.model.MedicineModel;
import windy.mariwoo.model.UserModel;


public class UserDAO {
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
					"SELECT no, id, pw, tel, email, birth, name "
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
				user.setName(rs.getString("name"));
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
	// - 아이디 중복 확인
	// //////////////////////////////////////////////////
	public boolean checkId(String userId) {

		boolean check = true;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT id FROM user_info WHERE id=? ");
			
			pstmt.setString(1, userId);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				check = false;
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
	// - 회원가입
	// //////////////////////////////////////////////////
	public boolean insertUser(UserModel modelParam) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"INSERT INTO user_info(id, pw, tel, email, birth, name) "
					+ "VALUES(?, ?, ?, ?, ?, ?) ");
			
			pstmt.setString(1, modelParam.getId());
			pstmt.setString(2, modelParam.getPw());
			pstmt.setString(3, modelParam.getTel());
			pstmt.setString(4, modelParam.getEmail());
			pstmt.setString(5, modelParam.getBirth());
			pstmt.setString(6, modelParam.getName());
			
			int cnt = pstmt.executeUpdate();
			System.out.println("cnt : "+cnt);
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
	// - 사용자 정보 수정 
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

	// //////////////////////////////////////////////////
	// - 용품 등록
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
	// - 가족 신청
	// //////////////////////////////////////////////////
	public boolean insertRelation(long userNo, long targetNo) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"INSERT INTO user_relation_info(user_no, target_no) "
					+ "VALUES(?, ?) ");
			
			pstmt.setLong(1, userNo);
			pstmt.setLong(2, targetNo);
			
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
	// - 타겟 유무 확인
	// //////////////////////////////////////////////////
	public long selectTarget(String userId, String userTel) {

		long targetNo = -1;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT no FROM user_info WHERE id=? AND tel=? ");
			
			pstmt.setString(1, userId);
			pstmt.setString(2, userTel);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				targetNo = rs.getLong("no");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return targetNo;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 가족 신청 수락 혹은 거절
	// //////////////////////////////////////////////////
	public boolean updateRelation(long no, String accept) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"UPDATE user_relation_info "
					+ "SET accept = ? "
					+ "WHERE no=? ");
			
			pstmt.setString(1, accept);
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
	// - 내 정보 열람자 목록
	// //////////////////////////////////////////////////
	public List<UserModel> selectListRelation(long userNo, String type) {

		List<UserModel> listUser = new ArrayList<UserModel>();
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			if("열람자".equals(type)) {

				pstmt = connection.prepareStatement(
						"select ui.no, ui.name, ui.tel "
						+ "from user_info ui, user_relation_info uri "
						+ "where ui.no=uri.target_no AND accept='Y' AND target_no=? "
						+ "ORDER BY uri.no DESC ");

				pstmt.setLong(1, userNo);
				
			}
			else if("대상자".equals(type)) {

				pstmt = connection.prepareStatement(
						"select ui.no, ui.name, ui.tel "
						+ "from user_info ui, user_relation_info uri "
						+ "where ui.no=uri.user_no AND accept='Y' AND target_no=? "
						+ "ORDER BY uri.no DESC ");

				pstmt.setLong(1, userNo);
			} 
			else if("신청자".equals(type)) {

				pstmt = connection.prepareStatement(
						"select ui.no, ui.name, ui.tel "
						+ "from user_info ui, user_relation_info uri "
						+ "where ui.no=uri.target_no AND accept='N' AND target_no=? "
						+ "ORDER BY uri.no DESC ");

				pstmt.setLong(1, userNo);
			}
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				UserModel user = new UserModel();
				user.setNo(rs.getLong("ui.no"));
				user.setName(rs.getString("name"));
				user.setTel(rs.getString("tel"));
				
				listUser.add(user);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return listUser;				
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
