function EmployeeModalController($scope, $modalInstance, $http, employee, customFields, action) {
    $scope.customFieldDataEditing=true;//TODO remove this
    $scope.customFields = customFields;
    $scope.employeeAction = action;
    $scope.employee= {};
    
    var daysAvailable = [];
    var numeroPerguntas = 7;     
    for (var i = 0; i < numeroPerguntas; i++) {
    	daysAvailable.push(false);
    }
    
    if(employee){
    	$scope.employee = employee;
    }
    if(action=="add"){
    	employee.gender="female";
        employee.minHours=1;
        employee.maxHours=40;
        employee.customFields=customFields;
        var values = [];
        
        if(customFields){
        	var size = customFields.length;
        	for(var i=0;i<size;i++){
            	values.push(false);
            }
        	employee.customValue=values;
        }
        
        var hoursAvailable1=[];
        var hoursAvailable2=[];
        var hoursAvailable3=[];
        var hoursAvailable4=[];
        var hoursAvailable5=[];
        var hoursAvailable6=[];
        var hoursAvailable7=[];
        for(var i=0;i<24;i++){
        	hoursAvailable1.push(false);
        	hoursAvailable2.push(false);
        	hoursAvailable3.push(false);
        	hoursAvailable4.push(false);
        	hoursAvailable5.push(false);
        	hoursAvailable6.push(false);
        	hoursAvailable7.push(false);
        }
        employee.sundaysAvailability=hoursAvailable1;
        employee.mondaysAvailability=hoursAvailable2;
        employee.tuesdaysAvailability=hoursAvailable3;
        employee.wednesdaysAvailability=hoursAvailable4;
        employee.thursdaysAvailability=hoursAvailable5;
        employee.fridaysAvailability=hoursAvailable6;
        employee.saturdaysAvailability=hoursAvailable7;
        
        $scope.employee.daysAvailable=daysAvailable;
    }

    $scope.toggleAvailabilityFor = function toggleAvailabilityFor(day,boolean) {
    	var hoursAvailable=[];
    	for(var i=0;i<24;i++){
        	hoursAvailable.push(boolean);
    	}
    	if(day==0){//sunday
    		employee.sundaysAvailability=hoursAvailable;
    	}
    	if(day==1){//monday
    		employee.mondaysAvailability=hoursAvailable;
    	}
    	if(day==2){//tuesday
    		employee.tuesdaysAvailability=hoursAvailable;
    	}
    	if(day==3){//wednesday
    		employee.wednesdaysAvailability=hoursAvailable;
    	}
    	if(day==4){
    		employee.thursdaysAvailability=hoursAvailable;
    	}
    	if(day==5){
    		employee.fridaysAvailability=hoursAvailable;
    	}
    	if(day==6){
    		employee.saturdaysAvailability=hoursAvailable;
    	}
    }
    
    $scope.ok = function () {
        $modalInstance.close($scope.employee);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};