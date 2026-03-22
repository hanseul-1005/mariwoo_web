package windy.mariwoo.rest;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import windy.mariwoo.dao.LoginDAO;
import windy.mariwoo.model.UserModel;

/**
 * Servlet implementation class Rest
 */
@WebServlet(description = "RestLogin", urlPatterns = { "/login.windy" })
public class RestLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestLogin() {
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

		LoginDAO lDao = new LoginDAO();
		
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
			
			user = lDao.login(user);
			
			JSONObject json = new JSONObject();
			
			if(user.isCheck()) {
				json.put("result", String.valueOf(user.isCheck()));
				json.put("no", user.getNo());
				json.put("tel", user.getTel());
				json.put("email", user.getEmail());
				json.put("birth", user.getBirth());
				
			}else{
				json.put("result", "false");
				json.put("message", "아이디와 비밀번호를 확인해주세요.");
			}
			
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
