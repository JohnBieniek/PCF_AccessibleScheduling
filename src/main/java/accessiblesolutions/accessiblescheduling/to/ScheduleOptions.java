package accessiblescheduling.to;

public class ScheduleOptions {
	private String month;
	private String year;
	private boolean allowOvertime;
	private boolean allowInactive;
	private boolean allowUnavailable;
	private boolean prioritizeSecondShift;
	private boolean dailyMax;
	private boolean weeklyMax;
	
	public ScheduleOptions(String month, String year, boolean allowOvertime, boolean allowInactive,
			boolean allowUnavailable, boolean prioritizeSecondShift, boolean dailyMax, boolean weeklyMax) {
		super();
		this.month = month;
		this.year = year;
		this.allowOvertime = allowOvertime;
		this.allowInactive = allowInactive;
		this.allowUnavailable = allowUnavailable;
		this.prioritizeSecondShift = prioritizeSecondShift;
		this.dailyMax = dailyMax;
		this.weeklyMax = weeklyMax;
	}
	
	public ScheduleOptions() {
	}

	public String getMonth() {
		return month;
	}
	
	public int getMonthInt() {
		return Integer.parseInt(month);
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getYear() {
		return year;
	}
	public int getYearInt() {
		return Integer.parseInt(year);
	}
	public void setYear(String year) {
		this.year = year;
	}
	public boolean isAllowOvertime() {
		return allowOvertime;
	}
	public void setAllowOvertime(boolean allowOvertime) {
		this.allowOvertime = allowOvertime;
	}
	public boolean isAllowInactive() {
		return allowInactive;
	}
	public void setAllowInactive(boolean allowInactive) {
		this.allowInactive = allowInactive;
	}
	public boolean isAllowUnavailable() {
		return allowUnavailable;
	}
	public void setAllowUnavailable(boolean allowUnavailable) {
		this.allowUnavailable = allowUnavailable;
	}
	public boolean isPrioritizeSecondShift() {
		return prioritizeSecondShift;
	}
	public void setPrioritizeSecondShift(boolean prioritizeSecondShift) {
		this.prioritizeSecondShift = prioritizeSecondShift;
	}
	public boolean isDailyMax() {
		return dailyMax;
	}
	public void setDailyMax(boolean dailyMax) {
		this.dailyMax = dailyMax;
	}
	public boolean isWeeklyMax() {
		return weeklyMax;
	}
	public void setWeeklyMax(boolean weeklyMax) {
		this.weeklyMax = weeklyMax;
	}

	@Override
	public String toString() {
		return "ScheduleOptions [month=" + month + ", year=" + year + ", allowOvertime=" + allowOvertime
				+ ", allowInactive=" + allowInactive + ", allowUnavailable=" + allowUnavailable
				+ ", prioritizeSecondShift=" + prioritizeSecondShift + ", dailyMax=" + dailyMax + ", weeklyMax="
				+ weeklyMax + "]";
	}
}
