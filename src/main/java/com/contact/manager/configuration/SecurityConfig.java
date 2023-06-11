package com.contact.manager.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig{
	
	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider getProvider()
	{
		DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
		provider.setUserDetailsService(this.getUserDetailsService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}
	
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.authorizeHttpRequests()
			.requestMatchers("/admin/**").hasRole("ADMIN")
			.requestMatchers("/user/**").hasRole("USER")
			.requestMatchers("/**").permitAll()
			.and()
			.formLogin()
			.loginPage("/signin")
			.loginProcessingUrl("/do-login")
			.defaultSuccessUrl("/user/profile")
			.and()
			.csrf().disable();
		
		httpSecurity.formLogin().defaultSuccessUrl("/user/profile",true);
		
		return httpSecurity.build();

	}
	
}