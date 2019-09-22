angular.module('vacation', ['ngResource', 'ui.bootstrap']).
	factory("EditorStatus", function () {
        var editorEnabled = {};

        var enable = function (id, fieldName) {
            editorEnabled = { 'id': id, 'fieldName': fieldName };
        };

        var disable = function () {
            editorEnabled = {};
        };

        var isEnabled = function(id, fieldName) {
            return (editorEnabled['id'] == id && editorEnabled['fieldName'] == fieldName);
        };

        return {
            isEnabled: isEnabled,
            enable: enable,
            disable: disable
        }
    });

function VacationController($scope, $modal, $http) {
	 $scope.multiTableEditing=false;

	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 $scope.addAbsence = function(selectedEmployee,newDate){
	        selectedEmployee.requestedOff.push(newDate);
	        $scope.saveEmployee(selectedEmployee);
    };
	    
    $scope.removeAbsence=function(selectedEmployee,item){ 
    	 if(confirm("Are you sure you want to delete this request?")){
	        var index=selectedEmployee.requestedOff.indexOf(item)
	        selectedEmployee.requestedOff.splice(index,1);     
	        $scope.saveEmployee(selectedEmployee);
	    }
      }
	    
    $scope.saveEmployee = function saveEmployee(employee) {
     	if(employee.availability){
	     	for(var index = 0; index<employee.availability.length;index++){
				if(!employee.availabilityStartTimes){
					employee.availabilityStartTimes=[];
				}
				if(!employee.availabilityEndTimes){
					employee.availabilityEndTimes=[];
				}			
				if(!employee.availabilityDays){
					employee.availabilityDays=[];
				}
				employee.availabilityStartTimes.push(employee.availability[index].startTime);
				employee.availabilityEndTimes.push(employee.availability[index].endTime);
				employee.days.push(employee.availability[index].day);	
			}
      	}
 	   $http({
           url: '/employees',
           method: 'POST',
           headers: {
               'Authorization': $scope.idToken,
               'Content-Type': 'application/x-www-form-urlencoded'
           },
           params: {
        	   param: employee
           }
       })
       .then(function (response) {//TODO handle error state
    	   $scope.setEmployee(response.data);
       	   employee.id=response.data.id;
           if(employee.customFields){
	            var size = employee.customFields.length;
	           
	            for(var i = 0; i < size ;i++){
	                $http({
	                    url: 'https://scheduleaccessqa.cfapps.io/compatibility/setCustomFieldData',
	                    method: 'POST',
	                    headers: {
	                        'Content-Type': 'application/x-www-form-urlencoded'
	                    },
	                    params: {
	                        employee: employee,
	                        customField: employee.customFields[i],
	                        value:employee.customValue[i],
	                    }
	                });
	            }
           }
           
           $scope.notify("Employee saved");
       });
    }
}
