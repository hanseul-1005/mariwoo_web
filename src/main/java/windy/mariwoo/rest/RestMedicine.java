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

import windy.mariwoo.dao.LoginDAO;
import windy.mariwoo.model.MedicineModel;
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

		MedicineDAO mDao = new MedicineDAO();
		
		String cmd = request.getParameter("cmd");
		System.out.println("rest cmd : "+cmd);
		
		if("list".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			int weekDay = Integer.parseInt(request.getParameter("week_day"));
			
			MedicineModel moedlParam = new MedicineModel();
			moedlParam.setUserNo(no);
			moedlParam.setWeekDay(weekDay);

			List<MedicineModel> listMedicine = mDao.selectListMedicine(moedlParam);

			JSONArray jArr = new JSONArray();
			for(int i=0; i<listMedicine.size(); i++) {

				JSONObject jObj = new JSONObject();
				JSONArray jArr2 = new JSONArray();
				
				jObj.put("name", listMedicine.get(i).getName());
				
				for(int j=0; j<listMedicine.get(i).getListMedicine().size(); j++) {

					MedicineModel medicine = listMedicine.get(i).getListMedicine().get(j);
					
					JSONObject jObj2 = new JSONObject();
					jObj2.put("schedule_no", medicine.getScheduleNo());
					jObj2.put("intake_time", medicine.getIntakeTime());
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
			json.put("listMedicine", jArr);
			json.put("result", result);

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("change_pw".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			String pw = request.getParameter("pw");
			
			boolean check = lDao.updatePw(no, pw);

			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("change_info".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			String tel = request.getParameter("tel");
			String email = request.getParameter("email");
			String birth = request.getParameter("birth");
			
			UserModel user = new UserModel();
			user.setNo(no);
			user.setTel(tel);
			user.setEmail(email);
			user.setBirth(birth);
			
			boolean check = lDao.updateUser(user);

			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
	}

}
