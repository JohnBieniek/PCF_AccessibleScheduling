package accessiblesolutions.accessiblescheduling.util;

import javax.security.sasl.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionMapper {
   @ExceptionHandler(value = AuthenticationException.class)
   public ResponseEntity<String> exception(AuthenticationException exception) {
	  System.out.println("Auto handling exception"+exception.getMessage());
      return new ResponseEntity<String>("User does not meet the required role to access this data", HttpStatus.FORBIDDEN);
   }
}
