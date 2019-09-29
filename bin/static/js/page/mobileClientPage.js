angular.module('client', ['ngResource', 'ui.bootstrap']).
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

function MobileClientController($scope, $modal, $http) {
	$scope.setTabAndInfo = function(newTab){
        $scope.setTab(newTab);
        $scope.updateClient();
        $scope.getAllClientCustomFieldData();
        $scope.listRequests();
        
        $scope.listShifts();

        $scope.listClients();
	}
	
	$scope.setClientAndInfo = function(newClient){
	  $scope.detailsChanged=false;
      $scope.setClient(newClient);
      $scope.getAllClientCustomFieldData();
      $scope.listRequests();
      
      $scope.listShifts();

      $scope.listClients();
	}
	
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	$scope.setDetailsToChanged = function(){
      $scope.detailsChanged=true;;
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
        shift.displayValue = (startHour!=0?startHour:"12")+":"+startMinute+startModifier+"-";
        shift.displayValue += (endHour!=0?endHour:"12")+":"+endMinute+endModifier;
		 
		if(shift.staffName!=null){
			 shift.displayValue+=" with "+shift.staffName;
		}
		 
		shift.displayValue+= ".";
     }
     
	$scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    }
    
    $scope.isClientSet = function(client){
        return client !==null && $scope.client !==null && client !==undefined && $scope.client !==undefined && $scope.client.id === client.id;
    }
      
    $scope.isClientChangeValid = function(client){
      let valid = true;
      
      if(client.first ==undefined || client.first.length<1){
    	  valid=false;
      }
      if(!$scope.detailsChanged){
    	  valid=false;
      }

      return valid;
    }
    
	 $scope.setInitialDays = function setInitialDays(request){
		 if(request.days==null || request.days==undefined){
			 request.days=[false,false,false,false,false,false,false];
		 }
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
        request.displayValue = (startHour!=0?startHour:"12")+":"+startMinute+startModifier+"-";
        request.displayValue += (endHour!=0?endHour:"12")+":"+endMinute+endModifier;
		 
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

	 $scope.listClientsAndInfo = function listClientsAndInfo() {
		$scope.listClients();
        if($scope.client==null){
        	$scope.setClientAndInfo($scope.clients[0]);
        }
        $scope.listEmployees();
    }

    function saveShift(shift) {
    	$http({
            url: '/shifts',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	param: shift
            }
        })
        .then(function(response) {
        	$scope.notify("Shift saved");
        	
            $scope.listShifts();
        });
    }
    
    $scope.addShift = function () {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/shiftForm.html',
            controller: ShiftModalController,
            resolve: {
            	idToken: function(){
         		   return $scope.clone($scope.idToken);
         	    },
            	shift: function(){
            		return {};
            	},
            	client: function(){
            		return $scope.clone($scope.client);
            	},
            	clients: function(){
            		return $scope.clone($scope.clients);
            	},
            	employees:function(){
            		return $scope.clone($scope.employees);
            	},
            	date: function(){
             	   return $scope.week;
                },
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (shift) {
        	if(shift.startYear==0 || shift.startYear == undefined || shift.startYear==null){
        		shift.startYear=shift.startDate.split("-")[0];
        	}
        	
        	if(shift.startMonth==0 || shift.startMonth == undefined || shift.startMonth==null){
        		shift.startMonth=shift.startDate.split("-")[1];
        	}
        	
            saveShift(shift);
        });
    }
    
    
    $scope.deleteShift = function (shift) {
    	$http({
            url: '/shifts/'+shift.id,
            method: 'DELETE',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function (response) {//TODO handle error state
            $scope.notify("Shift removed.");
            
            $scope.listEmployees();
        });
    }
 
    $scope.editRequest = function (selectedRequest,selectedClient,employees) {
      var editModal = $modal.open({
          templateUrl: 'templates/modal/requestForm.html',
          controller: RequestModalController,
          windowClass: 'app-modal-window',
          resolve: {
          	selectedClient: function(){
          		return $scope.clone(selectedClient);
          	},
          	employees: function(){
          		return $scope.clone(employees);
          	},
          	selectedEmployee: function(){
          		return selectedRequest.staffId;
          	},
            shiftRequest: function () {
            	return selectedRequest;
            },
            date:function(){
            	return "";
            },
            action: function() {
                return 'edit';
            }
          }
    });

    editModal.result.then(function (shiftRequest) {
   		$http({//TODO refactor to saveClientRequest method
               url: '/clientRequests',
               method: 'POST',
               headers: {
            	   'Authorization': $scope.idToken,
                   'Content-Type': 'application/x-www-form-urlencoded'
               },
               params: {
                   param: shiftRequest
               }
           })
           .then(function(response) {
           		$scope.notify("Request saved");
           		
        		$scope.requests = response.data;
           });
      	});
	}
  
    $scope.addRequest = function (selectedClient,employees) {
       var addModal = $modal.open({
           templateUrl: 'templates/modal/requestForm.html',
           controller: RequestModalController,
           windowClass: 'app-modal-window',
           resolve: {
        	client : function(){
        		return $scope.clone(selectedClient);
        	},
           	selectedClient: function(){
           		return $scope.clone(selectedClient);
           	},
           	employees: function(){
           		return $scope.clone(employees);
           	},
           	selectedEmployee: function(){
           		return "";
           	},
               shiftRequest: function () {
                   return {};
               },
               date: function(){
            	   return $scope.week;
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
    	            'Authorization': $scope.idToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                params: {
                    param: shiftRequest
                }
            })
            .then(function(response) {

            	$scope.notify("Request saved.");
            	$scope.requests = response.data;
            });
       });
   }
   
   function saveShiftRequest(shiftRequest) {
	   $http({
           url: '/clientRequests',
           method: 'POST',
           headers: {
               'Authorization': $scope.idToken,
               'Content-Type': 'application/x-www-form-urlencoded'
           },
           params: {
        	   param: shiftRequest
           }
       })
       .then(function (response) {//TODO handle error state
           $scope.notify("Request saved.");

           $scope.listShiftRequests();
       });
   }
   
   $scope.editShift = function (shift) {
       var updateModal = $modal.open({
           templateUrl: 'templates/modal/shiftForm.html',
           controller: ShiftModalController,
           resolve: {
        	    idToken: function(){
        	    	return $scope.clone($scope.idToken);
        	    },
                shift: function() {
                	return $scope.clone(shift);
                },
                client: function(){
                	return $scope.clone($scope.selectedClient);
                },
                clients: function(){
                	return {};
	            },
	            employees:function(){
	        		return $scope.clone($scope.employees);
	            },
	           	date: function(){
	           		return $scope.week;
	            },
                action: function() {
                	return 'update';
                }
           }
       });

       updateModal.result.then(function (shift) {
		   saveShift(shift);
           $scope.listShifts();
       }, function () {
           $scope.listShifts();
       });
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
           		return $scope.clone(selectedClient);
           	},
               shiftRequest: function() {
                   return $scope.clone(shiftRequest);
               },
           	employees: function(){
           		return $scope.clone(employees);
           	},
           	selectedEmployee: function(){
           		if(selectedEmployee!=null && selectedEmployee.length>0){
           			return $scope.clone(selectedEmployee[0]);
           		}
           		return "";
           	},
           	date: function(){
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
   }
   
   $scope.deleteShiftRequest = function (shiftRequest) {
	   if(confirm("Are you sure to delete info for the following request?"+shiftRequest.displayValue)){
		   $http({
               url: '/schedule/deleteRequest',
               method: 'GET',
               headers: {
            	   'Authorization': $scope.idToken,
                   'Content-Type': 'application/x-www-form-urlencoded'
               },
               params: {
                   id: shiftRequest.id
               }
           })
           .then(function(response) {
        	   	$scope.notify("Request removed.");
           		$scope.requests = response.data;
           });
	   }
    }
    
    $scope.newClient = function () {
    	$http({
            url: '/schedule/createClient',
            method: 'POST',
            headers: {
	            'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
                $scope.notify("Client created");
        		$scope.clients = response.data;
        		$scope.tab="Schedule";
        	}
        	else{
        		$scope.warn("Failed to save client info.")
        	}
        });
    }
    
    $scope.ok = function () {
    	var originalClient = $scope.clone($scope.unmodifiedClient);
    	var modifiedClient = $scope.clone($scope.client);
    	var lastUpdated = originalClient.lastUpdated;
    	if(null==lastUpdated || undefined == lastUpdated){
    		lastUpdated="null";
    	}
    	if(modifiedClient!=$scope.unmodifiedClient){
		   $http({
	            url: '/schedule/clientWasUpdated',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	lastUpdated:lastUpdated,
	            	clientId:originalClient.id
	            }
	        })
	        .then(function(response) {
	        	var updateData=false;
    	    	if(response.data=="UPDATED"){
    	    		   if(confirm(modifiedClient.first+" has just been modified by another user. Saving your changes will overwrite thier updates. Would you " +
    	    		   				"still like to save your changes?")){
    	    			   updateData=true;
    	    		   }else{
    	    			   $scope.cancel();
    	    		   }
    	    	}
    	    	else{
    	    		updateData=true;
    	    	}
    	    	
    	    	if(updateData){
    		  		$scope.detailsChanged=false;
    		  		
    		        var size = $scope.customFields.length;
    		        for(var i = 0; i < size ;i++){
    		            $http({
    		                url: '/compatibility/setClientCustomFieldData',
    		                method: 'POST',
    		                headers: {
    		    	            'Authorization': $scope.idToken,
    		                    'Content-Type': 'application/x-www-form-urlencoded'
    		                },
    		                params: {
    		                    client: $scope.client,
    		                    customField: $scope.customFields[i],
    		                    value:$scope.customValue[i],
    		                }
    		            });
    		        }
    		        
    		    	$http({
    		            url: '/schedule/updateClient',
    		            method: 'POST',
    		            headers: {
    			            'Authorization': $scope.idToken,
    		                'Content-Type': 'application/x-www-form-urlencoded'
    		            },
    		            params: {
    		                param: $scope.client
    		            }
    		        })
    		        .then(function(response) {
    		        	if(response.data){
    		                $scope.notify("Client saved");
    		                
    		            	$scope.detailsChanged=false;
    		            	$scope.updateClient();
    		            	$scope.getAllClientCustomFieldData();
    		        	}
    		        	else{
    		        		$scope.warn("Failed to save client info.")
    		        	}
    		        });
    	    	}
	        });
    	}
    }
    
    $scope.deleteShift = function (shift) {
    	console.log("deleteing shift");
    	console.log(shift);
    	var lastUpdated = (shift.lastUpdated!=null?shift.lastUpdated:"null");
    	$http({
            url: '/schedule/shiftWasUpdated',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	lastUpdated:lastUpdated,
            	shiftId:shift.id
            }
        })
        .then(function(response) {
        	var deleteShift=false;
        	
        	if(response.data=="UPDATED"){
    		   if(confirm("This shift has just been modified by another user. Deleteing this shift will overwrite thier updates. Would you " +
    		   				"still like to delete this shift?")){
    			   deleteShift=true;
    		   }
    		   else{
    			   $scope.listShifts();
    		   }
        	}
    		else if(confirm("Are you sure you want to delete the following shift? "+shift.display)){
    			deleteShift=true;
    		}
        	
        	if(deleteShift){
             	$http({
                    url: '/shifts/'+shift.id,
                    method: 'DELETE',
                    headers: {
                       'Authorization': $scope.idToken,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    params: {
                    }
                })
                .then(function(response) {
                	if(response){
                        $scope.notify("Shift deleted.");
                        $scope.listShifts();
                	}
                	else{
                		$scope.warn("Failed to delete shift info.")
                	}
                });
        	}
    	});
     };
     
    $scope.delete = function () {
 	   if(confirm("Are you sure you want to delete info for "+$scope.client.first + " "+$scope.client.initial+"?")){
    	$http({
            url: '/clients/'+$scope.client.id,
            method: 'DELETE',
            headers: {
	            'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response){
                $scope.notify("Client deleted.");
        		$scope.clients =response.data;
        		$scope.client =response.data[0];
        		$scope.tab="Schedule";
        	}
        	else{
        		$scope.warn("Failed to delete client info.")
        	}
        });
 	   }
    }
    
    $scope.updateClient = function () {
        $http({
            url: '/clients/'+$scope.client.id,
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
        		$scope.setClient(response.data);
        	}
        });
    }
    
    $scope.cancel = function () {
    	$scope.detailsChanged=false;
    	$scope.updateClient();
    	$scope.getAllClientCustomFieldData();
    }
}
