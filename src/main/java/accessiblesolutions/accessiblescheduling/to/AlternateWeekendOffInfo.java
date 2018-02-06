package accessiblesolutions.accessiblescheduling.to;

import java.util.ArrayList;
import java.util.HashMap;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class AlternateWeekendOffInfo {
    private ArrayList<Shift> shifts;
    
    public AlternateWeekendOffInfo(HashMap<Integer,ArrayList<Shift>> info,Employee employee,int selectedWeek) {
    }
}
