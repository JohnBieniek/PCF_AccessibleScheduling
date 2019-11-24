package accessiblescheduling.proxy;

import javax.security.sasl.AuthenticationException;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import accessiblescheduling.to.User;

@Component
public class SecurityProxy {
	RestTemplate restTemplate = new RestTemplate();
	
	public SecurityProxy() {
    }
	

}
