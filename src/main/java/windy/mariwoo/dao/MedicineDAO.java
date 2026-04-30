package windy.mariwoo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import windy.mariwoo.model.DatabaseModel;
import windy.mariwoo.model.MedicineCardItem;
import windy.mariwoo.model.MedicineModel;
import windy.mariwoo.model.MedicineScheduleItem;
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
	// - 약 등록 (medicine 테이블)
	// medicine_info → medicine 으로 수정
	// //////////////////////////////////////////////////
	public long insertMedicine(MedicineModel modelParam) {

	    long no = -1;

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);

	        pstmt = connection.prepareStatement(
	                "INSERT INTO medicine(name, user_no) VALUES(?, ?)");

	        pstmt.setString(1, modelParam.getName());
	        pstmt.setLong(2, modelParam.getUserNo());

	        pstmt.executeUpdate();

	        // 자동생성된 no 가져오기
	        pstmt = connection.prepareStatement(
	                "SELECT no FROM medicine ORDER BY no DESC LIMIT 1");

	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            no = rs.getLong("no");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection);
	    }
	    return no;
	}


	// //////////////////////////////////////////////////
	// - 스케줄 등록 (medicine_schedule 테이블)
	// //////////////////////////////////////////////////
	public boolean insertSchedule(MedicineModel modelParam) {

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);

	        pstmt = connection.prepareStatement(
	                "INSERT INTO medicine_schedule(medicine_no, weekday, intake_time_type, intake_time, intake_type) "
	                + "VALUES(?, ?, ?, ?, ?)");

	        pstmt.setLong(1, modelParam.getNo());               // medicine_no
	        pstmt.setInt(2, modelParam.getWeekDay());            // weekday (0:월 ~ 6:일)
	        pstmt.setString(3, modelParam.getIntakeTimeType());  // intake_time_type (0~3)
	        pstmt.setString(4, modelParam.getIntakeTime());      // intake_time (HH:mm)
	        pstmt.setString(5, modelParam.getIntakeType());      // intake_type (식전/식후)

	        int result = pstmt.executeUpdate();
	        return result > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        close(rs, pstmt, connection);
	    }
	}
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
				medicine.setNo(rs.getLong("no"));
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
				}

				medicine.setListMedicine(listModel);

				// ✅ 스케줄 있는 약만 추가
				if (listModel.size() > 0) {
				    listMedicine.add(medicine);
				}
				
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
	public List<MedicineCardItem> getIntakeList(long userNo, String date, int weekday) {
	    List<MedicineCardItem> result = new ArrayList<>();

	    String sql = "SELECT m.name, ms.no AS schedule_no, ms.intake_time_type, " +
	                 "ms.intake_time, ms.intake_type, " +
	                 "IFNULL(mi.is_taken, 0) AS is_taken " +
	                 "FROM medicine_schedule ms " +
	                 "JOIN medicine m ON ms.medicine_no = m.no " +
	                 "LEFT JOIN medicine_intake mi ON ms.no = mi.schedule_no " +
	                 "AND mi.intake_date = ? " +
	                 "WHERE m.user_no = ? AND ms.weekday = ? " +
	                 "ORDER BY m.no, ms.intake_time";

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password); // ✅ 수정
	        pstmt = connection.prepareStatement(sql);

	        pstmt.setString(1, date);
	        pstmt.setLong(2, userNo);
	        pstmt.setInt(3, weekday);

	        rs = pstmt.executeQuery();

	        String currentMedicineName = "";
	        MedicineCardItem currentCard = null;

	        while (rs.next()) {
	            String medicineName = rs.getString("name");

	            if (!medicineName.equals(currentMedicineName)) {
	                currentCard = new MedicineCardItem();
	                currentCard.setMedicineName(medicineName);
	                currentCard.setSchedules(new ArrayList<>());
	                result.add(currentCard);
	                currentMedicineName = medicineName;
	            }

	            MedicineScheduleItem schedule = new MedicineScheduleItem();
	            schedule.setScheduleNo(rs.getLong("schedule_no"));
	            schedule.setIntakeTimeType(rs.getString("intake_time_type"));
	            schedule.setIntakeType(rs.getString("intake_type"));
	            schedule.setIntakeTime(rs.getString("intake_time"));
	            schedule.setTaken(rs.getInt("is_taken") == 1);

	            currentCard.getSchedules().add(schedule);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection); // ✅ 수정
	    }

	    return result;
	}

	public void checkIntake(long scheduleNo, String date, int isTaken) {
	    String sql = "INSERT INTO medicine_intake (schedule_no, intake_date, is_taken) " +
	                 "VALUES (?, ?, ?) " +
	                 "ON DUPLICATE KEY UPDATE is_taken = ?";

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password); // ✅ 수정
	        pstmt = connection.prepareStatement(sql);

	        pstmt.setLong(1, scheduleNo);
	        pstmt.setString(2, date);
	        pstmt.setInt(3, isTaken);
	        pstmt.setInt(4, isTaken);

	        pstmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection); // ✅ 수정
	    }
	}
	// 약 상세 조회 (수정 화면용)
	public List<MedicineModel> getMedicineDetail(long medicineNo, int weekday) {
	    List<MedicineModel> list = new ArrayList<>();

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);

	        pstmt = connection.prepareStatement(
	                "SELECT no, medicine_no, weekday, intake_time_type, " +
	                "DATE_FORMAT(intake_time, '%H:%i') as intake_time, intake_type " +
	                "FROM medicine_schedule " +
	                "WHERE medicine_no = ? AND weekday = ? " +
	                "ORDER BY intake_time");

	        pstmt.setLong(1, medicineNo);
	        pstmt.setInt(2, weekday);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            MedicineModel model = new MedicineModel();
	            model.setScheduleNo(rs.getLong("no"));       // ✅ 스케줄 PK
	            model.setNo(rs.getLong("medicine_no"));      // ✅ 약 번호
	            model.setWeekDay(rs.getInt("weekday"));
	            model.setIntakeTimeType(rs.getString("intake_time_type"));
	            model.setIntakeTime(rs.getString("intake_time"));
	            model.setIntakeType(rs.getString("intake_type"));
	            list.add(model);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection);
	    }
	    return list;
	}
	// 약 스케줄 수정 (UPDATE + INSERT)
	public boolean updateSchedule(long medicineNo, List<MedicineModel> insertList, List<MedicineModel> updateList) {
	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);
	        connection.setAutoCommit(false);

	        // ✅ scheduleNo(PK) 기준 UPDATE → 해당 요일 행만 변경
	        pstmt = connection.prepareStatement(
	                "UPDATE medicine_schedule " +
	                "SET intake_time=?, intake_type=? " +
	                "WHERE no=?");

	        for (MedicineModel s : updateList) {
	            pstmt.setString(1, s.getIntakeTime());
	            pstmt.setString(2, s.getIntakeType());
	            pstmt.setLong(3, s.getScheduleNo()); // ✅ 스케줄 PK
	            pstmt.executeUpdate();
	        }
	        pstmt.close();

	        // ✅ 신규 시간대 INSERT
	        pstmt = connection.prepareStatement(
	                "INSERT INTO medicine_schedule " +
	                "(medicine_no, weekday, intake_time_type, intake_time, intake_type) " +
	                "VALUES (?, ?, ?, ?, ?)");

	        for (MedicineModel s : insertList) {
	            pstmt.setLong(1, s.getNo());          // ✅ 약 번호 (no 필드)
	            pstmt.setInt(2, s.getWeekDay());
	            pstmt.setString(3, s.getIntakeTimeType());
	            pstmt.setString(4, s.getIntakeTime());
	            pstmt.setString(5, s.getIntakeType());
	            pstmt.executeUpdate();
	        }
	        pstmt.close();

	        connection.commit();
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        if (connection != null) {
	            try { connection.rollback(); } catch (Exception ignored) {}
	        }
	        return false;
	    } finally {
	        close(rs, pstmt, connection);
	    }
	}

	// ✅ 스케줄 단건 삭제
	public void deleteScheduleByNo(long scheduleNo) {
	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);

	        pstmt = connection.prepareStatement(
	                "DELETE FROM medicine_schedule WHERE no=?");
	        pstmt.setLong(1, scheduleNo);
	        pstmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection);
	    }
	}
	public List<MedicineCardItem> getFamilyIntakeList(long targetNo, String date, int weekday) {
	    List<MedicineCardItem> cardList = new ArrayList<>();

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);

	        pstmt = connection.prepareStatement(
	            "SELECT m.no          AS medicine_no, " +
	            "       m.name        AS medicine_name, " +
	            "       ms.no         AS schedule_no, " +
	            "       ms.intake_time_type, " +
	            "       ms.intake_type, " +
	            "       DATE_FORMAT(ms.intake_time, '%H:%i') AS intake_time, " +
	            "       IFNULL(mi.is_taken, 0) AS is_taken " +  // ✅ 복용 기록 없으면 0
	            "FROM medicine m " +
	            "JOIN medicine_schedule ms " +
	            "  ON ms.medicine_no = m.no " +
	            "  AND ms.weekday = ? " +                       // ✅ 요일 필터
	            "LEFT JOIN medicine_intake mi " +
	            "  ON mi.schedule_no = ms.no " +
	            "  AND mi.intake_date = ? " +                   // ✅ 날짜 필터
	            "WHERE m.user_no = ? " +                        // ✅ 대상자 필터
	            "ORDER BY m.no, ms.intake_time"
	        );

	        pstmt.setInt(1, weekday);
	        pstmt.setString(2, date);
	        pstmt.setLong(3, targetNo);

	        rs = pstmt.executeQuery();

	        // medicine_no 기준으로 카드 묶기
	        Map<Long, MedicineCardItem> cardMap = new LinkedHashMap<>();

	        while (rs.next()) {
	            long   medicineNo   = rs.getLong("medicine_no");
	            String medicineName = rs.getString("medicine_name");

	            // ✅ 약 카드가 없으면 새로 생성
	            if (!cardMap.containsKey(medicineNo)) {
	                MedicineCardItem card = new MedicineCardItem();
	                card.setMedicineName(medicineName);
	                card.setSchedules(new ArrayList<>());
	                cardMap.put(medicineNo, card);
	            }

	            MedicineScheduleItem schedule = new MedicineScheduleItem();
	            schedule.setScheduleNo(rs.getLong("schedule_no"));
	            schedule.setIntakeTimeType(rs.getString("intake_time_type"));
	            schedule.setIntakeType(rs.getString("intake_type"));
	            schedule.setIntakeTime(rs.getString("intake_time"));
	            schedule.setTaken(rs.getInt("is_taken") == 1);

	            cardMap.get(medicineNo).getSchedules().add(schedule);
	        }

	        cardList.addAll(cardMap.values());

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection);
	    }
	    return cardList;
	}
	
	
	public List<Map<String, String>> getCalendarIntake(long userNo, int year, int month) {

	    List<Map<String, String>> list = new ArrayList<>();

	    try {
	        Class.forName(dbDriver);
	        connection = DriverManager.getConnection(jdbcUrl, id, password);
	        pstmt = connection.prepareStatement(
	        	    "SELECT " +
	        	    "    d.intake_date, " +
	        	    "    COUNT(ms.no) AS total, " +
	        	    "    SUM(CASE WHEN mi.is_taken = 1 THEN 1 ELSE 0 END) AS taken " +
	        	    "FROM ( " +
	        	    "    SELECT DATE(?) + INTERVAL (seq) DAY AS intake_date " +
	        	    "    FROM ( " +
	        	    "        SELECT a.N + b.N * 10 AS seq " +
	        	    "        FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
	        	    "              UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a, " +
	        	    "             (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) b " +
	        	    "    ) nums " +
	        	    "    WHERE DATE(?) + INTERVAL (seq) DAY <= LEAST(LAST_DAY(?), CURDATE()) " +
	        	    ") d " +
	        	    "JOIN medicine m ON m.user_no = ? " +
	        	    "JOIN medicine_schedule ms ON ms.medicine_no = m.no AND ms.weekday = WEEKDAY(d.intake_date) " +
	        	    "LEFT JOIN medicine_intake mi ON mi.schedule_no = ms.no AND mi.intake_date = d.intake_date " +
	        	    "GROUP BY d.intake_date"
	        	);

	        	String firstDay = year + "-" + String.format("%02d", month) + "-01";
	        	pstmt.setString(1, firstDay);
	        	pstmt.setString(2, firstDay);
	        	pstmt.setString(3, firstDay);
	        	pstmt.setLong(4, userNo);
	        	
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            String date  = rs.getString("intake_date");
	            int    total = rs.getInt("total");
	            int    taken = rs.getInt("taken");

	            String status = (total == taken) ? "all" : "partial";

	            Map<String, String> item = new HashMap<>();
	            item.put("date",   date);
	            item.put("status", status);
	            list.add(item);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        close(rs, pstmt, connection);
	    }

	    return list;
	}
	/*
	 * // 스케줄 전체 삭제 후 재등록 (수정) public boolean updateSchedule(long medicineNo,
	 * List<MedicineModel> schedules) { try { Class.forName(dbDriver); connection =
	 * DriverManager.getConnection(jdbcUrl, id, password);
	 * 
	 * // 기존 스케줄 전체 삭제 pstmt = connection.prepareStatement(
	 * "DELETE FROM medicine_schedule WHERE medicine_no = ?"); pstmt.setLong(1,
	 * medicineNo); pstmt.executeUpdate();
	 * 
	 * // 새 스케줄 등록 for (MedicineModel schedule : schedules) { pstmt =
	 * connection.prepareStatement(
	 * "INSERT INTO medicine_schedule(medicine_no, weekday, intake_time_type, intake_time, intake_type) "
	 * + "VALUES(?, ?, ?, ?, ?)"); pstmt.setLong(1, medicineNo); pstmt.setInt(2,
	 * schedule.getWeekDay()); pstmt.setString(3, schedule.getIntakeTimeType());
	 * pstmt.setString(4, schedule.getIntakeTime()); pstmt.setString(5,
	 * schedule.getIntakeType()); pstmt.executeUpdate(); }
	 * 
	 * return true;
	 * 
	 * } catch (Exception e) { e.printStackTrace(); return false; } finally {
	 * close(rs, pstmt, connection); } }
	 */
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
