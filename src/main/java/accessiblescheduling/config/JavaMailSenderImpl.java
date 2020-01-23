package accessiblescheduling.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

public class JavaMailSenderImpl implements EmailService {
  
    @Autowired
    public JavaMailSender emailSender;
 
    public void sendSimpleMessage(
      String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo(to); 
        message.setSubject(subject); 
        message.setText(text);
        emailSender.send(message);
    }
}
