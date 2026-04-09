package windy.mariwoo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import windy.mariwoo.model.DatabaseModel;
import windy.mariwoo.model.HospitalScheduleModel;
import windy.mariwoo.model.UserModel;

public class HospitalScheduleDAO {
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
	// - 스케쥴 목록 조회
	// //////////////////////////////////////////////////
	public List<HospitalScheduleModel> selectListSchedule(HospitalScheduleModel modelParam) {

		List<HospitalScheduleModel> listSchedule = new ArrayList<HospitalScheduleModel>();
		
		String whereSQL = "";
		
		if(!"".equals(modelParam.getStartAt()) && !"".equals(modelParam.getEndAt())) {
			whereSQL = " AND ( "
					+ "date(time) LIKE CONCAT('%', '"+modelParam.getStartAt()+"', '%') "
					+ "OR date(time) LIKE CONCAT('%', '"+modelParam.getEndAt()+", '%')"
					+ ")  ";
		}
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT no, user_no, name, time, notification_time, memo, created_at, updated_at "
					+ "FROM hospital_schedule_info "
					+ "WHERE user_no = ? "
					+ whereSQL
					+ "ORDER BY time ASC ");

			pstmt.setLong(1, modelParam.getUserNo());
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				HospitalScheduleModel schedule = new HospitalScheduleModel();
				schedule.setNo(rs.getLong("no"));
				schedule.setUserNo(rs.getLong("user_no"));
				schedule.setName(rs.getString("name"));
				schedule.setNotificationTime(rs.getString("notification_time"));
				schedule.setMemo(rs.getString("memo"));
				schedule.setCreatedAt(rs.getString("created_at"));
				schedule.setUpdatedAt(rs.getString("updated_at"));
				
				listSchedule.add(schedule);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return listSchedule;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 스케쥴 조회
	// //////////////////////////////////////////////////
	public HospitalScheduleModel selectSchedule(long no) {

		HospitalScheduleModel schedule = new HospitalScheduleModel();
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT no, user_no, name, time, notification_time, memo, created_at, updated_at "
					+ "FROM hospital_schedule_info "
					+ "WHERE no=? ");

			pstmt.setLong(1, no);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				schedule.setNo(rs.getLong("no"));
				schedule.setUserNo(rs.getLong("user_no"));
				schedule.setName(rs.getString("name"));
				schedule.setNotificationTime(rs.getString("notification_time"));
				schedule.setMemo(rs.getString("memo"));
				schedule.setCreatedAt(rs.getString("created_at"));
				schedule.setUpdatedAt(rs.getString("updated_at"));
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return schedule;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 스케쥴 등록
	// //////////////////////////////////////////////////
	public boolean insertHospitalSchedule(HospitalScheduleModel modelParam) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"INSERT INTO hospital_schedule (user_no, name, time, notification_time, memo) "
					+ "VALUES (?, ?, ?, DATE_SUB(?, INTERVAL 1 HOUR), ?) ");

			pstmt.setLong(1, modelParam.getUserNo());
			pstmt.setString(2, modelParam.getName());
			pstmt.setString(3, modelParam.getTime());
			pstmt.setString(4, modelParam.getTime());
			pstmt.setString(5, modelParam.getMemo());

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
	// - 스케쥴 수정
	// //////////////////////////////////////////////////
	public boolean updateHospitalSchedule(HospitalScheduleModel modelParam) {

		boolean check = false;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"UPDATE hospital_schedule "
					+ "SET name=?, time=?, notification_time=DATE_SUB(?, INTERVAL 1 HOUR), memo=? "
					+ "WHERE no=? ");

			pstmt.setString(1, modelParam.getName());
			pstmt.setString(2, modelParam.getTime());
			pstmt.setString(3, modelParam.getNotificationTime());
			pstmt.setString(4, modelParam.getMemo());
			pstmt.setLong(5, modelParam.getNo());

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
