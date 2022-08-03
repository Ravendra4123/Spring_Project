package com.example.project.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.dao.UserRepository;
import com.example.project.entity.User;
import com.example.project.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@RequestMapping("/")
	public String home(Model model){
		model.addAttribute("title", "Home Page");
		User user=new User();
				
		
		return "home";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Sign Up Page");
		model.addAttribute("user",new User());
		return "signup";
	} 
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About Us");
		return"about";
	}
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
	
	@PostMapping("/do_register")
	public String userRegistration(
			@Valid @ModelAttribute("user") User user,BindingResult bindingResult,
            Model model,
            HttpSession session) {
		try {
			
			if(bindingResult.hasErrors()) {
				System.out.println("Error"+bindingResult.toString());
				model.addAttribute("user",user);
				return "signup";	
			}else {
			
			user.setRole("ROLE_USER");
			user.setAbout("I am a normal user.");
            user.setEnabled(true);
            user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			
			User result=this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Registration Successful.","alert-success"));

			System.out.println(user);
			return "signup";
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong !"+e.getMessage(),"alert-danger"));
			
			return "signup";
		}
		
	}

}
