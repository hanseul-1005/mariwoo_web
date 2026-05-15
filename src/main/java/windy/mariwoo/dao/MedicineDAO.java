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

/**
 * 약(medicine) 관련 DB 처리를 담당하는 DAO 클래스
 * - medicine        : 약 기본 정보 테이블
 * - medicine_schedule : 요일/시간대별 복용 스케줄 테이블
 * - medicine_intake  : 실제 복용 기록 테이블
 */
public class MedicineDAO {

	// DB 연결 관련 객체 (사용 후 반드시 close() 호출)
	private Connection        connection = null;
	private PreparedStatement pstmt      = null;
	private ResultSet         rs         = null;

	private DatabaseModel dbModel = new DatabaseModel();

	// MariaDB JDBC 드라이버
	String dbDriver = "org.mariadb.jdbc.Driver";

	// DB 접속 정보 (DatabaseModel에서 관리)
	private String jdbcUrl  = dbModel.getJdbcUrl();
	private String id       = dbModel.getUser();
	private String password = dbModel.getPassword();

	// ==========================================================
	// 약 등록 (medicine 테이블 INSERT)
	// 반환값: 등록된 약의 PK (no), 실패 시 -1
	// ==========================================================
	public long insertMedicine(MedicineModel modelParam) {

		long no = -1; // 등록 실패 기본값

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			// del='N' : 삭제되지 않은 정상 상태로 초기화 (soft delete 방식)
			// RETURN_GENERATED_KEYS: executeUpdate 후 자동 생성된 PK를 바로 조회
			pstmt = connection.prepareStatement(
					"INSERT INTO medicine(name, user_no, del) VALUES(?, ?, 'N')",
					java.sql.Statement.RETURN_GENERATED_KEYS);

			pstmt.setString(1, modelParam.getName());   // 약 이름
			pstmt.setLong(2, modelParam.getUserNo());   // 등록한 사용자 번호

			int affected = pstmt.executeUpdate();

			if (affected > 0) {
				// INSERT 성공: 자동 생성된 PK 조회 (LAST_INSERT_ID() 별도 쿼리 불필요)
				rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					no = rs.getLong(1);
					System.out.println("insertMedicine 성공: no=" + no + ", name=" + modelParam.getName());
				}
			} else {
				System.out.println("insertMedicine 실패: affected=0, name=" + modelParam.getName());
			}

		} catch (Exception e) {
			System.err.println("insertMedicine 예외 발생: name=" + modelParam.getName()
					+ ", userNo=" + modelParam.getUserNo());
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
		return no;
	}

	// ==========================================================
	// 복용 스케줄 등록 (medicine_schedule 테이블 INSERT)
	// 반환값: 성공 true / 실패 false
	// ==========================================================
	public boolean insertSchedule(MedicineModel modelParam) {

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"INSERT INTO medicine_schedule(medicine_no, weekday, intake_time_type, intake_time, intake_type) "
					+ "VALUES(?, ?, ?, ?, ?)");

			pstmt.setLong(1, modelParam.getNo());              // 약 번호 (FK)
			pstmt.setInt(2, modelParam.getWeekDay());          // 요일 (0:월 ~ 6:일)
			pstmt.setString(3, modelParam.getIntakeTimeType()); // 시간대 (아침/점심/저녁/취침 전)
			pstmt.setString(4, modelParam.getIntakeTime());    // 복용 시간 (HH:mm)
			pstmt.setString(5, modelParam.getIntakeType());    // 식전/식후

			int result = pstmt.executeUpdate();
			return result > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			close(rs, pstmt, connection);
		}
	}

	// ==========================================================
	// 약 목록 조회 (약 목록 탭 - 요일별)
	// del='N'인 약만 조회 (soft delete 적용)
	// 해당 요일에 스케줄이 있는 약만 목록에 포함
	// ==========================================================
	public List<MedicineModel> selectListMedicine(MedicineModel modelParam) {

		List<MedicineModel> listMedicine = new ArrayList<>();

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			// 1차 쿼리: 사용자의 약 목록 (삭제되지 않은 것만)
			pstmt = connection.prepareStatement(
					"SELECT no, name, user_no "
					+ "FROM medicine m "
					+ "WHERE user_no = ? AND del = 'N' "
					+ "ORDER BY m.no ASC");

			pstmt.setLong(1, modelParam.getUserNo());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				MedicineModel medicine = new MedicineModel();
				System.out.println("rs.getLong(\"no\") : " + rs.getLong("no"));
				medicine.setNo(rs.getLong("no"));
				medicine.setName(rs.getString("name"));

				ArrayList<MedicineModel> listModel = new ArrayList<>();

				// 2차 쿼리: 해당 약의 요일별 스케줄 조회
				pstmt = connection.prepareStatement(
						"SELECT ms.no, ms.medicine_no, ms.weekday, m.name, "
						+ "DATE_FORMAT(ms.intake_time, '%H:%i') AS intake_time, "
						+ "ms.intake_type, ms.alarm_enabled, ms.intake_time_type "
						+ "FROM medicine_schedule ms "
						+ "JOIN medicine m ON ms.medicine_no = m.no "
						+ "WHERE ms.weekday = ? AND ms.medicine_no = ? "
						+ "ORDER BY ms.weekday, m.name, ms.intake_time");

				pstmt.setInt(1, modelParam.getWeekDay()); // 선택된 요일
				pstmt.setLong(2, rs.getLong("no"));       // 약 번호

				ResultSet rs2 = pstmt.executeQuery();

				while (rs2.next()) {
					MedicineModel model = new MedicineModel();
					model.setScheduleNo(rs2.getLong("no"));
					model.setName(medicine.getName());
					model.setIntakeTimeType(rs2.getString("intake_time_type"));
					// 예: "식후 08:00" 형태로 합쳐서 저장
					model.setIntakeType(rs2.getString("intake_type") + " " + rs2.getString("intake_time"));
					listModel.add(model);
				}
				rs2.close(); // ResultSet 명시적 닫기 (커넥션 누수 방지)

				medicine.setListMedicine(listModel);

				// 해당 요일에 스케줄이 있는 약만 목록에 추가
				if (listModel.size() > 0) {
					listMedicine.add(medicine);
				}
			}

			System.out.println("listMedicine.size() : " + listMedicine.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
		return listMedicine;
	}

	// ==========================================================
	// 약 목록 조회 (수정용 - 현재 미사용)
	// ==========================================================
	public List<MedicineModel> selectListMedicineForModify(MedicineModel modelParam) {

		List<MedicineModel> listMedicine = new ArrayList<>();

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT ms.no, ms.medicine_no, ms.weekday, m.name, "
					+ "ms.intake_time, ms.intake_type, ms.alarm_enabled, ms.intake_time_type "
					+ "FROM medicine_schedule ms "
					+ "JOIN medicine m ON ms.medicine_no = m.no "
					+ "WHERE ms.weekday = ? AND ms.medicine_no = ? "
					+ "ORDER BY ms.weekday, m.name, ms.intake_time");

			pstmt.setLong(1, modelParam.getUserNo());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				MedicineModel medicine = new MedicineModel();
				medicine.setScheduleNo(rs.getLong("no"));
				medicine.setName(rs.getString("name"));
				medicine.setIntakeTimeType(rs.getString("intake_time_type"));
				medicine.setIntakeType(rs.getString("intake_type") + " " + rs.getString("intake_time"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
		return listMedicine;
	}

	// ==========================================================
	// 복용 내역 조회 (복용 체크 탭 - 날짜/요일별)
	// medicine_intake에 기록이 없으면 is_taken=0 (미복용)으로 처리
	// del='N'인 약만 조회 (soft delete 적용)
	// ==========================================================
	public List<MedicineCardItem> getIntakeList(long userNo, String date, int weekday) {
		List<MedicineCardItem> result = new ArrayList<>();

		String sql = "SELECT m.name, ms.no AS schedule_no, ms.intake_time_type, " +
				"ms.intake_time, ms.intake_type, " +
				"IFNULL(mi.is_taken, 0) AS is_taken " +  // 복용 기록 없으면 0(미복용)
				"FROM medicine_schedule ms " +
				"JOIN medicine m ON ms.medicine_no = m.no " +
				"LEFT JOIN medicine_intake mi ON ms.no = mi.schedule_no " +
				"AND mi.intake_date = ? " +               // 특정 날짜의 복용 기록만
				"WHERE m.user_no = ? AND ms.weekday = ? AND m.del = 'N' " +
				"ORDER BY m.no, ms.intake_time";

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);
			pstmt = connection.prepareStatement(sql);

			pstmt.setString(1, date);    // 조회 날짜 (yyyy-MM-dd)
			pstmt.setLong(2, userNo);    // 사용자 번호
			pstmt.setInt(3, weekday);    // 요일 (0:월 ~ 6:일)

			rs = pstmt.executeQuery();

			// 같은 약 이름이 연속으로 나오면 하나의 카드로 묶기
			String currentMedicineName = "";
			MedicineCardItem currentCard = null;

			while (rs.next()) {
				String medicineName = rs.getString("name");

				// 새로운 약 이름이 나오면 카드 새로 생성
				if (!medicineName.equals(currentMedicineName)) {
					currentCard = new MedicineCardItem();
					currentCard.setMedicineName(medicineName);
					currentCard.setSchedules(new ArrayList<>());
					result.add(currentCard);
					currentMedicineName = medicineName;
				}

				// 스케줄 항목 추가
				MedicineScheduleItem schedule = new MedicineScheduleItem();
				schedule.setScheduleNo(rs.getLong("schedule_no"));
				schedule.setIntakeTimeType(rs.getString("intake_time_type")); // 아침/점심/저녁/취침 전
				schedule.setIntakeType(rs.getString("intake_type"));          // 식전/식후
				schedule.setIntakeTime(rs.getString("intake_time"));          // HH:mm
				schedule.setTaken(rs.getInt("is_taken") == 1);                // 1=복용, 0=미복용

				currentCard.getSchedules().add(schedule);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}

		return result;
	}

	// ==========================================================
	// 복용 체크 저장/업데이트
	// 동일한 (schedule_no, intake_date) 조합이 이미 있으면 UPDATE
	// 없으면 INSERT (ON DUPLICATE KEY UPDATE 활용)
	// ==========================================================
	public void checkIntake(long scheduleNo, String date, int isTaken) {
		String sql = "INSERT INTO medicine_intake (schedule_no, intake_date, is_taken) " +
				"VALUES (?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE is_taken = ?";

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);
			pstmt = connection.prepareStatement(sql);

			pstmt.setLong(1, scheduleNo);   // 스케줄 번호 (FK)
			pstmt.setString(2, date);        // 복용 날짜 (yyyy-MM-dd)
			pstmt.setInt(3, isTaken);        // 복용 여부 (1:복용, 0:미복용)
			pstmt.setInt(4, isTaken);        // ON DUPLICATE UPDATE용

			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
	}

	// ==========================================================
	// 약 상세 조회 (수정 화면 진입 시 기존 데이터 불러오기)
	// 특정 요일의 복용 스케줄 목록 반환
	// ==========================================================
	public List<MedicineModel> getMedicineDetail(long medicineNo, int weekday) {
		List<MedicineModel> list = new ArrayList<>();

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT no, medicine_no, weekday, intake_time_type, " +
					"DATE_FORMAT(intake_time, '%H:%i') AS intake_time, intake_type " +
					"FROM medicine_schedule " +
					"WHERE medicine_no = ? AND weekday = ? " +
					"ORDER BY intake_time");

			pstmt.setLong(1, medicineNo); // 약 번호
			pstmt.setInt(2, weekday);     // 요일
			rs = pstmt.executeQuery();

			while (rs.next()) {
				MedicineModel model = new MedicineModel();
				model.setScheduleNo(rs.getLong("no"));         // 스케줄 PK (수정/삭제에 사용)
				model.setNo(rs.getLong("medicine_no"));        // 약 번호
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

	// ==========================================================
	// 약 스케줄 수정 (UPDATE + INSERT 트랜잭션)
	// - updateList: 기존 스케줄 시간/타입 변경
	// - insertList: 새로 추가된 시간대 INSERT
	// 실패 시 rollback으로 데이터 일관성 보장
	// ==========================================================
	public boolean updateSchedule(long medicineNo, List<MedicineModel> insertList, List<MedicineModel> updateList) {
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);
			connection.setAutoCommit(false); // 트랜잭션 시작

			// 기존 스케줄 수정: 스케줄 PK(no) 기준으로 시간/타입 변경
			pstmt = connection.prepareStatement(
					"UPDATE medicine_schedule " +
					"SET intake_time=?, intake_type=? " +
					"WHERE no=?");

			for (MedicineModel s : updateList) {
				pstmt.setString(1, s.getIntakeTime());
				pstmt.setString(2, s.getIntakeType());
				pstmt.setLong(3, s.getScheduleNo()); // 스케줄 PK로 특정 행만 수정
				pstmt.executeUpdate();
			}
			pstmt.close();

			// 신규 시간대 추가: 기존에 없던 시간대(아침/점심/저녁/취침 전) INSERT
			pstmt = connection.prepareStatement(
					"INSERT INTO medicine_schedule " +
					"(medicine_no, weekday, intake_time_type, intake_time, intake_type) " +
					"VALUES (?, ?, ?, ?, ?)");

			for (MedicineModel s : insertList) {
				pstmt.setLong(1, s.getNo());           // 약 번호 (FK)
				pstmt.setInt(2, s.getWeekDay());       // 요일
				pstmt.setString(3, s.getIntakeTimeType());
				pstmt.setString(4, s.getIntakeTime());
				pstmt.setString(5, s.getIntakeType());
				pstmt.executeUpdate();
			}
			pstmt.close();

			connection.commit(); // UPDATE + INSERT 모두 성공 시 커밋
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			if (connection != null) {
				try { connection.rollback(); } catch (Exception ignored) {} // 실패 시 롤백
			}
			return false;
		} finally {
			close(rs, pstmt, connection);
		}
	}

	// ==========================================================
	// 스케줄 단건 삭제
	// 수정 화면에서 특정 시간대 제거 시 호출 (ex: 아침 스케줄 삭제)
	// ==========================================================
	public void deleteScheduleByNo(long scheduleNo) {
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"DELETE FROM medicine_schedule WHERE no=?");
			pstmt.setLong(1, scheduleNo); // 삭제할 스케줄 PK
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
	}

	// ==========================================================
	// 가족(대상자) 복용 내역 조회
	// - 열람 관계인 대상자의 특정 날짜/요일 복용 현황 조회
	// - del='N'인 약만 조회 (soft delete 적용)
	// - LinkedHashMap으로 약 순서 보장
	// ==========================================================
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
					"       IFNULL(mi.is_taken, 0) AS is_taken " + // 복용 기록 없으면 0(미복용)
					"FROM medicine m " +
					"JOIN medicine_schedule ms " +
					"  ON ms.medicine_no = m.no " +
					"  AND ms.weekday = ? " +                      // 요일 필터
					"LEFT JOIN medicine_intake mi " +
					"  ON mi.schedule_no = ms.no " +
					"  AND mi.intake_date = ? " +                  // 날짜 필터
					"WHERE m.user_no = ? AND m.del = 'N' " +       // 대상자 + soft delete 필터
					"ORDER BY m.no, ms.intake_time"
			);

			pstmt.setInt(1, weekday);   // 요일
			pstmt.setString(2, date);   // 날짜
			pstmt.setLong(3, targetNo); // 대상자 user_no

			rs = pstmt.executeQuery();

			// medicine_no 기준으로 카드 묶기 (삽입 순서 보장을 위해 LinkedHashMap 사용)
			Map<Long, MedicineCardItem> cardMap = new LinkedHashMap<>();

			while (rs.next()) {
				long   medicineNo   = rs.getLong("medicine_no");
				String medicineName = rs.getString("medicine_name");

				// 해당 약 카드가 없으면 새로 생성
				if (!cardMap.containsKey(medicineNo)) {
					MedicineCardItem card = new MedicineCardItem();
					card.setMedicineName(medicineName);
					card.setSchedules(new ArrayList<>());
					cardMap.put(medicineNo, card);
				}

				// 스케줄 항목 추가
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

	// ==========================================================
	// 달력 복용 현황 조회 (월별)
	// - 해당 월의 1일~오늘까지 날짜별 복용 완료/부분 상태 반환
	// - 인라인 날짜 시퀀스 생성으로 날짜 테이블 없이 범위 조회
	// - del='N'인 약만 포함 (soft delete 적용)
	// 반환: [{"date":"2025-05-01","status":"all"}, ...]
	//   all     → 해당 날짜 전체 복용
	//   partial → 일부 미복용
	// ==========================================================
	public List<Map<String, String>> getCalendarIntake(long userNo, int year, int month) {

		List<Map<String, String>> list = new ArrayList<>();

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);

			pstmt = connection.prepareStatement(
					"SELECT " +
					"    d.intake_date, " +
					"    COUNT(ms.no) AS total, " +                              // 해당 날짜 전체 스케줄 수
					"    SUM(CASE WHEN mi.is_taken = 1 THEN 1 ELSE 0 END) AS taken " + // 실제 복용 수
					"FROM ( " +
					// 해당 월의 1일부터 오늘까지 날짜 생성 (최대 31일 × 4 = 0~39 범위)
					"    SELECT DATE(?) + INTERVAL (seq) DAY AS intake_date " +
					"    FROM ( " +
					"        SELECT a.N + b.N * 10 AS seq " +
					"        FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
					"              UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a, " +
					"             (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) b " +
					"    ) nums " +
					"    WHERE DATE(?) + INTERVAL (seq) DAY <= LEAST(LAST_DAY(?), CURDATE()) " + // 오늘 이전까지만
					") d " +
					"JOIN medicine m ON m.user_no = ? AND m.del = 'N' " +       // 삭제된 약 제외
					"JOIN medicine_schedule ms ON ms.medicine_no = m.no AND ms.weekday = WEEKDAY(d.intake_date) " +
					"LEFT JOIN medicine_intake mi ON mi.schedule_no = ms.no AND mi.intake_date = d.intake_date " +
					"GROUP BY d.intake_date"
			);

			// 해당 월의 첫째 날 (예: "2025-05-01")
			String firstDay = year + "-" + String.format("%02d", month) + "-01";
			pstmt.setString(1, firstDay); // 시작 날짜
			pstmt.setString(2, firstDay); // 날짜 범위 필터용
			pstmt.setString(3, firstDay); // LAST_DAY 계산용
			pstmt.setLong(4, userNo);     // 사용자 번호

			rs = pstmt.executeQuery();

			while (rs.next()) {
				String date  = rs.getString("intake_date");
				int    total = rs.getInt("total");
				int    taken = rs.getInt("taken");

				// 전체 복용했으면 "all", 하나라도 미복용이면 "partial"
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

	// ==========================================================
	// 약 삭제 (Soft Delete 방식)
	// 실제 DB에서 삭제하지 않고 del='Y'로 마킹
	// → 이미 등록된 복용 기록 보존 가능
	// ==========================================================
	public void deleteMedicine(long medicineNo) {
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);
			connection.setAutoCommit(false); // 트랜잭션 시작

			// del='Y': 삭제 처리 (모든 SELECT 쿼리에서 del='N' 조건으로 필터링됨)
			pstmt = connection.prepareStatement(
					"UPDATE medicine SET del='Y' WHERE no=?");
			pstmt.setLong(1, medicineNo);
			pstmt.executeUpdate();

			connection.commit(); // 성공 시 커밋

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (connection != null) connection.rollback(); // 실패 시 롤백
			} catch (Exception re) {
				re.printStackTrace();
			}
		} finally {
			close(rs, pstmt, connection);
		}
	}

	// ==========================================================
	// 전체 알람 목록 조회 (로그인 후 알람 복원용)
	// - 해당 계정의 삭제되지 않은(del='N') 약 중 alarm_enabled=1인 스케줄 전체 반환
	// - 앱에서 AlarmHelper.setAlarm()을 반복 호출해 AlarmManager에 재등록
	// 반환: [medicine_no, name, weekday, intake_time_type, intake_time, intake_type]
	// ==========================================================
	public List<MedicineModel> getAllAlarms(long userNo) {
		List<MedicineModel> list = new ArrayList<>();

		String sql = "SELECT m.no AS medicine_no, m.name AS medicine_name, " +
				"ms.weekday, ms.intake_time_type, ms.intake_time, ms.intake_type " +
				"FROM medicine m " +
				"JOIN medicine_schedule ms ON ms.medicine_no = m.no " +
				"WHERE m.user_no = ? AND m.del = 'N' AND ms.alarm_enabled = 1 " +
				"ORDER BY ms.weekday, ms.intake_time";

		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(jdbcUrl, id, password);
			pstmt = connection.prepareStatement(sql);

			pstmt.setLong(1, userNo); // 사용자 번호

			rs = pstmt.executeQuery();

			while (rs.next()) {
				MedicineModel model = new MedicineModel();
				model.setNo(rs.getLong("medicine_no"));              // 약 번호
				model.setName(rs.getString("medicine_name"));        // 약 이름
				model.setWeekDay(rs.getInt("weekday"));              // 요일 (0:월 ~ 6:일)
				model.setIntakeTimeType(rs.getString("intake_time_type")); // 아침/점심/저녁/취침 전
				model.setIntakeTime(rs.getString("intake_time"));    // "HH:mm:ss" (앱에서 HH:mm으로 자름)
				model.setIntakeType(rs.getString("intake_type"));    // 식전/식후
				list.add(model);
			}

			System.out.println("getAllAlarms - userNo=" + userNo + ", count=" + list.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, pstmt, connection);
		}
		return list;
	}

	// ==========================================================
	// DB 리소스 정리 (ResultSet → PreparedStatement → Connection 순으로 닫기)
	// finally 블록에서 항상 호출하여 커넥션 누수 방지
	// ==========================================================
	public void close(ResultSet rs, PreparedStatement pstmt, Connection conn) {

		if (rs != null) {
			try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
		}
		if (pstmt != null) {
			try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
		}
		if (conn != null) {
			try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
		}
	}
}
