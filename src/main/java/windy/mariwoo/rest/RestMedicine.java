package windy.mariwoo.rest;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
 * Servlet implementation class Rest
 */
@WebServlet(description = "RestMedicine", urlPatterns = { "/medicine.windy" })
public class RestMedicine extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestMedicine() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");  // 요청 인코딩 설정 (이게 빠지면 받는 쪽이 깨짐)
		response.setContentType("application/json; charset=UTF-8");  // 응답 인코딩 설정

		MedicineDAO mDao = new MedicineDAO();
		
		String cmd = request.getParameter("cmd");
		System.out.println("rest cmd : "+cmd);
		
		if("list".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			int weekDay = Integer.parseInt(request.getParameter("week_day"));
			
			System.out.println("no : "+no);
			System.out.println("weekDay : "+weekDay);
			
			MedicineModel moedlParam = new MedicineModel();
			moedlParam.setUserNo(no);
			moedlParam.setWeekDay(weekDay);

			List<MedicineModel> listMedicine = mDao.selectListMedicine(moedlParam);

			JSONArray jArr = new JSONArray();
			for(int i=0; i<listMedicine.size(); i++) {

				JSONObject jObj = new JSONObject();
				JSONArray jArr2 = new JSONArray();
				
				jObj.put("name", listMedicine.get(i).getName());
				jObj.put("medicine_no", listMedicine.get(i).getNo());
				
				for(int j=0; j<listMedicine.get(i).getListMedicine().size(); j++) {

					MedicineModel medicine = listMedicine.get(i).getListMedicine().get(j);
					
					JSONObject jObj2 = new JSONObject();
					jObj2.put("schedule_no", String.valueOf(medicine.getScheduleNo()));
					jObj2.put("intake_time", medicine.getIntakeType());
					jObj2.put("intake_time_type", medicine.getIntakeTimeType());

					jArr2.add(jObj2);
				}
				jObj.put("listMedicine", jArr2);
				
				jArr.add(jObj);
			}
			
			JSONObject json = new JSONObject();
			
			String result = "false";
			if(0<jArr.size()) {
				result = "true";
			}

			System.out.println("result : "+result);
			response.setContentType("text/json; charset=utf-8");
			json.put("listName", jArr);
			json.put("result", result);

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("add_medicine".equals(cmd)) {

		    long userNo = Long.parseLong(request.getParameter("user_no"));
		    String name = request.getParameter("name");
		    String strWeekday = request.getParameter("weekday"); // 예: "0,2,4"
		    String intakeTimeType1 = request.getParameter("intake_time_type1");
		    String intakeTime1 = request.getParameter("intake_time1");
		    String intakeType1 = request.getParameter("intake_type1");
		    String intakeTimeType2 = request.getParameter("intake_time_type2");
		    String intakeTime2 = request.getParameter("intake_time2");
		    String intakeType2 = request.getParameter("intake_type2");
		    String intakeTimeType3 = request.getParameter("intake_time_type3");
		    String intakeTime3 = request.getParameter("intake_time3");
		    String intakeType3 = request.getParameter("intake_type3");
		    String intakeTimeType4 = request.getParameter("intake_time_type4");
		    String intakeTime4 = request.getParameter("intake_time4");
		    String intakeType4 = request.getParameter("intake_type4");

		    // 약 이름 먼저 등록
		    MedicineModel medicine = new MedicineModel();
		    medicine.setUserNo(userNo);
		    medicine.setName(name);
		    long no = mDao.insertMedicine(medicine); // 등록된 약 no 반환

		    // 요일 파싱 (쉼표 구분 "0,2,4" → [0, 2, 4])
		    String[] days = strWeekday.split(",");

		    // 시간대별 데이터 배열로 관리
		    String[] intakeTimeTypes = {intakeTimeType1, intakeTimeType2, intakeTimeType3, intakeTimeType4};
		    String[] intakeTimes     = {intakeTime1,     intakeTime2,     intakeTime3,     intakeTime4};
		    String[] intakeTypes     = {intakeType1,      intakeType2,     intakeType3,     intakeType4};

		    for (String day : days) {
		        // ✅ 이 조건 추가
		        if (day == null || day.trim().isEmpty()) continue;
		        
		        int weekDay = Integer.parseInt(day.trim());

		        for (int i = 0; i < 4; i++) {
		            if (intakeTimes[i] == null || intakeTimes[i].isEmpty()) continue;

		            MedicineModel schedule = new MedicineModel();
		            schedule.setNo(no);
		            schedule.setWeekDay(weekDay);
		            schedule.setIntakeTimeType(intakeTimeTypes[i]);
		            schedule.setIntakeTime(intakeTimes[i]);
		            schedule.setIntakeType(intakeTypes[i]);

		            mDao.insertSchedule(schedule);
		        }
		    }

		    // 결과 반환
		    JSONObject json = new JSONObject();
		    json.put("result", "true");

		    response.setContentType("text/html; charset=utf-8");
		    PrintWriter out = response.getWriter();
		    out.print(json);
			
		}
		
		else if ("get_medicine_detail".equals(cmd)) {
		    try {
		        long medicineNo = Long.parseLong(request.getParameter("medicine_no"));
		        int weekday     = Integer.parseInt(request.getParameter("weekday"));

		        List<MedicineModel> list = mDao.getMedicineDetail(medicineNo, weekday);

		        JSONArray jArr = new JSONArray();
		        for (MedicineModel model : list) {
		            JSONObject jObj = new JSONObject();
		            jObj.put("no", model.getScheduleNo()); // ✅ getNo() → getScheduleNo()
		            jObj.put("weekday", model.getWeekDay());
		            jObj.put("intake_time_type", model.getIntakeTimeType());
		            jObj.put("intake_time", model.getIntakeTime());
		            jObj.put("intake_type", model.getIntakeType());
		            jArr.add(jObj);
		        }

		        JSONObject json = new JSONObject();
		        json.put("result", jArr.size() > 0 ? "true" : "false");
		        json.put("list", jArr);

		        response.setContentType("application/json; charset=utf-8");
		        PrintWriter out = response.getWriter();
		        out.print(json);

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		// 약 스케줄 수정
		else if ("modify_medicine".equals(cmd)) {
		    try {
		        String medicineNoStr = request.getParameter("medicine_no");
		        String strWeekday    = request.getParameter("weekday");

		        if (medicineNoStr == null || strWeekday == null || strWeekday.trim().isEmpty()) {
		            JSONObject json = new JSONObject();
		            json.put("result", "false");
		            json.put("message", "필수 파라미터 누락");
		            response.setContentType("application/json; charset=utf-8");
		            response.getWriter().print(json);
		            return;
		        }

		        long medicineNo = Long.parseLong(medicineNoStr);
		        int  weekday    = Integer.parseInt(strWeekday.trim());

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

		        // ✅ 기존 스케줄 no (-1이면 신규 INSERT, 그 외 UPDATE)
		        long[] nos = {
		            parseLongSafe(request.getParameter("no1")),
		            parseLongSafe(request.getParameter("no2")),
		            parseLongSafe(request.getParameter("no3")),
		            parseLongSafe(request.getParameter("no4"))
		        };

		        // ✅ 삭제 대상 처리
		        String deleteNosStr = request.getParameter("delete_nos");
		        if (deleteNosStr != null && !deleteNosStr.trim().isEmpty()) {
		            for (String noStr : deleteNosStr.split(",")) {
		                long deleteNo = parseLongSafe(noStr.trim());
		                if (deleteNo != -1) {
		                    mDao.deleteScheduleByNo(deleteNo);
		                }
		            }
		        }

		        List<MedicineModel> insertList = new ArrayList<>();
		        List<MedicineModel> updateList = new ArrayList<>();

		        for (int i = 0; i < 4; i++) {
		            if (intakeTimes[i] == null || intakeTimes[i].isEmpty()) continue;

		            if (nos[i] == -1) {
		                // ✅ 신규 시간대 → 해당 요일 단일 INSERT
		                MedicineModel schedule = new MedicineModel();
		                schedule.setNo(medicineNo);              // ✅ 약 번호
		                schedule.setWeekDay(weekday);
		                schedule.setIntakeTimeType(intakeTimeTypes[i]);
		                schedule.setIntakeTime(intakeTimes[i]);
		                schedule.setIntakeType(intakeTypes[i] != null ? intakeTypes[i] : "");
		                insertList.add(schedule);
		            } else {
		                // ✅ 기존 시간대 수정 → scheduleNo(PK) 기준 UPDATE
		                MedicineModel schedule = new MedicineModel();
		                schedule.setScheduleNo(nos[i]);          // ✅ 스케줄 PK
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
		else if ("get_intake_list".equals(cmd)) {
		    try {
		        long userNo = Long.parseLong(request.getParameter("user_no"));
		        String date = request.getParameter("date");
		        int weekday = Integer.parseInt(request.getParameter("weekday"));

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
		        PrintWriter out = response.getWriter();
		        out.print(jsonArray);

		    } catch (Exception  e) {
		        e.printStackTrace();
		    }
		}

		else if ("check_intake".equals(cmd)) {
		    try {
		        long scheduleNo = Long.parseLong(request.getParameter("schedule_no"));
		        String date     = request.getParameter("date");
		        int isTaken     = Integer.parseInt(request.getParameter("is_taken"));

		        mDao.checkIntake(scheduleNo, date, isTaken);

		        JSONObject json = new JSONObject();
		        json.put("result", "true");

		        response.setContentType("application/json; charset=utf-8");
		        PrintWriter out = response.getWriter();
		        out.print(json);

		    } catch (Exception  e) {
		        e.printStackTrace();
		    }
		}
		else if("relation_list".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("no"));
			long targetNo = Long.parseLong(request.getParameter("target_no"));
			String date = request.getParameter("date");
			
			MedicineModel medicine = new MedicineModel();
			medicine.setUserNo(targetNo);
			medicine.setIntakeDate(date);
			
			UserDAO uDao = new UserDAO();
			
			List<UserModel> listUser = uDao.selectListRelation(userNo, "대상자");
			JSONArray jArrUser = new JSONArray();
			for(int j=0; j<listUser.size(); j++) {
				
				UserModel user = listUser.get(j);

				JSONObject jObj = new JSONObject();
				jObj.put("no", user.getNo());
				jObj.put("name", user.getName());
			
				jArrUser.add(jObj);
				
			}
			

			List<MedicineModel> listMedicine = mDao.selectListMedicine(medicine);

			JSONArray jArrMedicine = new JSONArray();
			for(int i=0; i<listMedicine.size(); i++) {

				JSONObject jObj = new JSONObject();
				JSONArray jArr2 = new JSONArray();
				
				jObj.put("name", listMedicine.get(i).getName());
				
				for(int j=0; j<listMedicine.get(i).getListMedicine().size(); j++) {

					MedicineModel mModel = listMedicine.get(i).getListMedicine().get(j);
					
					JSONObject jObj2 = new JSONObject();
					jObj2.put("schedule_no", String.valueOf(mModel.getScheduleNo()));
					jObj2.put("intake_time", mModel.getIntakeType());
					jObj2.put("intake_time_type", mModel.getIntakeTimeType());

					jArr2.add(jObj2);
				}
				jObj.put("listMedicine", jArr2);
				
				jArrMedicine.add(jObj);
			}
			
			JSONObject json = new JSONObject();
			
			String result = "false";
			if(0<jArrMedicine.size()) {
				result = "true";
			}
			
			System.out.println("jArrUser : "+jArrUser.size());
			System.out.println("result : "+result);
			response.setContentType("text/json; charset=utf-8");
			json.put("listName", jArrMedicine);
			json.put("listTarget", jArrUser);
			json.put("result", "true");

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
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
		    }
		}
	}

	// ✅ 헬퍼 메서드
	private long parseLongSafe(String val) {
	    try { return Long.parseLong(val); }
	    catch (Exception e) { return -1L; }
	}
}
