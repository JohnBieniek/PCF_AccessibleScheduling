package accessiblescheduling;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests().antMatchers("/*").permitAll().and()
//			.authorizeRequests()
//				.anyRequest().authenticated()
//				.and()
			.formLogin()
				.loginPage("/login.html").permitAll().defaultSuccessUrl("/index.html", true)
				.and()
			.httpBasic()
				.and()
            .csrf().disable();
	}

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	Map<String, String> env = System.getenv();
    	String username = env.get("temp1") == null ? "admin" : env.get("temp1");
    	String password = env.get("temp2") == null ? "password" : env.get("temp2");
        auth
            .inMemoryAuthentication()
                .withUser(username).password(password).roles("USER");
    }
}
