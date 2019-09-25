angular.module('mainNavigation', ['ngResource', 'ui.bootstrap']).
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

function MainNavigationController($scope, $modal, $http) {
	 $scope.init = function() {
        $scope.setPage("login");
		$scope.tab="Schedule";
		$scope.employeeTab="Schedule";
        $scope.showToast=false;
        $scope.alertMessage="";
        
        $scope.profile = null;
        $scope.idToken = null;
        $scope.user=false;
        $scope.manager=false;
        $scope.admin=false;
        
        $scope.lastShiftUpdate=null;
        $scope.lastLocalCustomFieldUpdate=null;
        $scope.lastLocalShiftUpdate=null;
        $scope.lastClientUpdate=null;
        $scope.lastLocalClientUpdate=null;
        $scope.lastEmployeeUpdate=null;
        $scope.lastLocalEmployeeUpdate=null;
        $scope.lastCustomFieldUpdate = null;
        
        if($scope.week==undefined || $scope.week ==null){
			 $scope.week = new Date();
		}
        $scope.monthTab=$scope.week.getMonth()+2;

    	$scope.customValue=[];//Holds custom field data in the details tab
        $scope.employee=null;
        $scope.employees=null;
        $scope.client=null;
        $scope.unmodifiedClient=null;
        $scope.clients=null;
        $scope.customFields=null;
        $scope.shifts=null;
        $scope.requests=null;
        $scope.alerts=null;
        
		$scope.days=['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
		$scope.maxMonth=$scope.week.getMonth();
		$scope.minMonth=$scope.maxMonth-1;
		if($scope.minMonth<0){
			 $scope.minMonth=11;
		}
		$scope.minMonth=$scope.minMonth-1;
		if($scope.minMonth<0){
			 $scope.minMonth=11;
		}
		$scope.maxMonth=$scope.maxMonth+1;
		if($scope.maxMonth>11){
			 $scope.maxMonth=0;
		}
		
		$scope.updateCycle=1;
		$scope.autoUpdateData();
	 };
	 
	 
	 $scope.incrementCycle = function(){
		 $scope.updateCycle=$scope.updateCycle+1;
		 if($scope.updateCycle=5){
			 $scope.updateCycle=1;
		 }
	 }
	 
	 $scope.autoUpdateData = function autoUpdateData(){
		 if($scope.page!="templates/page/scheduler.html" && $scope.page!="templates/page/alerts.html"){
			 $scope.listCustomFields();
			 
			 if($scope.page=="templates/page/employee.html" || $scope.page=="templates/page/client.html"){
				 $scope.listClients();
				 $scope.listEmployees();
				 $scope.listShifts();//TODO Make this only update when new
				 
				 if($scope.page=="templates/page/employee.html"){

				 }
				 else if($scope.page=="templates/page/client.html"){
					 $scope.listRequests();//TODO Make this only update when new
				 }
			 }
		 }else if($scope.page=="templates/page/alerts.html"){
			 $scope.listAlerts();
		 }
		 
		 $scope.incrementCycle();
		 setTimeout(autoUpdateData,60000);
	 }
	 
	 //API Access
	 $scope.getClientCustomFieldData = function (client,customField,index){
     	$http({
            url: '/compatibility/clientCustomFieldData',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                client: client,
                customField: customField,
                index:index,
            }
        })
        .then(function(response) {
        	$scope.customValue[response.data.numericResponse] = response.data.booleanResponse;
        });
     }
	 
	 $scope.listAlerts = function (){
    	if($scope.admin){
	    	$http({
	            url: '/alerts/findAll',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken
	            },
	            params: {
	            }
	        })
	        .then(function(response) {
	        	$scope.alerts=response.data;
	        });
    	}
	 }
	 
     $scope.listShifts = function listShifts(){
    	let id = "-1";
    	
       	$scope.getDisplayWeek();
       	
		 if($scope.page=="templates/page/employee.html"){
	    	if(null!=$scope.employee){
	    		id=$scope.employee.id;
		    	$http({
		            url: '/schedule/employeeShiftsForWeek',
		            method: 'GET',
		            headers: {
		                'Authorization': $scope.idToken,
		                'Content-Type': 'application/x-www-form-urlencoded'
		            },
		            params: {
		            	employeeId:id,
		            	month:$scope.week.getMonth()+1,
		            	day: $scope.week.getDate(),
		            	year:$scope.week.getFullYear()
		            }
		        })
		        .then(function(response) {
		    		$scope.setShifts(response.data);
		    	});
	    	}
		 }
		 else if($scope.page=="templates/page/client.html"){
	    	if(null!=$scope.client){
		    	if($scope.manager || $scope.admin){
		    		id=$scope.client.id;
		    		
			    	$http({
			            url: '/schedule/clientShiftsForWeek',
			            method: 'GET',
			            headers: {
				            'Authorization': $scope.idToken,
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
			    		$scope.setShifts(response.data);
			    	});
		    	}
	    	}
		 }
     }
     
	 $scope.listRequests = function listRequests(){
    	if($scope.manager || $scope.admin){
	    	let id = "-1";
	    	
	    	if(null!=$scope.client){
	    		id=$scope.client.id;

	    	
		    	$http({
		            url: '/schedule/clientsRequests',
		            method: 'GET',
		            headers: {
			            'Authorization': $scope.idToken,
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
    	}
     }
	 
	 $scope.listClients = function listClients() {
	    	if($scope.manager || $scope.admin){
	    		$http({
			           url: '/updateInfo/clients',
			           method: 'GET',
			           headers: {
			               'Authorization': $scope.idToken,
			               'Content-Type': 'application/x-www-form-urlencoded'
			           },
			           params: {
			           }
		       })
		       .then(function (response) {//TODO handle error state	    
		    	   if($scope.lastLocalClientUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalClientUpdate.time.nano){
		    		   $scope.lastLocalClientUpdate=response.data;
		    		   
		    		   $http({
				            url: '/clients/',
				            method: 'GET',
				            headers: {
					            'Authorization': $scope.idToken,
				                'Content-Type': 'application/x-www-form-urlencoded'
				            },
				            params: {
				            }
				        })
				        .then(function(response) {
				        	$scope.clients =response.data;
				        });
		    	   }
		       })
	    	}
	 }
	 
	 $scope.listCustomFields = function listCustomFields() {
		 if($scope.idToken!=null){
			 $http({
		           url: '/updateInfo/customFields',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		     })
		     .then(function (response) {//TODO handle error state	    	 
		  	   if($scope.lastLocalCustomFieldUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalCustomFieldUpdate.time.nano){
		  		   $scope.lastLocalCustomFieldUpdate=response.data;
					 $http({
			            url: '/customFields/',
			            method: 'GET',
			            headers: {
				            'Authorization': $scope.idToken,
			                'Content-Type': 'application/x-www-form-urlencoded'
			            },
			            params: {
			            }
			        })
			        .then(function(response) {
			        	$scope.customFields=response.data;
			        });
		  	   }
		     })
		 }
     }
	 
    $scope.listEmployees = function listEmployees() {
    	if($scope.manager || $scope.admin){
    		$http({
		           url: '/updateInfo/employees',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
	       })
	       .then(function (response) {//TODO handle error state	    	 
	    	   if($scope.lastLocalEmployeeUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalEmployeeUpdate.time.nano){
	    		   $scope.lastLocalEmployeeUpdate=response.data;
	    		   
	    		   $http({
			            url: '/employees/',
			            method: 'GET',
			            headers: {
				            'Authorization': $scope.idToken,
			                'Content-Type': 'application/x-www-form-urlencoded'
			            },
			            params: {
			            }
			        })
			        .then(function(response) {
			        	$scope.employees =response.data;
			        });
	    	   }
	       })
    	}
    }
    
    $scope.getLastShiftUpdate = function (){
		$http({
	           url: '/updateInfo/shifts',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   $scope.setLastShiftUpdate(response.data);
	       })
	}
		
	$scope.getLastClientUpdate = function (){
		$http({
	           url: '/updateInfo/clients',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   $scope.setLastClientUpdate(response.data);
	       })
	}
		
	$scope.getLastEmployeeUpdate = function (){
		var serverUpdate;
		
		$http({
	           url: '/updateInfo/employees',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
        })
        .then(function (response) {//TODO handle error state
    	   $scope.setLastEmployeeUpdate(response.data);
    	   serverUpdate=response.data;
        })
       
        return serverUpdate;
	}

	 //View Utils
	 $scope.getDisplayMonth = function(date){
    	 var monthName = "January";
    	 
		 switch(parseInt(date.getMonth())+1){
		  	  case 1:
		  		  monthName="January";
		  		  break;
		  	  case 2:
		  		  monthName="Febuary";
		  		  break;
		  	  case 3:
		  		  monthName="March";
		  		  break;
		  	  case 4:
		  		  monthName="April";
		  		  break;
		  	  case 5:
		  		  monthName="May";
		  		  break;
		  	  case 6:
		  		  monthName="June";
		  		  break;
		  	  case 7:
		  		  monthName="July";
		  		  break;
		  	  case 8:
		  		  monthName="August";
		  		  break;
		  	  case 9:
		  		  monthName="September";
		  		  break;
		  	  case 10:
		  		  monthName="October";
		  		  break;
		  	  case 11:
		  		  monthName="November";
		  		  break;
		  	  case 12:
		  		  monthName="December";
		  		  break;
	 	 }
		 
		 return monthName;
     }
	 
     $scope.getDisplayWeek = function(){
    	 var date = parseInt($scope.week.getDate());
    	 var day = parseInt($scope.week.getDay());
    	 
    	 var weekStart = $scope.week.addDays(-day);
    	 var weekEnd = weekStart.addDays(6);
    	 
    	 $scope.displayWeek = weekStart.getDate()+ " - " +weekEnd.getDate();

    	 $scope.year = parseInt(weekStart.getYear())+1900;
    	 $scope.monthName=$scope.getDisplayMonth(weekStart);
    	 $scope.displayDays=[
			'Sunday '+$scope.monthName + " "+weekStart.getDate(),
			'Monday '+$scope.getDisplayMonth(weekStart.addDays(1)) + " "+weekStart.addDays(1).getDate(),
			'Tuesday '+$scope.getDisplayMonth(weekStart.addDays(2)) + " "+weekStart.addDays(2).getDate(),
			'Wednesday '+$scope.getDisplayMonth(weekStart.addDays(3)) + " "+weekStart.addDays(3).getDate(),
			'Thursday '+$scope.getDisplayMonth(weekStart.addDays(4)) + " "+weekStart.addDays(4).getDate(),
			'Friday '+$scope.getDisplayMonth(weekStart.addDays(5)) + " "+weekStart.addDays(5).getDate(),
			'Saturday '+$scope.getDisplayMonth(weekStart.addDays(6)) + " "+weekStart.addDays(6).getDate()
		];
     }
     	 
	 $scope.noShiftDay= function(day){
		var unscheduled =true;
		
		if(null!=$scope.shifts){
			$scope.shifts.forEach(function(shift) {
			  if(shift.startsLocalDate.dayOfWeek==day.toUpperCase().split(" ")[0]){
			    unscheduled=false;
			  }
			});
		}
		
		return unscheduled;
	}
	 
    $scope.isDay = function(shift, day){
    	return day.toUpperCase().includes(shift.startsLocalDate.dayOfWeek.toUpperCase());
    }
	 
    //View setters
	$scope.setEmployee = function(employee){
		 $scope.employee=employee;
	}
	$scope.setEmployees = function(employees){
		 $scope.employees=employees;
	}
	$scope.setWeek = function (isWeek) {
       $scope.week = isWeek;
    };
	$scope.setShifts = function setShifts(shifts){
		 $scope.shifts=shifts;
	}
	$scope.setLastLocalShiftUpdate = function setLastLocalShiftUpdate(time){
		 $scope.lastLocalShiftUpdate=time;
	}
	$scope.setLastShiftUpdate = function (time){
		 $scope.lastShiftUpdate = time;
	}
	$scope.setLastEmployeeUpdate = function (time){
		 $scope.lastEmployeeUpdate = time;
	}
	$scope.setLastClientUpdate = function (time){
		 $scope.lastClientUpdate = time;
	}
	$scope.setClient = function (client){
		 $scope.client = client;
		 $scope.unmodifiedClient = $scope.clone(client);
	}
	$scope.setClients = function (clients){
		 $scope.clients = clients;
	}

	 
	//Navigation
	$scope.setMonthTab = function(monthTab){
		 $scope.monthTab = monthTab;
	}
	$scope.setTab = function(newTab){//ClientTab
	      $scope.tab = newTab;
	}
	$scope.setEmployeeTab = function(newTab){
	      $scope.employeeTab = newTab;
	}
	$scope.setPage = function (viewName) {
		var newPage = "templates/page/" + viewName + ".html"
		if(newPage!=$scope.page){
	        $scope.shifts=null;
	    	$scope.page = "templates/page/" + viewName + ".html";
		}
    };
    
    $scope.decrementWeek = function(){
   	 $scope.setWeek($scope.week.addDays(-7));
   	 
   	 $scope.getDisplayWeek();
   	 
   	 $scope.listShifts();
    }
    $scope.incrementWeek = function(){
    	$scope.setWeek($scope.week.addDays(7));
   	 
   	 	$scope.getDisplayWeek();
   	 
   	 	$scope.listShifts();
    }
	
     
    //Auth
	$scope.loggedIn = function loggedIn() {
		 if($scope.idToken!=null){
			 return true;
		 }
		 else{
			 return false;
		 }
	 }
	 
    $scope.signOut = function signOut() {
		 $scope.manager=false;
		 $scope.admin=false;
		 $scope.user=false;
		 $scope.idToken=null;
		 $scope.profile=null;
		 $scope.employee=null;
		 $scope.tab="Schedule";
		 $scope.employeeTab="Schedule";
	    
		 var auth2 = gapi.auth2.getAuthInstance();
	     auth2.signOut().then(function () {});
    }
    $scope.setIdToken = function (idToken) {
        $scope.idToken = idToken;
    };
    $scope.setProfile = function (profile) {
        $scope.profile = profile;
    };
    $scope.setUser = function (isUser) {
        $scope.user = isUser;
    };
    $scope.setManager = function (isManager) {
        $scope.manager = isManager;
    };
    $scope.setAdmin = function (isAdmin) {
        $scope.admin = isAdmin;
    };
   
	 //Toast notifications 
	 $scope.notify = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=false;
		 $scope.showToast=true;
		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,5000);
	 }
	 
	 $scope.warn = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=true;
		 $scope.showToast=true;

		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,5000);
	 }
	 
	 $scope.hideToast = function(){
		 $scope.showToast=false;
		 $scope.$apply();
	 }
	 
	 $scope.autoHideToast = function(){
		 var d = new Date();
		 var n = d.getTime();
		 var difference = n-$scope.lastAlert;
		 if(difference>=5000){
			 $scope.showToast=false;
			 $scope.$apply();
		 }
		 else{
			 setTimeout($scope.autoHideToast,500);
		 }
	 }
	 
	 
	 //Utils
	 //Allows for date.addDays. It's fucking awesome
     Date.prototype.addDays = function(days) {
 	    var date = new Date(this.valueOf());
 	    date.setDate(date.getDate() + days);
 	    return date;
 	 }
     
     $scope.clone = function clone (obj) {
         return JSON.parse(JSON.stringify(obj));
     }
}
