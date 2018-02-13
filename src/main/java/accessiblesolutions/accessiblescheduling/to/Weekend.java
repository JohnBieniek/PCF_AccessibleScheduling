package accessiblesolutions.accessiblescheduling.to;

import java.time.LocalDate;
import java.time.Month;

public class Weekend {
	private Month month;
	private LocalDate saturday;
	private LocalDate sunday;
	
    public Weekend() {
    }

	public Month getMonth() {
		return month;
	}

	public void setMonth(Month month) {
		this.month = month;
	}

	public LocalDate getSaturday() {
		return saturday;
	}

	public void setSaturday(LocalDate saturday) {
		this.saturday = saturday;
	}

	public LocalDate getSunday() {
		return sunday;
	}

	public void setSunday(LocalDate sunday) {
		this.sunday = sunday;
	}
}
