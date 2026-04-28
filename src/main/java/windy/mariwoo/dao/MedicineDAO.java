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


public class MedicineDAO {
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
	// - 용품 등록
	// //////////////////////////////////////////////////
	public long insertMedicine(MedicineModel modelParam) {

		long no = -1;
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"INSERT INTO medicine_info(name, user_no) VALUES(?, ?) ");
			
			pstmt.setString(1, modelParam.getName());
			pstmt.setLong(2, modelParam.getUserNo());
			
			pstmt.executeUpdate();
			
			pstmt = connection.prepareStatement(
					"SELECT no FROM medicine_info ORDER BY medicine_no DESC limit 1 ");
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				no = rs.getLong("no");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return no;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 약 목록
	// //////////////////////////////////////////////////
	public List<MedicineModel> selectListMedicine(MedicineModel modelParam) {

		List<MedicineModel> listMedicine = new ArrayList<MedicineModel>();
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"select no, name, user_no "
					+ "from medicine m "
					+ "where user_no = ? "
					+ "ORDER BY m.no ASC ");

			pstmt.setLong(1, modelParam.getUserNo());
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				MedicineModel medicine = new MedicineModel();
				System.out.println("rs.getLong(\"no\") : "+rs.getLong("no"));
				medicine.setScheduleNo(rs.getLong("no"));
				medicine.setName(rs.getString("name"));

				ArrayList<MedicineModel> listModel = new ArrayList<>();
				
				pstmt = connection.prepareStatement(
						"SELECT ms.no, ms.medicine_no, ms.weekday, m.name, DATE_FORMAT(ms.intake_time, '%H:%i') as intake_time, ms.intake_type, ms.alarm_enabled, ms.intake_time_type "
						+ "FROM medicine_schedule ms "
						+ "JOIN medicine m ON ms.medicine_no = m.no "
						+ "WHERE ms.weekday = ? AND ms.medicine_no = ? "
						+ "ORDER BY ms.weekday, m.name, ms.intake_time ");

				pstmt.setInt(1, modelParam.getWeekDay());
				pstmt.setLong(2, rs.getLong("no"));
				
				ResultSet rs2 = pstmt.executeQuery();
				
				while(rs2.next()) {
					MedicineModel model = new MedicineModel();
					model.setScheduleNo(rs2.getLong("no"));
					model.setName(medicine.getName());
					model.setIntakeTimeType(rs2.getString("intake_time_type"));
					model.setIntakeType(rs2.getString("intake_type")+" "+rs2.getString("intake_time"));
					
					listModel.add(model);
				};
				
				medicine.setListMedicine(listModel);
				
				listMedicine.add(medicine);	
				
				
			}
			System.out.println("listMedicine.size() : "+listMedicine.size());;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return listMedicine;				
	}
			
	// //////////////////////////////////////////////////

	// //////////////////////////////////////////////////
	// - 약 목록
	// //////////////////////////////////////////////////
	public List<MedicineModel> selectListMedicineForModify(MedicineModel modelParam) {

		List<MedicineModel> listMedicine = new ArrayList<MedicineModel>();
		
		try {
			// 데이터베이스 객체 생성
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT ms.no, ms.medicine_no, ms.weekday, m.name, ms.intake_time, ms.intake_type, ms.alarm_enabled, ms.intake_time_type "
					+ "FROM medicine_schedule ms "
					+ "JOIN medicine m ON ms.medicine_no = m.no "
					+ "WHERE ms.weekday = ? AND ms.medicine_no = ? "
					+ "ORDER BY ms.weekday, m.name, ms.intake_time ");

			pstmt.setLong(1, modelParam.getUserNo());
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				MedicineModel medicine = new MedicineModel();
				medicine.setScheduleNo(rs.getLong("no"));
				medicine.setName(rs.getString("name"));
				medicine.setIntakeTimeType(rs.getString("intake_time_type"));
				medicine.setIntakeType(rs.getString("intake_type")+" "+rs.getString("intake_time"));
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 사용한 객체 종료
			close(rs, pstmt, connection);
		}
		return listMedicine;				
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
