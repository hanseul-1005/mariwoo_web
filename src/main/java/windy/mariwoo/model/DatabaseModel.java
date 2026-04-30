package windy.mariwoo.model;

public class DatabaseModel {
	
	private String dbDriver = "org.mariadb.jdbc.Driver";
    //private String jdbcUrl = "jdbc:mariadb://localhost:3306/mariwoo";

	private String jdbcUrl = "jdbc:mariadb://mariwoodb.windygnt.myds.me:33306/mariwoo";
	private String user = "mariwoo";         
	private String password = "windy0136";
	
	public String getDbDriver() {
		return dbDriver;
	}
	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
