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

/**
 * Servlet implementation class Rest
 */
@WebServlet(description = "RestUser", urlPatterns = { "/user.windy" })
public class RestUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestUser() {
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

		UserDAO uDao = new UserDAO();
		
		String cmd = request.getParameter("cmd");
		System.out.println("rest cmd : "+cmd);
		
		if("login".equals(cmd)) {
			String id = request.getParameter("id");
			String pw = request.getParameter("pw");
			
			System.out.println("id : "+id);
			System.out.println("pw : "+pw);
			UserModel user = new UserModel();
			user.setId(id);
			user.setPw(pw);
			
			user = uDao.login(user);
			
			JSONObject json = new JSONObject();
			
			if(user.isCheck()) {
				json.put("result", String.valueOf(user.isCheck()));
				json.put("no", String.valueOf(user.getNo()));
				json.put("tel", user.getTel());
				json.put("email", user.getEmail());
				json.put("birth", user.getBirth());
				json.put("name", user.getName());
				
			}else{
				json.put("result", "false");
				json.put("message", "아이디와 비밀번호를 확인해주세요.");
			}
			
			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		else if("check_id".equals(cmd)) {
			String id = request.getParameter("id");
			
			boolean check = uDao.checkId(id);

			JSONObject json = new JSONObject();

			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		else if("sign".equals(cmd)) {
			String id = request.getParameter("id");
			String pw = request.getParameter("pw");
			String name = request.getParameter("name");
			String tel = request.getParameter("tel");
			String email = request.getParameter("email");
			String birth = request.getParameter("birth");
			
			UserModel user = new UserModel();
			user.setId(id);
			user.setPw(pw);
			user.setName(name);
			user.setTel(tel);
			user.setEmail(email);
			user.setBirth(birth);
			
			boolean check = uDao.insertUser(user);

			JSONObject json = new JSONObject();
			
			System.out.println("result : "+String.valueOf(check));
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
			
		}
		else if("change_pw".equals(cmd)) {
			long no = Long.parseLong(request.getParameter("no"));
			String pw = request.getParameter("pw");
			
			System.out.println("no : "+no);
			System.out.println("pw : "+pw);
			
			boolean check = uDao.updatePw(no, pw);

			System.out.println("check : "+check);
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
			
			boolean check = uDao.updateUser(user);

			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("req_relation".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("user_no"));
			String targetId = request.getParameter("target_id");
			String targetTel = request.getParameter("target_tel");
			
			long targetNo = uDao.selectTarget(targetId, targetTel);
			
			boolean check = false;
			
			if(0<targetNo) {
				check = uDao.insertRelation(userNo, targetNo);
			}

			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("change_accept".equals(cmd)) {
			long relationNo = Long.parseLong(request.getParameter("relation_no"));
			String accept = request.getParameter("accept");
			
			boolean check = uDao.updateRelation(relationNo, accept);
			

			JSONObject json = new JSONObject();
			
			json.put("result", String.valueOf(check));

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
		
		else if("relation_list".equals(cmd)) {
			long userNo = Long.parseLong(request.getParameter("user_no"));
			String type = request.getParameter("type");
			
			List<UserModel> listUser = uDao.selectListRelation(userNo, type);

			JSONArray jArr = new JSONArray();
			
			for(int i=0; i<listUser.size(); i++) {
				UserModel user = listUser.get(i);

				JSONObject jObj = new JSONObject();
				jObj.put("no", user.getNo());
				jObj.put("name", user.getName());
				jObj.put("tel", user.getTel());
				
				jArr.add(jObj);
			}

			JSONObject json = new JSONObject();
			
			String result = "false";
			if(0<jArr.size()) {
				result = "true";
			}

			System.out.println("result : "+result);
			response.setContentType("text/json; charset=utf-8");
			json.put("listUser", jArr);
			json.put("result", result);

			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(json);
		}
	}

}
