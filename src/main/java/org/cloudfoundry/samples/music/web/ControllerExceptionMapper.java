package org.cloudfoundry.samples.music.web;


import javax.security.sasl.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
 
@ControllerAdvice
public class ControllerExceptionMapper {
// 
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity handleMyException(AuthenticationException mex) {
//    	ResponseEntity<?> response = new ResponseEntity(HttpStatus.FORBIDDEN);
//    	
//        return response;
//    }
// 
//    @ExceptionHandler(Exception.class)
//    public ModelAndView handleException(Exception ex) {
// 
//        ModelAndView model = new ModelAndView();
//        model.addObject("errMsg", "This is a 'Exception.class' message.");
//        model.setViewName("error/generic_error");
//        return model;
// 
//    }
}