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
	/**
	 * Changes what client info we are looking at and gets all info for that new client
	 * Interaction notification is done in setTab
	 */
	$scope.setTabAndInfo = function(newTab){
        $scope.setTab(newTab);
        $scope.updateClient();
        $scope.getAllClientCustomFieldData();
        $scope.listRequests();
        
        $scope.listShifts();

        $scope.listClients();//Consider removing and all auto update to do this
	}
	
	/**
	 * Interaction notification is done in setClient
	 */
	$scope.setClientAndInfo = function(newClient){
	  $scope.setDetailsChanged(false);
      $scope.setSelectedClient(newClient);
      $scope.getAllClientCustomFieldData();//Done now for visual crispness
      
	  if($scope.tab=="Requests"){		 
		  $scope.listRequests();
	  }
	  else if($scope.tab == "Schedule"){
		  $scope.listShifts();
	  }

      $scope.listClients();//Consider removing and all auto update to do this
	}
	
	//May be used in checking if a shift or request is valid, TODO factor out
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	/**
	 * Called whenever something in the client object is changed.
	 * Used to show the okay and cancel buttons at the bottom of the screen to save the changes
	 */
	$scope.setDetailsToChanged = function(){
	  $scope.updateLastInteractionTime();
      $scope.setDetailsChanged(true);
	}
	
    /**
     * Updates the provided object with .displayValue containing a human readable string of what this shift is for.
     * Differs from the client version in its description of who this is for.
     */
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
     
     /**
      * Determines if the given tab should have the selected style applied
      */
	$scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    }
    
    /**
     * Determines if the given tab should have the selected style applied
     */
    $scope.isClientSet = function(client){
        return client !==null && $scope.client !==null && client !==undefined && $scope.client !==undefined && $scope.client.id === client.id;
    }
  
    /**
     * Determines if the ok button can pop up in client details
     */
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
	 
	/**
	 * Enters or updates the provided shift in the database.
	 * Requires a unique version per page due to the differences in how shifts are listed afterward.
	 */
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
    	$scope.updateLastInteractionTime();
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
        	$scope.updateLastInteractionTime();
            saveShift(shift);
        });
    }
 
    $scope.addRequest = function (selectedClient,employees) {
       $scope.updateLastInteractionTime();
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
               },
				idToken: function(){
        	    	return $scope.clone($scope.idToken);
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
            	$scope.updateLastInteractionTime();
            	$scope.notify("Request saved.");
            	$scope.setRequests(response.data);
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

           $scope.listRequests();
       });
   }
   
   $scope.editShift = function (shift) {
	   $scope.updateLastInteractionTime();
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
        	var updateData=true;

        	if(response.data=="DELETED"){
        	   updateDate=false;
	    	   $scope.warn("This shift has just been deleted by another user. If you still wish to make edits please make a new shift.");
   	    	}
   	    	
   	    	if(updateData){
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
		           $scope.updateLastInteractionTime();
		       }, function () {
		           $scope.listShifts();
		           $scope.updateLastInteractionTime();
		       });
   	    	}
        });
   }
   
   $scope.updateShiftRequest = function (selectedClient, shiftRequest,employees) {
	   $scope.updateLastInteractionTime();
	   var lastUpdated = (shiftRequest.lastUpdated!=null?shiftRequest.lastUpdated:"null");
	   $http({
            url: '/schedule/requestWasUpdated',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	lastUpdated:lastUpdated,
            	requestId:shiftRequest.id
            }
        })
        .then(function(response) {
        	if(response.data=="DELETED"){
	    	   $scope.warn("This request has just been deleted by another user. If you still wish to make edits please make a new shift.");
	    	   $scope.listRequests();
   	    	}
        	else{
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
						},
						idToken: function(){
		        	    	return $scope.clone($scope.idToken);
		        	    }
					}
				});
						
				updateModal.result.then(function (shiftRequest) {
					saveShiftRequest(shiftRequest);
    			    $scope.listRequests();
    			    $scope.updateLastInteractionTime();
				}, function () {
					$scope.updateLastInteractionTime();
			        $scope.listRequests();
		        });
   	    	}
        });

   }
   
   $scope.deleteShiftRequest = function (shiftRequest) {
	    $scope.updateLastInteractionTime();
	   	var lastUpdated = (shiftRequest.lastUpdated!=null?shiftRequest.lastUpdated:"null");
    	$http({
            url: '/schedule/requestWasUpdated',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	lastUpdated:lastUpdated,
            	requestId:shiftRequest.id
            }
        })
        .then(function(response) {
        	var deleteRequest=false;
        	
        	if(response.data=="UPDATED"){
    		   if(confirm("This request has just been modified by another user. Deleteing this request will overwrite thier updates. Would you " +
    		   				"still like to delete this request?")){
    			   deleteRequest=true;
    		   }
    		   else{
    			   $scope.listRequests();
    		   }
        	}
    		else if(confirm("Are you sure you want to delete this request?")){
    			deleteRequest=true;
    		}
        	
        	if(deleteRequest){
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
    			   $scope.listRequests();
    			   $scope.updateLastInteractionTime();
	           });
        	}
	   });
    }
    
   /**
    * Creates a new client with name A Client and refreshes the client list to contain the change
    */
    $scope.newClient = function () {
    	$scope.updateLastInteractionTime();
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
    
    /**
     * Checks to see if we have the latest info in this client.
     * If we don't we ask if they want to overwrite what's on the server or see what the changes are.
     * If the choose to proceed or if we had the latest data we save the client and custom field data.
     */
    $scope.ok = function () {
    	$scope.updateLastInteractionTime();
    	var originalClient = $scope.clone($scope.unmodifiedClient);
    	var modifiedClient = $scope.clone($scope.client);
    	var lastUpdated = modifiedClient.lastUpdated;
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
    	    		   if(confirm(modifiedClient.name+" has just been modified by another user. Saving your changes will overwrite thier updates. Would you " +
    	    		   				"still like to save your changes?")){
    	    			   updateData=true;
    	    			   $scope.updateLastInteractionTime();
    	    		   }else{
    	    			   $scope.updateLastInteractionTime();
    	    			   $scope.cancel();
    	    		   }
    	    	}
    	    	else{
    	    		updateData=true;
    	    	}
    	    	
    	    	if(updateData){
    		  		$scope.setDetailsChanged(false);
    		  		
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
    		                
    		            	$scope.setDetailsChanged(false);//TODO try removing
    		            	$scope.updateClient();
    		            	$scope.getAllClientCustomFieldData();
    		        	}
    		        	else{
    		        		$scope.warn("Failed to save client info.")
    		        	}
    		        });
    	    	}
	        })
	        .catch(function(data, status) {
            	console.error('Error', data,status);
        		$scope.warn("Failed to save client info.")
    		});
    	}
    }
    
    /**
     * Checks to see if we have the latest info in this shift.
     * If we don't we ask if they want to delete anyways or see what the changes are.
     * If the choose to proceed or if we had the latest data we delete the shift.
     */
    $scope.deleteShift = function (shift) {
    	$scope.updateLastInteractionTime();
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
    		else if(confirm("Are you sure you want to delete this shift?")){
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
    	$scope.updateLastInteractionTime();
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
            	$scope.setDetailsChanged(false);
        		$scope.tab="Schedule";
        	}
        	else{
        		$scope.warn("Failed to delete client info.")
        	}
        });
 	   }
    }
    
    /**
     * Updates the local copy of client to the server version
     */
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
            	$scope.setDetailsChanged(false);
        	}
        });
    }
    
    $scope.cancel = function () {
    	$scope.updateLastInteractionTime();
    	$scope.setDetailsChanged(false);
    	$scope.updateClient();
    	$scope.getAllClientCustomFieldData();
    }
}
