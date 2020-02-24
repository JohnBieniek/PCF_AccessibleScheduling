package accessiblescheduling.controller;

import java.io.IOException;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.security.sasl.AuthenticationException;

import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import accessiblescheduling.constants.Constants;
import accessiblescheduling.manager.AccessibleSecurityManager;
import accessiblescheduling.manager.AlertManager;
import accessiblescheduling.to.User;

@RestController
@RequestMapping(value = "/auth")
public class SecurityController {
	@Autowired
	AccessibleSecurityManager manager;

	@Autowired
	AlertManager alertManager;

	@RequestMapping(value = "/user/registration", method = RequestMethod.GET)
	public ModelAndView registerUserAccount(WebRequest request, Model model) {
		return new ModelAndView("badUser.html", "message", "some information");
	}

	@RequestMapping("/to-be-redirected")
	public RedirectView localRedirect() {
		RedirectView redirectView = new RedirectView();
		String value1="DickButtMcGee";
		redirectView.setAttributesCSV("invitation={"+value1+"}");
		redirectView.setUrl("https://accessiblescheduling-dev.cfapps.io/signup");
		return redirectView;
	}

//	@RequestMapping(value = "/regitrationConfirm", method = RequestMethod.GET)
//	public String confirmRegistration(WebRequest request, Model model, @RequestParam("token") String token) {
//
//		Locale locale = request.getLocale();
//
//		// VerificationToken verificationToken = service.getVerificationToken(token);
//		// if (verificationToken == null) {
//		// String message = messages.getMessage("auth.message.invalidToken", null,
//		// locale);
//		model.addAttribute("invitation", token);
//		return "redirect:/signUp.html?lang=" + locale.getLanguage();
//		// }
//		//
//		// User user = verificationToken.getUser();
//		// Calendar cal = Calendar.getInstance();
//		// if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime())
//		// <= 0) {
//		// String messageValue = messages.getMessage("auth.message.expired", null,
//		// locale)
//		// model.addAttribute("message", messageValue);
//		// return "redirect:/badUser.html?lang=" + locale.getLanguage();
//		// }
//		//
//		// user.setEnabled(true);
//		// service.saveRegisteredUser(user);
//		// return "redirect:/login.html?lang=" + request.getLocale().getLanguage();
//	}

	@RequestMapping(value = "/tokensignin", method = RequestMethod.GET)
	public User tokenSignIn(@RequestHeader(value = "Authorization", required = false) String idToken) throws Exception {
		User user = manager.getUserDetails(idToken);
		return user;
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signUp2(@RequestParam String idtoken,@RequestParam String invitation) throws AuthenticationException {
		return manager.signUp(idtoken,invitation).toString();
	}

	@RequestMapping(value = "/linkEmployee", method = RequestMethod.GET)
	public String linkEmployee(@RequestParam String idtoken, @RequestParam String email,
			@RequestParam String employeeId) throws AddressException, MessagingException, IOException, JSONException {
		return manager.linkEmployee(email, employeeId).toString();
	}

	@RequestMapping(value = "/approve", method = RequestMethod.GET)
	public String approve(@RequestHeader(value = "Authorization", required = false) String idToken,
			@RequestParam String userId, String employeeId) throws AuthenticationException {
		manager.authorize(idToken, Constants.ADMIN);

		return manager.approve(userId, employeeId);
	}

	@RequestMapping(value = "/deny", method = RequestMethod.GET)
	public String deny(@RequestHeader(value = "Authorization", required = false) String idToken,
			@RequestParam String userId) throws AuthenticationException {
		manager.authorize(idToken, Constants.ADMIN);

		return manager.deny(userId);
	}

	@RequestMapping(value = "/signedup", method = RequestMethod.GET)
	public Boolean signUp2(@RequestHeader(value = "Authorization", required = false) String idToken)
			throws AuthenticationException {
		return manager.signedUp(idToken);
	}
}
