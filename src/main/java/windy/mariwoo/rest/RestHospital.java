package windy.mariwoo.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import windy.mariwoo.dao.HospitalScheduleDAO;
import windy.mariwoo.model.HospitalScheduleModel;
import windy.mariwoo.model.UserModel;

/**
 * Servlet implementation class RestHospital
 */
@WebServlet(description = "RestHospital", urlPatterns = { "/hospital.windy" })
public class RestHospital extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestHospital() {
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

		HospitalScheduleDAO hDao = new HospitalScheduleDAO();
		
		String cmd = request.getParameter("cmd");
		System.out.println("rest cmd : "+cmd);
		
		if("list".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("user_no"));
			String startAt = request.getParameter("start_at");
			String endAt = request.getParameter("end_at");
			
			HospitalScheduleModel schedule = new HospitalScheduleModel();
			schedule.setUserNo(userNo);
			schedule.setStartAt(startAt);
			schedule.setEndAt(endAt);
			
			List<HospitalScheduleModel> listSchedule = hDao.selectListSchedule(schedule);
			
			JSONArray jArr = new JSONArray();
			
			for(int i=0; i<listSchedule.size(); i++) {
				HospitalScheduleModel sch = listSchedule.get(i);

				JSONObject jObj = new JSONObject();
				jObj.put("no", sch.getNo());
				jObj.put("name", sch.getName());
				jObj.put("time", sch.getTime());
				jObj.put("memo", sch.getMemo());
				
				jArr.add(jObj);
			}

			JSONObject json = new JSONObject();
			
			String result = "false";
			if(0<jArr.size()) {
				result = "true";
			}

			System.out.println("result : "+result);
			response.setContentType("text/json; charset=utf-8");
			json.put("listSchedule", jArr);
			json.put("result", result);

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		else if("detail".equals(cmd)) {
			
			long no = Long.parseLong(request.getParameter("no"));

			HospitalScheduleModel schedule = hDao.selectSchedule(no);
			
			JSONObject json = new JSONObject();
			json.put("name", schedule.getName());
			json.put("time", schedule.getTime());
			json.put("notification_time", schedule.getNotificationTime());
			json.put("memo", schedule.getMemo());

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		else if("add".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("user_no"));
			String name = request.getParameter("name");
			String time = request.getParameter("time");
			String notificationTime = request.getParameter("notification_time");
			String memo = request.getParameter("memo");
			 
			HospitalScheduleModel schedule = new HospitalScheduleModel();
			schedule.setUserNo(userNo);
			schedule.setName(name);
			schedule.setTime(time);
			schedule.setNotificationTime(notificationTime);
			schedule.setMemo(memo);
			
			boolean check = hDao.insertHospitalSchedule(schedule);
			
			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		else if("modify".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			String name = request.getParameter("name");
			String time = request.getParameter("time");
			String notificationTime = request.getParameter("notification_time");
			String memo = request.getParameter("memo");

			HospitalScheduleModel schedule = new HospitalScheduleModel();
			schedule.setNo(no);
			schedule.setName(name);
			schedule.setTime(time);
			schedule.setNotificationTime(notificationTime);
			schedule.setMemo(memo);
			
			boolean check = hDao.updateHospitalSchedule(schedule);
			
			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
	}

}
