package com.contact.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.manager.dao.UserRepository;
import com.contact.manager.entities.User;
import com.contact.manager.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signin")
	public String signin(Model model) {
		model.addAttribute("title","Login - Smart Contact Manager");
		return "login";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","SignUp - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")
		User user,BindingResult result1, @RequestParam(value="agreement",defaultValue = "false") 
		boolean agreement,Model model, HttpSession session) {
		try {
			if(result1.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			if(!agreement) {
				System.out.println("Please agree Terms & Conditions");
				throw new Exception("Please agree Terms & Conditions");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			
			System.out.println(result);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("User registered succesfuly","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong "+ e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
	}
}
