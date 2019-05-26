angular.module('client', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('client/:id', {id: '@id'});
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

function MobileClientController($scope, $modal, $http, Clients, Client,Employee, Employees, Status) {
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
	 $scope.selectedInterval="days";
	$scope.setTab = function(newTab){
      $scope.tab = newTab;
	}
	$scope.setClient = function(newClient){
	      $scope.client = newClient;
	      $scope.listRequests(newClient);
		}
	
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
     
	 $scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    };
    
    $scope.isClientSet = function(client){
        return $scope.client === client && client !==null;
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
				 request.displayValue+" as noted";
			 }
		 }
		 
		 request.displayValue+=".";
	 }

	 $scope.listClients = function listClients() {
        $scope.clients = Clients.query();

        if($scope.client==null){
        	$scope.client = $scope.clients[0];
        }
        
        $scope.employees = Employees.query();
    }
    
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
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
    
    $scope.listCurrentShifts = function listCurrentShifts(client){
    	let id = "-1";
    	
    	if(null!=client){
    		id=client.id;
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
}
