package org.cloudfoundry.samples.music.web;
import java.io.IOException;
import java.util.Collections;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpTransport;


@RestController
@RequestMapping(value = "/auth")
public class SecurityController {
	private static final String CLIENT_ID = "728962972693-5uudecgh1ir6if7imo2hb6cv9qa9nrk3728962972693-5uudecgh1ir6if7imo2hb6cv9qa9nrk3.apps.googleusercontent.com";



	// (Receive idTokenString by HTTPS POST)
	@RequestMapping(value = "/tokensignin",method = RequestMethod.POST)
    public String staffShift(@RequestParam String idtoken)throws Exception {
    	Shift shift =null;

    	System.out.println("idtoken:"+idtoken);
    	
    	GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
    		    .setAudience(Collections.singletonList(CLIENT_ID))
    		    .build();
    	System.out.println("verifier:"+verifier);    	
    	GoogleIdToken gIdToken = verifier.verify(idtoken);
    	System.out.println("gIdToken:"+gIdToken);
    	if(gIdToken==null) {
    		System.out.println("calling :"+"https://www.googleapis.com/oauth2/v2/tokeninfo?id_token="+idtoken);
    		 RestTemplate restTemplate = new RestTemplate();
    	     String json = restTemplate.getForObject("https://www.googleapis.com/oauth2/v2/tokeninfo?id_token="+idtoken, String.class);
    	    	System.out.println("token info:"+json);
    		return json;
    	}
    	if (gIdToken != null) {
    	  Payload payload = gIdToken.getPayload();

    	  // Print user identifier
    	  String userId = payload.getSubject();
    	  System.out.println("User ID: " + userId);

    	  // Get profile information from payload
    	  String email = payload.getEmail();
    	  boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
    	  String name = (String) payload.get("name");
    	  String pictureUrl = (String) payload.get("picture");
    	  String locale = (String) payload.get("locale");
    	  String familyName = (String) payload.get("family_name");
    	  String givenName = (String) payload.get("given_name");

    	  // Use or store profile information
    	  // ...
    	  return name;
    	} else {
    	  System.out.println("Invalid ID token.");
    	  return "couldn't get name";
    	}
    }
}
