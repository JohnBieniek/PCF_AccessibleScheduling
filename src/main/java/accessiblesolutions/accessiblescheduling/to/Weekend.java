package accessiblesolutions.accessiblescheduling.to;

import java.time.LocalDate;
import java.time.Month;

public class Weekend {
	private Month month=null;
	private LocalDate saturday=null;
	private LocalDate sunday=null;
	
    public Weekend() {
    }

    @Override
    public String toString(){
    	if(isValid()){
			return month.name() + " " + saturday.getDayOfMonth() + " & " + sunday.getDayOfMonth();
    	}
    	return "Invalid month";
    	
    }
	public boolean isValid() {
		boolean valid=true;
		
		if(null==saturday){
			valid=false;
		}else if(saturday.getDayOfWeek().getValue()!=6){
			valid=false;
		}
		
		if(null==sunday){
			valid=false;
		}else if(sunday.getDayOfWeek().getValue()!=7){
			valid=false;
		}
		
		if(month==null){
			valid=false;
		}
		
		return valid;
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
