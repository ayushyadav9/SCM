package com.contact.manager.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.manager.dao.UserRepository;
import com.contact.manager.entities.User;
import com.contact.manager.helper.EmailService;
import com.contact.manager.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotPasswordController {
	Random random = new Random(1000);
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/forgot")
	public String openEmailForm(Model model) {
		model.addAttribute("title","Forgot Password - Smart Contact Manager");
		return "forgot_password";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email, Model model,HttpSession session) {
		
		try {
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message", new Message("The User is not registered!","warning"));
				return "forgot_password";
			}
			
			int otp = random.nextInt(999999);
			model.addAttribute("title","Enter OTP details");
			String subject = "OTP from Smat Contact Manager";
			String message = "OTP = " + otp ;
			String to = email;
			
			boolean flag =  this.emailService.sendMail(subject, message, to);
			
			if(flag) {
				session.setAttribute("myOTP", otp);
				session.setAttribute("email", email);
				session.setAttribute("message", new Message("Otp sent succesfuly!","success"));
				return "verify_otp";
			}
			
			session.setAttribute("message", new Message("Invalid Email","danger"));
			return "forgot_password";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "forgot_password";
	}
	
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp")int otp,HttpSession session,Model model) {
		model.addAttribute("title","Enter OTP details");
		int myOTP = (int)session.getAttribute("myOTP");
		
		if(myOTP == otp) {
			model.addAttribute("title","Change Password");
			return "password_change_form";
		}else {
			session.setAttribute("message", new Message("You have entered wrong OTP! Please try again","danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword")String newPassword,HttpSession session,Model model) {
		model.addAttribute("title","Enter New Password");
		
		try {
			String email = (String)session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
				
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password updated succesfuly","succes"));
			return "redirect:/signin";
		} catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong","danger"));
			e.printStackTrace();
		}
		return "password_change_form";
	}
}
