angular.module('client', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('client/:id', {id: '@id'});
	}).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Shift', function ($resource) {
	    return $resource('shift/:id', {id: '@id'});
	}).
	factory('Employees', function ($resource) {
	    return $resource('employees');
	}).
	factory('Employee', function ($resource) {
	    return $resource('employees/:id', {id: '@id'});
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

function MobileClientController($scope, $modal, $http, Clients, Client,Shifts,Shift,Employee, Employees, Status) {
	 $scope.multiTableEditing=false;
	 $scope.month=1;
	 $scope.allowOvertime=false;
	 $scope.allowUnavailable=false;
	 $scope.prioritizeSecondShift=false;
	 $scope.useDailyMax=true;
	 $scope.useWeeklyMax=true;
	 $scope.allowInactive=false;
	 $scope.generatedBool = false;
	 $scope.generated="Generated";
	 $scope.assigned="Unassigned";
	 $scope.tab="Schedule";
	 $scope.monthName="January";
	 $scope.statusList=[];
	 $scope.unscheduled=0;
	 $scope.scheduled=0;
	 $scope.selectedInterval="day(s)";
	 $scope.detailsChanged=false;
	 $scope.week = new Date();//.getTime();
	 console.log($scope.week);
	 $scope.days=['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
	$scope.setTab = function(newTab){
      $scope.tab = newTab;
	}
	$scope.setClient = function(newClient){
	      $scope.client = newClient;
	      $scope.listRequests(newClient);
	      $scope.listShifts();
		}
	
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	$scope.setDetailsToChanged = function(){
      $scope.detailsChanged=true;;
	}
     $scope.isDay = function(shift, day){
    	 return day.toUpperCase() === shift.startsLocalDate.dayOfWeek.toUpperCase();
     }
     
     $scope.getDisplayWeek = function(){
    	 console.log("week"+$scope.week);
    	 console.log("getDate"+$scope.week.getDate());
    	 console.log("scope.week.getDay"+$scope.week.getDay());
    	 $scope.displayWeek = parseInt($scope.week.getDate())-parseInt($scope.week.getDay()) + " - " +parseInt($scope.week.getDate())-parseInt($scope.week.getDay()) ;
    	 console.log("displayWeek"+$scope.displayWeek);
    	 switch(parseInt($scope.week.getMonth())+1){
		   	  case 1:
		   		  $scope.monthName="January";
		   		  break;
		   	  case 2:
		   		  $scope.monthName="Febuary";
		   		  break;
		   	  case 3:
		   		  $scope.monthName="March";
		   		  break;
		   	  case 4:
		   		  $scope.monthName="April";
		   		  break;
		   	  case 5:
		   		  $scope.monthName="May";
		   		  break;
		   	  case 6:
		   		  $scope.monthName="June";
		   		  break;
		   	  case 7:
		   		  $scope.monthName="July";
		   		  break;
		   	  case 8:
		   		  $scope.monthName="August";
		   		  break;
		   	  case 9:
		   		  $scope.monthName="September";
		   		  break;
		   	  case 10:
		   		  $scope.monthName="October";
		   		  break;
		   	  case 11:
		   		  $scope.monthName="November";
		   		  break;
		   	  case 12:
		   		  $scope.monthName="December";
		   		  break;
	     }
    	 
     }
     
     $scope.setShiftDisplay = function(shift){
    	 if(shift.startsLocalDateTime==null || shift.startsLocalDateTime==undefined){
			 return null;
		 }
		 
		let startHour = parseInt(shift.startsLocalDateTime.hour);
	 	let endHour= parseInt(shift.endsLocalDateTime.hour);
	 	
	 	let startMinute = parseInt(shift.startsLocalDateTime.minute);
	 	let endMinute= parseInt(shift.endsLocalDateTime.minute);
	 	
	 	let startModifier = "AM";
	 	let endModifier = "AM";
	 	
	 	if(startHour>11){
	 		startHour-=12;
	 		startModifier="PM"
	 	}
	 	
	 	if(endHour>11){
	 		endHour-=12;
	 		endModifier="PM"
	 	}
	 	
	 	if(startMinute<10){
	 		startMinute="0"+startMinute
	 	}
	 	
	 	if(endMinute<10){
	 		endMinute="0"+endMinute
	 	}
        shift.displayValue = startHour+":"+startMinute+startModifier+"-";
        shift.displayValue += endHour+":"+endMinute+endModifier;
		 
		 if(shift.staffName!=null){
			 shift.displayValue+=" with "+shift.staffName;
		 }
		 shift.displayValue+= ".";
     }
     
	 $scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    };
    
    $scope.isClientSet = function(client){
        return $scope.client === client && client !==null;
      };
      
      $scope.isClientChangeValid = function(client){
          let valid = true;
          
          if(client.first ==undefined || client.first.length<1){
        	  valid=false;
          }
          if(!$scope.detailsChanged){
        	  valid=false;
          }

          return valid;
        };
    
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
	 $scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	    }
	 $scope.setInitialDays = function setInitialDays(request){
		 console.log("days initial");
		 console.log(request.days);
		 if(request.days==null || request.days==undefined){
			 request.days=[false,false,false,false,false,false,false];
		 }
		 console.log(request.days);
	 }
	 
	 $scope.getDisplayValue = function getDisplayValue(request) {
		 if(request.startsLocalDateTime==null || request.startsLocalDateTime==undefined){
			 return null;
		 }
		 
		let startHour = parseInt(request.startsLocalDateTime.hour);
	 	let endHour= parseInt(request.endsLocalDateTime.hour);
	 	
	 	let startMinute = parseInt(request.startsLocalDateTime.minute);
	 	let endMinute= parseInt(request.endsLocalDateTime.minute);
	 	
	 	let startModifier = "AM";
	 	let endModifier = "AM";
	 	
	 	if(startHour>11){
	 		startHour-=12;
	 		startModifier="PM"
	 	}
	 	
	 	if(endHour>11){
	 		endHour-=12;
	 		endModifier="PM"
	 	}
	 	
	 	if(startMinute<10){
	 		startMinute="0"+startMinute
	 	}
	 	
	 	if(endMinute<10){
	 		endMinute="0"+endMinute
	 	}
        request.displayValue = startHour+":"+startMinute+startModifier+"-";
        request.displayValue += endHour+":"+endMinute+endModifier;
		 
		 if(request.requestEmployee){
			 request.displayValue+=" with "+request.staffName;
		 }
		 if(request.repeats){
			 request.displayValue+= " every "+request.repeatsEvery + " " +request.interval; 
		 }
		 if(request.days && request.interval=="week(s)"){
			 request.displayValue+=" [";
			 if(request.days[0]){
				 request.displayValue+="Su";
			 }
			 if(request.days[1]){
				 request.displayValue+="M";
			 }
			 if(request.days[2]){
				 request.displayValue+="Tu";
			 }
			 if(request.days[3]){
				 request.displayValue+="W";
			 }
			 if(request.days[4]){
				 request.displayValue+="Th";
			 }
			 if(request.days[5]){
				 request.displayValue+="F";
			 }
			 if(request.days[6]){
				 request.displayValue+="Sa";
			 }
			 request.displayValue+="]";
		 }
		 var weekOfMonth = 0;
		 var dateCursor = request.startsLocalDate.dayOfMonth;
		 
		 for(var i =0; i<8;i++){
			 if(dateCursor>0){
				 dateCursor-=7;
				 weekOfMonth+=1;
			 }
		 }
		 
		 if(request.interval=="month(s)"){
			 if(request.monthInterval=="days"){
				 request.displayValue+=" on day "+request.startsLocalDate.dayOfMonth;
			 }
			 else{
				 request.displayValue+=" on "+request.startsLocalDate.dayOfWeek + " of week "+weekOfMonth;
			 }
		 }
		 console.log("yearInterval"+request.yearInterval);
		 if(request.interval=="year(s)"){
			 request.displayValue+= " in " +request.startsLocalDate.month;
			 if(request.yearInterval=="days"){
				 request.displayValue+=" on day "+request.startsLocalDate.dayOfMonth;
			 }
			 else{
				 request.displayValue+=" on "+request.startsLocalDate.dayOfWeek + " of week "+weekOfMonth;
			 }
		 }
		 
		 if(request.repeats){
			 request.displayValue+= " starting "+request.startDate; 
		 }
		 else{
			 request.displayValue+=" on "+ request.startDate;
		 }

		 //Add exceptions
		 if(request.exceptions !=undefined && request.exceptions!=null && request.exceptions.length>0){
			 request.displayValue+=" except ";
			 
			 if(request.exceptions.length<4){
				 for(var index = 0; index <request.exceptions.length;index++){
					 if(index==request.exceptions.length-1 && request.exceptions.length>1){
						 request.displayValue+=" and " 
					 }
					 else if(index>0){
						 request.displayValue+=" ,"
					 }
					 request.displayValue+=request.exceptions[index];
				 }
			 }
			 else{
				 request.displayValue+=" as noted";
			 }
		 }
		 
		 request.displayValue+=".";
	 }

	 $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
        $scope.clients = $scope.clients.sort(function(a, b){return a.first > b.first});
        console.log("clients");
        console.log($scope.clients);
        if($scope.client==null){
        	$scope.client = $scope.clients[0];
        }
        
        $scope.employees = Employees.query();
    }
    
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
    }
    
    $scope.getDisplayWeek = function () {
    	
    }
    
    function saveShift(shift) {
        Shifts.save(shift,
            function () {
                Status.success("Shift saved");
                $scope.listShifts();
            },
            function (result) {
                Status.error("Error saving shift: " + result.status);
            }
        );
    }
    
    $scope.addShift = function () {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/shiftForm.html',
            controller: ShiftModalController,
            resolve: {
            	shift: function(){
            		return {};
            	},
            	client: function(){
            		return clone($scope.client);
            	},
            	clients: function(){
            		return clone($scope.clients);
            	},
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (shift) {
        	console.log(shift.startDate);
        	console.log(shift.startDate.split("-"));
        	if(shift.startYear==0 || shift.startYear == undefined || shift.startYear==null){
        		shift.startYear=shift.startDate.split("-")[0];
        	}
        	if(shift.startMonth==0 || shift.startMonth == undefined || shift.startMonth==null){
        		shift.startMonth=shift.startDate.split("-")[1];
        	}
        	console.log(shift.startMonth);
        	console.log(shift.startYear);
            saveShift(shift);
        });
    };
    
    
    $scope.deleteShift = function (shift) {
        Shift.delete({id: shift.id},
            function () {
                Status.success("Shift deleted");
                $scope.listShifts();
            },
            function (result) {
                Status.error("Error deleting shift: " + result.status);
            }
        );
    };
    
    $scope.listShifts = function listShifts(){
    	let id = "-1";
    	
    	if(null!=$scope.client){
    		id=$scope.client.id;
    	}
    	console.log($scope.week);
    	$http({
            url: '/schedule/clientShiftsForWeek',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	clientId:id,
            	month:$scope.week.getMonth()+1,
            	day: $scope.week.getDate(),
            	year:$scope.week.getFullYear()
            }
        })
        .then(function(response) {
        	console.log("got shifts:");
        	console.log(response.data);
    		$scope.shifts = response.data;
    	});
    }
   
    $scope.listRequests = function listRequests(client){
    	let id = "-1";
    	
    	if(null!=client){
    		id=client.id;
    	}
    	
    	$http({
            url: '/schedule/clientsRequests',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	clientId:id
            }
        })
        .then(function(response) {
    		$scope.requests = response.data;
    	});
    }
    
    $scope.listCurrentShifts = function listCurrentShifts(){
    	let id = "-1";
    	
    	if(null!=$scope.client){
    		id=$scope.client.id;
    	}
    	
    	$http({
            url: '/schedule/currentShifts',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	clientId:id
            }
        })
        .then(function(response) {
        	console.log("shifts");
        	console.log(response.data);
    		$scope.currentShifts = response.data;
    	});
    }
    
 
    $scope.editRequest = function (selectedRequest,selectedClient,employees) {
      var editModal = $modal.open({
          templateUrl: 'templates/modal/requestForm.html',
          controller: RequestModalController,
          windowClass: 'app-modal-window',
          resolve: {
          	selectedClient: function(){
          		return clone(selectedClient);
          	},
          	employees: function(){
          		return clone(employees);
          	},
          	selectedEmployee: function(){
          		return selectedRequest.staffId;
          	},
              shiftRequest: function () {
                  return selectedRequest;
              },
              action: function() {
                  return 'edit';
              }
          }
      });

      editModal.result.then(function (shiftRequest) {
   		$http({
               url: '/clientRequests',
               method: 'POST',
               headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
               },
               params: {
                   param: shiftRequest
               }
           })
           .then(function(response) {
        		$scope.requests = response.data;
           });
      });
  };
  
    $scope.addRequest = function (selectedClient,employees) {
		 console.log("attempting to open requestForm and RequestModalController");
       var addModal = $modal.open({
           templateUrl: 'templates/modal/requestForm.html',
           controller: RequestModalController,
           windowClass: 'app-modal-window',
           resolve: {
           	selectedClient: function(){
           		return clone(selectedClient);
           	},
           	employees: function(){
           		return clone(employees);
           	},
           	selectedEmployee: function(){
           		return "";
           	},
               shiftRequest: function () {
                   return {};
               },
               action: function() {
                   return 'add';
               }
           }
       });

       addModal.result.then(function (shiftRequest) {
    	   $scope.getDisplayValue(shiftRequest);
    		$http({
                url: '/clientRequests',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                params: {
                    param: shiftRequest
                }
            })
            .then(function(response) {
            	$scope.requests = response.data;
            });
       });
   };
   
   function clone (obj) {
       return JSON.parse(JSON.stringify(obj));
   }
   
   function saveShiftRequest(shiftRequest) {
       Requests.save(shiftRequest,
           function () {
               Status.success("Request saved");
               $scope.listShiftRequests();
           },
           function (result) {
               Status.error("Error saving shift Request: " + result.status);
           }
       );
   }
   
   $scope.editShift = function (shift) {
       var updateModal = $modal.open({
           templateUrl: 'templates/modal/shiftForm.html',
           controller: ShiftModalController,
           resolve: {
               shift: function() {
                   return clone(shift);
               },
               clients: function(){
           		return {};
           	},
               action: function() {
                   return 'update';
               }
           }
       });

       updateModal.result.then(function (shift) {
           saveShift(shift);
       });
   };
   
   $scope.updateShiftRequest = function (selectedClient, shiftRequest,employees) {
   	var selectedEmployee = employees.filter(function( employee ) {
 		  return employee.id == shiftRequest.staffId;
   	});
       var updateModal = $modal.open({
           templateUrl: 'templates/modal/requestForm.html',
           controller: RequestModalController,
           resolve: {
           	selectedClient: function(){
           		return clone(selectedClient);
           	},
               shiftRequest: function() {
                   return clone(shiftRequest);
               },
           	employees: function(){
           		return clone(employees);
           	},
           	selectedEmployee: function(){
           		if(selectedEmployee!=null && selectedEmployee.length>0){
           			return clone(selectedEmployee[0]);
           		}
           		return "";
           	},
               action: function() {
                   return 'update';
               }
           }
       });

       updateModal.result.then(function (shiftRequest) {
           saveShiftRequest(shiftRequest);
       });
   };
   
   $scope.deleteShiftRequest = function (shiftRequest) {
	   if(confirm("Are you sure to delete info for the following request?"+shiftRequest.displayValue)){
		   $http({
               url: '/schedule/deleteRequest',
               method: 'GET',
               headers: {
                   'Content-Type': 'application/x-www-form-urlencoded'
               },
               params: {
                   id: shiftRequest.id
               }
           })
           .then(function(response) {
           		$scope.requests = response.data;
           });
	   }
    };
    
    $scope.newClient = function () {
    	$http({
            url: '/schedule/createClient',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Client created");
        		$scope.clients = response.data;
        		$scope.tab="Schedule";
        	}
        	else{
        		Status.error("Failed to save client info.")
        	}
        });
    };
    
    $scope.ok = function () {
    	$scope.detailsChanged=false;
    	$http({
            url: '/schedule/updateClient',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                param: $scope.client
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Client saved");
        		$scope.client = response.data;
            	$scope.detailsChanged=false;
        	}
        	else{
        		Status.error("Failed to save client info.")
        	}
        });
    };
    
    $scope.deleteShift = function (shift) {
  	   if(confirm("Are you sure you want to delete the following shift? "+shift.display)){
     	$http({
             url: '/shifts/'+shift.id,
             method: 'DELETE',
             headers: {
                 'Content-Type': 'application/x-www-form-urlencoded'
             },
             params: {
             }
         })
         .then(function(response) {
         	console.log(response);
         	if(response){
                 Status.success("Shift deleted.");
                 $scope.listShifts();
         	}
         	else{
         		Status.error("Failed to delete client info.")
         	}
         });
  	   }
     };
     
    $scope.delete = function () {
 	   if(confirm("Are you sure you want to delete info for "+$scope.client.first + " "+$scope.client.initial+"?")){
    	$http({
            url: '/clients/'+$scope.client.id,
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	console.log(response);
        	if(response){
                Status.success("Client deleted.");
        		$scope.clients =response.data;
        		$scope.client =response.data[0];
        		$scope.tab="Schedule";
        	}
        	else{
        		Status.error("Failed to delete client info.")
        	}
        });
 	   }
    };
    
    $scope.cancel = function () {
    	$http({
            url: '/clients/'+$scope.client.id,
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	$scope.detailsChanged=false;
        	if(response.data){
        		$scope.client = response.data;
        	}
        });
    };
}
