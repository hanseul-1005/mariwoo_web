package windy.mariwoo.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import windy.mariwoo.dao.UserDAO;
import windy.mariwoo.model.MedicineCardItem;
import windy.mariwoo.model.MedicineModel;
import windy.mariwoo.model.MedicineScheduleItem;
import windy.mariwoo.model.UserModel;
import windy.mariwoo.dao.MedicineDAO;

/**
 * 약 관련 REST API 서블릿
 * URL: /medicine.windy
 *
 * cmd 파라미터로 기능 분기:
 *   list                - 약 목록 조회 (요일별)
 *   add_medicine        - 약 등록
 *   get_medicine_detail - 약 상세 조회 (수정 화면용)
 *   modify_medicine     - 약 스케줄 수정
 *   get_intake_list     - 복용 체크 목록 조회
 *   check_intake        - 복용 체크 저장
 *   relation_list       - 가족 관계 + 약 목록 조회
 *   get_family_intake_list - 가족 복용 내역 조회
 *   get_calendar_intake - 달력 복용 현황 조회
 *   delete_medicine     - 약 삭제 (soft delete)
 */
@WebServlet(description = "RestMedicine", urlPatterns = { "/medicine.windy" })
public class RestMedicine extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public RestMedicine() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 요청/응답 인코딩 설정 (한글 깨짐 방지)
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");

		MedicineDAO mDao = new MedicineDAO();

		String cmd = request.getParameter("cmd");
		System.out.println("rest cmd : " + cmd);

		// ========================================================
		// 약 목록 조회 (요일별)
		// 요청: no(user_no), week_day
		// 응답: { result, listName: [{name, medicine_no, listMedicine: [...]}] }
		// ========================================================
		if ("list".equals(cmd)) {
			try {
				long no      = Long.parseLong(request.getParameter("no"));
				int  weekDay = Integer.parseInt(request.getParameter("week_day"));

				System.out.println("no : " + no);
				System.out.println("weekDay : " + weekDay);

				MedicineModel modelParam = new MedicineModel();
				modelParam.setUserNo(no);
				modelParam.setWeekDay(weekDay);

				List<MedicineModel> listMedicine = mDao.selectListMedicine(modelParam);

				JSONArray jArr = new JSONArray();
				for (int i = 0; i < listMedicine.size(); i++) {
					JSONObject jObj  = new JSONObject();
					JSONArray  jArr2 = new JSONArray();

					jObj.put("name",        listMedicine.get(i).getName());
					jObj.put("medicine_no", listMedicine.get(i).getNo());

					// 스케줄 목록 (시간대별)
					for (int j = 0; j < listMedicine.get(i).getListMedicine().size(); j++) {
						MedicineModel medicine = listMedicine.get(i).getListMedicine().get(j);

						JSONObject jObj2 = new JSONObject();
						jObj2.put("schedule_no",      String.valueOf(medicine.getScheduleNo()));
						jObj2.put("intake_time",      medicine.getIntakeType());
						jObj2.put("intake_time_type", medicine.getIntakeTimeType());
						jArr2.add(jObj2);
					}
					jObj.put("listMedicine", jArr2);
					jArr.add(jObj);
				}

				JSONObject json   = new JSONObject();
				String     result = (jArr.size() > 0) ? "true" : "false";

				System.out.println("result : " + result);
				json.put("listName", jArr);
				json.put("result",   result);

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(json);

			} catch (Exception e) {
				e.printStackTrace();
				JSONObject errJson = new JSONObject();
				errJson.put("result", "false");
				errJson.put("listName", new JSONArray());
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(errJson);
			}
		}

		// ========================================================
		// 약 등록
		// 요청: user_no, name, weekday(쉼표구분), intake_time_type1~4, intake_time1~4, intake_type1~4
		// 응답: { result, medicine_no }
		// ========================================================
		else if ("add_medicine".equals(cmd)) {

			long   userNo         = Long.parseLong(request.getParameter("user_no"));
			String name           = request.getParameter("name");
			String strWeekday     = request.getParameter("weekday"); // 예: "0,2,4" (월,수,금)
			String intakeTimeType1 = request.getParameter("intake_time_type1");
			String intakeTime1    = request.getParameter("intake_time1");
			String intakeType1    = request.getParameter("intake_type1");
			String intakeTimeType2 = request.getParameter("intake_time_type2");
			String intakeTime2    = request.getParameter("intake_time2");
			String intakeType2    = request.getParameter("intake_type2");
			String intakeTimeType3 = request.getParameter("intake_time_type3");
			String intakeTime3    = request.getParameter("intake_time3");
			String intakeType3    = request.getParameter("intake_type3");
			String intakeTimeType4 = request.getParameter("intake_time_type4");
			String intakeTime4    = request.getParameter("intake_time4");
			String intakeType4    = request.getParameter("intake_type4");

			// 1. 약 기본 정보 등록 (medicine 테이블)
			MedicineModel medicine = new MedicineModel();
			medicine.setUserNo(userNo);
			medicine.setName(name);
			long no = mDao.insertMedicine(medicine); // 등록 후 PK 반환

			// 요일 파싱: "0,2,4" → [0, 2, 4]
			String[] days = strWeekday.split(",");

			// 시간대별 데이터 배열로 관리
			String[] intakeTimeTypes = { intakeTimeType1, intakeTimeType2, intakeTimeType3, intakeTimeType4 };
			String[] intakeTimes     = { intakeTime1,     intakeTime2,     intakeTime3,     intakeTime4     };
			String[] intakeTypes     = { intakeType1,     intakeType2,     intakeType3,     intakeType4     };

			// 2. 선택된 요일 × 입력된 시간대 조합으로 스케줄 INSERT
			for (String day : days) {
				if (day == null || day.trim().isEmpty()) continue;

				int weekDay = Integer.parseInt(day.trim());

				for (int i = 0; i < 4; i++) {
					if (intakeTimes[i] == null || intakeTimes[i].isEmpty()) continue; // 비어있는 시간대 skip

					MedicineModel schedule = new MedicineModel();
					schedule.setNo(no);                    // 방금 등록한 약 번호
					schedule.setWeekDay(weekDay);
					schedule.setIntakeTimeType(intakeTimeTypes[i]);
					schedule.setIntakeTime(intakeTimes[i]);
					schedule.setIntakeType(intakeTypes[i]);

					mDao.insertSchedule(schedule);
				}
			}

			// 3. 결과 반환 (medicine_no를 앱으로 전달 → 알람 등록에 사용)
			JSONObject json = new JSONObject();
			json.put("result",      "true");
			json.put("medicine_no", no); // 앱에서 AlarmHelper.setAlarm() 호출에 필요

			response.setContentType("text/html; charset=utf-8");
			response.getWriter().print(json);
		}

		// ========================================================
		// 약 상세 조회 (수정 화면 진입 시 기존 데이터 불러오기)
		// 요청: medicine_no, weekday
		// 응답: { result, list: [{no, weekday, intake_time_type, intake_time, intake_type}] }
		// ========================================================
		else if ("get_medicine_detail".equals(cmd)) {
			try {
				long medicineNo = Long.parseLong(request.getParameter("medicine_no"));
				int  weekday    = Integer.parseInt(request.getParameter("weekday"));

				List<MedicineModel> list = mDao.getMedicineDetail(medicineNo, weekday);

				JSONArray jArr = new JSONArray();
				for (MedicineModel model : list) {
					JSONObject jObj = new JSONObject();
					jObj.put("no",               model.getScheduleNo()); // 스케줄 PK (수정 시 UPDATE 대상)
					jObj.put("weekday",          model.getWeekDay());
					jObj.put("intake_time_type", model.getIntakeTimeType());
					jObj.put("intake_time",      model.getIntakeTime());
					jObj.put("intake_type",      model.getIntakeType());
					jArr.add(jObj);
				}

				JSONObject json = new JSONObject();
				json.put("result", jArr.size() > 0 ? "true" : "false");
				json.put("list",   jArr);

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(json);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시에도 반드시 JSON 응답 반환 (빈 body 방지)
				JSONObject errJson = new JSONObject();
				errJson.put("result", "false");
				errJson.put("message", e.getMessage());
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(errJson);
			}
		}

		// ========================================================
		// 약 스케줄 수정
		// 요청: medicine_no, weekday, intake_time_type1~4, intake_time1~4, intake_type1~4,
		//       no1~4(기존 스케줄 PK, -1이면 신규), delete_nos(삭제할 스케줄 PK 목록)
		// 응답: { result }
		// ========================================================
		else if ("modify_medicine".equals(cmd)) {
			try {
				String medicineNoStr = request.getParameter("medicine_no");
				String strWeekday    = request.getParameter("weekday");

				// 필수 파라미터 누락 체크
				if (medicineNoStr == null || strWeekday == null || strWeekday.trim().isEmpty()) {
					JSONObject json = new JSONObject();
					json.put("result",  "false");
					json.put("message", "필수 파라미터 누락");
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print(json);
					return;
				}

				long medicineNo = Long.parseLong(medicineNoStr);
				int  weekday    = Integer.parseInt(strWeekday.trim());

				// 4개 시간대(아침/점심/저녁/취침 전) 데이터
				String[] intakeTimeTypes = {
						request.getParameter("intake_time_type1"),
						request.getParameter("intake_time_type2"),
						request.getParameter("intake_time_type3"),
						request.getParameter("intake_time_type4")
				};
				String[] intakeTimes = {
						request.getParameter("intake_time1"),
						request.getParameter("intake_time2"),
						request.getParameter("intake_time3"),
						request.getParameter("intake_time4")
				};
				String[] intakeTypes = {
						request.getParameter("intake_type1"),
						request.getParameter("intake_type2"),
						request.getParameter("intake_type3"),
						request.getParameter("intake_type4")
				};

				// 기존 스케줄 PK (-1이면 신규 INSERT, 그 외는 UPDATE)
				long[] nos = {
						parseLongSafe(request.getParameter("no1")),
						parseLongSafe(request.getParameter("no2")),
						parseLongSafe(request.getParameter("no3")),
						parseLongSafe(request.getParameter("no4"))
				};

				// 삭제 대상 스케줄 처리 (시간 지운 기존 항목)
				String deleteNosStr = request.getParameter("delete_nos");
				if (deleteNosStr != null && !deleteNosStr.trim().isEmpty()) {
					for (String noStr : deleteNosStr.split(",")) {
						long deleteNo = parseLongSafe(noStr.trim());
						if (deleteNo != -1) {
							mDao.deleteScheduleByNo(deleteNo); // 스케줄 단건 삭제
						}
					}
				}

				List<MedicineModel> insertList = new ArrayList<>(); // 새로 추가할 시간대
				List<MedicineModel> updateList = new ArrayList<>(); // 기존 시간대 수정

				for (int i = 0; i < 4; i++) {
					if (intakeTimes[i] == null || intakeTimes[i].isEmpty()) continue;

					if (nos[i] == -1) {
						// 기존 스케줄 없음 → 신규 INSERT
						MedicineModel schedule = new MedicineModel();
						schedule.setNo(medicineNo);
						schedule.setWeekDay(weekday);
						schedule.setIntakeTimeType(intakeTimeTypes[i]);
						schedule.setIntakeTime(intakeTimes[i]);
						schedule.setIntakeType(intakeTypes[i] != null ? intakeTypes[i] : "");
						insertList.add(schedule);
					} else {
						// 기존 스케줄 존재 → 스케줄 PK 기준으로 UPDATE
						MedicineModel schedule = new MedicineModel();
						schedule.setScheduleNo(nos[i]); // 스케줄 PK
						schedule.setIntakeTime(intakeTimes[i]);
						schedule.setIntakeType(intakeTypes[i] != null ? intakeTypes[i] : "");
						updateList.add(schedule);
					}
				}

				boolean result = mDao.updateSchedule(medicineNo, insertList, updateList);

				JSONObject json = new JSONObject();
				json.put("result", result ? "true" : "false");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(json);

			} catch (Exception e) {
				e.printStackTrace();
				try {
					JSONObject errJson = new JSONObject();
					errJson.put("result", "false");
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print(errJson);
				} catch (Exception ignored) {}
			}
		}

		// ========================================================
		// 복용 체크 목록 조회 (날짜/요일별)
		// 요청: user_no, date, weekday
		// 응답: [{medicine_name, schedules: [{schedule_no, intake_time_type, ...}]}]
		// ========================================================
		else if ("get_intake_list".equals(cmd)) {
			try {
				long   userNo  = Long.parseLong(request.getParameter("user_no"));
				String date    = request.getParameter("date");    // yyyy-MM-dd
				int    weekday = Integer.parseInt(request.getParameter("weekday")); // 0:월 ~ 6:일

				List<MedicineCardItem> cardList = mDao.getIntakeList(userNo, date, weekday);

				JSONArray jsonArray = new JSONArray();

				for (MedicineCardItem card : cardList) {
					JSONObject cardJson = new JSONObject();
					cardJson.put("medicine_name", card.getMedicineName());

					JSONArray scheduleArray = new JSONArray();
					for (MedicineScheduleItem schedule : card.getSchedules()) {
						JSONObject scheduleJson = new JSONObject();
						scheduleJson.put("schedule_no",      schedule.getScheduleNo());
						scheduleJson.put("intake_time_type", schedule.getIntakeTimeType());
						scheduleJson.put("intake_type",      schedule.getIntakeType());
						scheduleJson.put("intake_time",      schedule.getIntakeTime());
						scheduleJson.put("is_taken",         schedule.isTaken() ? 1 : 0);
						scheduleArray.add(scheduleJson);
					}
					cardJson.put("schedules", scheduleArray);
					jsonArray.add(cardJson);
				}

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(jsonArray);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시에도 빈 배열 반환 (빈 body 방지)
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(new JSONArray());
			}
		}

		// ========================================================
		// 복용 체크 저장 (복용 완료 시 호출)
		// 요청: schedule_no, date, is_taken(1=복용/0=미복용)
		// 응답: { result }
		// ========================================================
		else if ("check_intake".equals(cmd)) {
			try {
				long   scheduleNo = Long.parseLong(request.getParameter("schedule_no"));
				String date       = request.getParameter("date");
				int    isTaken    = Integer.parseInt(request.getParameter("is_taken"));

				mDao.checkIntake(scheduleNo, date, isTaken);

				JSONObject json = new JSONObject();
				json.put("result", "true");

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(json);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시에도 JSON 응답 반환
				JSONObject errJson = new JSONObject();
				errJson.put("result", "false");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(errJson);
			}
		}

		// ========================================================
		// 가족 관계 + 약 목록 조회 (관계 탭)
		// 요청: no(user_no), target_no, date
		// 응답: { result, listName(약목록), listTarget(가족목록) }
		// ========================================================
		else if ("relation_list".equals(cmd)) {
			long   userNo   = Long.parseLong(request.getParameter("no"));
			long   targetNo = Long.parseLong(request.getParameter("target_no"));
			String date     = request.getParameter("date");

			MedicineModel medicine = new MedicineModel();
			medicine.setUserNo(targetNo);
			medicine.setIntakeDate(date);

			UserDAO uDao = new UserDAO();

			// 열람 관계인 가족 목록 조회
			List<UserModel> listUser  = uDao.selectListRelation(userNo, "대상자");
			JSONArray       jArrUser  = new JSONArray();
			for (int j = 0; j < listUser.size(); j++) {
				UserModel  user = listUser.get(j);
				JSONObject jObj = new JSONObject();
				jObj.put("no",   user.getNo());
				jObj.put("name", user.getName());
				jArrUser.add(jObj);
			}

			// 대상자의 약 목록 조회
			List<MedicineModel> listMedicine = mDao.selectListMedicine(medicine);

			JSONArray jArrMedicine = new JSONArray();
			for (int i = 0; i < listMedicine.size(); i++) {
				JSONObject jObj  = new JSONObject();
				JSONArray  jArr2 = new JSONArray();

				jObj.put("name", listMedicine.get(i).getName());

				for (int j = 0; j < listMedicine.get(i).getListMedicine().size(); j++) {
					MedicineModel mModel = listMedicine.get(i).getListMedicine().get(j);

					JSONObject jObj2 = new JSONObject();
					jObj2.put("schedule_no",      String.valueOf(mModel.getScheduleNo()));
					jObj2.put("intake_time",      mModel.getIntakeType());
					jObj2.put("intake_time_type", mModel.getIntakeTimeType());
					jArr2.add(jObj2);
				}
				jObj.put("listMedicine", jArr2);
				jArrMedicine.add(jObj);
			}

			JSONObject json = new JSONObject();
			System.out.println("jArrUser : " + jArrUser.size());
			json.put("listName",   jArrMedicine);
			json.put("listTarget", jArrUser);
			json.put("result",     "true");

			response.setContentType("text/html; charset=utf-8");
			response.getWriter().print(json);
		}

		// ========================================================
		// 가족 복용 내역 조회 (열람자가 대상자의 복용 현황 확인)
		// 요청: target_no, date, weekday
		// 응답: [{medicine_name, schedules: [{schedule_no, ...}]}]
		// ========================================================
		else if ("get_family_intake_list".equals(cmd)) {
			try {
				long   targetNo = Long.parseLong(request.getParameter("target_no"));
				String date     = request.getParameter("date");
				int    weekday  = Integer.parseInt(request.getParameter("weekday"));

				List<MedicineCardItem> cardList = mDao.getFamilyIntakeList(targetNo, date, weekday);

				JSONArray jsonArray = new JSONArray();
				for (MedicineCardItem card : cardList) {
					JSONObject cardJson = new JSONObject();
					cardJson.put("medicine_name", card.getMedicineName());

					JSONArray scheduleArray = new JSONArray();
					for (MedicineScheduleItem schedule : card.getSchedules()) {
						JSONObject scheduleJson = new JSONObject();
						scheduleJson.put("schedule_no",      schedule.getScheduleNo());
						scheduleJson.put("intake_time_type", schedule.getIntakeTimeType());
						scheduleJson.put("intake_type",      schedule.getIntakeType());
						scheduleJson.put("intake_time",      schedule.getIntakeTime());
						scheduleJson.put("is_taken",         schedule.isTaken() ? 1 : 0);
						scheduleArray.add(scheduleJson);
					}
					cardJson.put("schedules", scheduleArray);
					jsonArray.add(cardJson);
				}

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(jsonArray);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시 빈 배열 반환
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(new JSONArray());
			}
		}

		// ========================================================
		// 달력 복용 현황 조회 (월별)
		// 요청: user_no, year, month
		// 응답: [{date, status("all"/"partial")}]
		// ========================================================
		else if ("get_calendar_intake".equals(cmd)) {
			try {
				long userNo = Long.parseLong(request.getParameter("user_no"));
				int  year   = Integer.parseInt(request.getParameter("year"));
				int  month  = Integer.parseInt(request.getParameter("month"));

				List<Map<String, String>> calendarList = mDao.getCalendarIntake(userNo, year, month);

				JSONArray jsonArray = new JSONArray();
				for (Map<String, String> item : calendarList) {
					JSONObject obj = new JSONObject();
					obj.put("date",   item.get("date"));   // "2025-05-01"
					obj.put("status", item.get("status")); // "all" or "partial"
					jsonArray.add(obj);
				}

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(jsonArray);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시 빈 배열 반환
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(new JSONArray());
			}
		}

		// ========================================================
		// 약 삭제 (Soft Delete)
		// 요청: medicine_no
		// 응답: { result }
		// del='Y'로 마킹 → 모든 조회에서 자동 필터링됨
		// ========================================================
		else if ("delete_medicine".equals(cmd)) {
			try {
				long medicineNo = Long.parseLong(request.getParameter("medicine_no"));

				mDao.deleteMedicine(medicineNo);

				JSONObject json = new JSONObject();
				json.put("result", "true");

				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(json);

			} catch (Exception e) {
				e.printStackTrace();
				// 예외 발생 시에도 JSON 응답 반환 (빈 body 방지)
				JSONObject errJson = new JSONObject();
				errJson.put("result", "false");
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(errJson);
			}
		}
	}

	/**
	 * String → long 안전 변환 헬퍼
	 * 파싱 실패 시 -1 반환 (신규 INSERT 판별에 사용)
	 */
	private long parseLongSafe(String val) {
		try { return Long.parseLong(val); }
		catch (Exception e) { return -1L; }
	}
}
