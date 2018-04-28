angular.module('shift', ['ngResource', 'ui.bootstrap']).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Employees', function ($resource) {
        return $resource('employees');
    }).
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

//Requires $scope.selectedShift and $scope.employees currently
function ShiftController($scope, $modal, $http, Shifts, Employees, Status) {
    $scope.assignEmployee = function(employee){
   	 	$scope.selectedShift.staffId= employee.id;
   	 	$scope.selectedShift.staffName=employee.first;
   	 	$scope.selectedShift.assigned=true;
   	 	$scope.selectedShift.assignmentReason="Manual assignment";
   	 	$scope.saveShift($scope.selectedShift);
    } 
    
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
    }
    
	$scope.saveShift= function saveShift(shift) {
        Shifts.save(shift,
            function () {
                Status.success("Shift saved");
            },
            function (result) {
                Status.error("Error saving shift: " + result.status);
            }
        );
    }
	   
	 $scope.getDuration = function(shift){
    	$http({
            url: '/shifts/duration',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                shift: shift,
            }
        })
        .then(function(response) {
    		$scope.selectedShift.duration = response.data;
        });
    }
    
    $scope.getShiftsScheduled = function(employee,shift){
    	$http({
            url: '/compatibility/shifts',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.shiftsScheduled = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getHoursScheduled = function(employee,shift){
    	$http({
            url: '/compatibility/hours',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.hoursScheduled = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getHoursNeeded = function(employee,shift){
    	$http({
            url: '/compatibility/hoursNeeded',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.hoursNeeded = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getHoursNeededAfterAssignment = function(employee,shift){
    	$http({
            url: '/compatibility/hoursNeededAfterAssignment',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.hoursNeededAfterAssignment = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getHoursAvailable = function(employee,shift){
    	$http({
            url: '/compatibility/hoursAvailable',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.hoursAvailable = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getHoursAvailableAfterAssignment = function(employee,shift){
    	$http({
            url: '/compatibility/hoursAvailableAfterAssignment',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.hoursAvailableAfterAssignment = response.data.numericResponse;
    		    }
    		});
        });
    }
    
    $scope.getUnassigned = function(employee,shift){
    	$http({
            url: '/compatibility/unassigned',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.unassigned = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getRequestedOff = function(employee,shift){
    	$http({
            url: '/compatibility/requestedOff',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.vacationing = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getAssignmentViolatesOffAlternateWeekends = function(employee,shift){
    	$http({
            url: '/compatibility/assignmentViolatesOffAlternateWeekends',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.assignmentViolatesOffAlternateWeekends = response.data.booleanResponse;
    		    }
    		});
        });
    }

    $scope.getAssignmentIncursOvertime = function(employee,shift){
    	$http({
            url: '/compatibility/assignmentIncursOvertime',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.assignmentIncursOvertime = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getWorkedLastWeekend = function(employee,shift){
    	$http({
            url: '/compatibility/workedLastWeekend',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.workedLastWeekend = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getWorkedLastWeekend = function(employee,shift){
    	$http({
            url: '/compatibility/workedLastWeekend',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.workedLastWeekend = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getWorkingNextWeekend = function(employee,shift){
    	$http({
            url: '/compatibility/workingNextWeekend',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.workingNextWeekend = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getAvailability = function(employee,shift){
    	$http({
            url: '/compatibility/availability',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.available = response.data.booleanResponse;
    		    }
    		});
        });
    }	
    
    $scope.getValidity = function(employee,shift){
    	$http({
            url: '/compatibility/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.validity = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.getAssignability = function(employee,shift){
    	$http({
            url: '/compatibility/assignability',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.assignable = response.data.booleanResponse;
    		    }
    		});
        });
    }	
    
    $scope.getCompatibility = function(employee,shift){
    	$scope.compatibility = $http({
            url: '/compatibility/compatibility',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                shift: shift,
            }
        })
        .then(function(response) {
    		$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		        this.compatible = response.data.compatible;
    		    }
    		});
        });
    }
}
