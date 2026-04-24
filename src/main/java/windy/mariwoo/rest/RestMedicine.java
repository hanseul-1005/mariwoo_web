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

import windy.mariwoo.dao.UserDAO;
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
					jObj2.put("intake_time", medicine.getIntakeTimeType()+" "+medicine.getIntakeTime());
					
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
		
		else if("add_medicine".equals(cmd)) {
			
			long userNo = Long.parseLong(request.getParameter("user_no"));
			String name = request.getParameter("name");
			String strWeekday = request.getParameter("weekday");
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
			
			MedicineModel medicine = new MedicineModel();
			medicine.setUserNo(userNo);
			medicine.setName(name);
			
			long no = mDao.insertMedicine(medicine);
			
			int weekDay1 = 0;
			int weekDay2 = 0;
			int weekDay3 = 0;
			int weekDay4 = 0;
			int weekDay5 = 0;
			int weekDay6 = 0;
			int weekDay7 = 0;
			
			for (int i = 0; i < strWeekday.length(); i++) {
				String day = strWeekday.substring(i, i + 1);
			    
				if(i==0) weekDay1 = Integer.parseInt(day);
				if(i==1) weekDay2 = Integer.parseInt(day);
				if(i==2) weekDay3 = Integer.parseInt(day);
				if(i==3) weekDay4 = Integer.parseInt(day);
				if(i==4) weekDay5 = Integer.parseInt(day);
				if(i==5) weekDay6 = Integer.parseInt(day);
				if(i==6) weekDay7 = Integer.parseInt(day);
			}
			
			MedicineModel schedule = new MedicineModel();
			schedule.setNo(no);
			schedule.setWeekDay(weekDay1);
			schedule.setIntakeTimeType(intakeTimeType1);
			schedule.setIntakeTime(intakeTime1);
			schedule.setIntakeType(intakeType1);
			
			
			
			
		}
		else if("select_medicine".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("user_no"));
			long no = Long.parseLong(request.getParameter("no"));
			
			List<MedicineModel> listMedicine = mDao.selectListMedicine(null);
			
		}
		
	}

}
