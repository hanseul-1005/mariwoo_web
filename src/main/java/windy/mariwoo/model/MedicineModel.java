package windy.mariwoo.model;

import java.util.ArrayList;

public class MedicineModel {

	private long no = -1;
	private long userNo = -1;
	private String name = "";
	private String intakeType = "식전";
	
	
	private long scheduleNo = -1; 
	private int weekDay = 0;
	private String intakeTime = "";
	private String intakeTimeType = "";
	private boolean alarmEnable = false;
	private String createAt = "";
	private String updateAt = "";
	
	private long alarmNo = -1;
	private String alarmDate = "";
	private String alarmTime = "";
	private boolean alarmTriggered = false;
	private boolean intakeConfirmed = false;
	private String alarmCreateAt = "";
	
	private ArrayList<MedicineModel> listMedicine = null;
	
	
	public long getNo() {
		return no;
	}
	public void setNo(long no) {
		this.no = no;
	}
	public long getUserNo() {
		return userNo;
	}
	public void setUserNo(long userNo) {
		this.userNo = userNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIntakeType() {
		return intakeType;
	}
	public void setIntakeType(String intakeType) {
		this.intakeType = intakeType;
	}
	public long getScheduleNo() {
		return scheduleNo;
	}
	public void setScheduleNo(long scheduleNo) {
		this.scheduleNo = scheduleNo;
	}
	public int getWeekDay() {
		return weekDay;
	}
	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}
	public String getIntakeTime() {
		return intakeTime;
	}
	public void setIntakeTime(String intakeTime) {
		this.intakeTime = intakeTime;
	}
	public String getIntakeTimeType() {
		return intakeTimeType;
	}
	public void setIntakeTimeType(String intakeTimeType) {
		this.intakeTimeType = intakeTimeType;
	}
	public boolean isAlarmEnable() {
		return alarmEnable;
	}
	public void setAlarmEnable(boolean alarmEnable) {
		this.alarmEnable = alarmEnable;
	}
	public String getCreateAt() {
		return createAt;
	}
	public void setCreateAt(String createAt) {
		this.createAt = createAt;
	}
	public String getUpdateAt() {
		return updateAt;
	}
	public void setUpdateAt(String updateAt) {
		this.updateAt = updateAt;
	}
	public long getAlarmNo() {
		return alarmNo;
	}
	public void setAlarmNo(long alarmNo) {
		this.alarmNo = alarmNo;
	}
	public String getAlarmDate() {
		return alarmDate;
	}
	public void setAlarmDate(String alarmDate) {
		this.alarmDate = alarmDate;
	}
	public String getAlarmTime() {
		return alarmTime;
	}
	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}
	public boolean isAlarmTriggered() {
		return alarmTriggered;
	}
	public void setAlarmTriggered(boolean alarmTriggered) {
		this.alarmTriggered = alarmTriggered;
	}
	public boolean isIntakeConfirmed() {
		return intakeConfirmed;
	}
	public void setIntakeConfirmed(boolean intakeConfirmed) {
		this.intakeConfirmed = intakeConfirmed;
	}
	public String getAlarmCreateAt() {
		return alarmCreateAt;
	}
	public void setAlarmCreateAt(String alarmCreateAt) {
		this.alarmCreateAt = alarmCreateAt;
	}
	public ArrayList<MedicineModel> getListMedicine() {
		return listMedicine;
	}
	public void setListMedicine(ArrayList<MedicineModel> listMedicine) {
		this.listMedicine = listMedicine;
	}
	
	
}
