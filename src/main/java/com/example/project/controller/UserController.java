package com.example.project.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.project.dao.UserRepository;
import com.example.project.entity.User;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	UserRepository userRepository;
	
	@GetMapping("/dashboard")
	public String dashboard(Model model,Principal pricipal) {
		
		model.addAttribute("title","User Dashboard" );
		String userName=pricipal.getName();
		
		User user=userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		System.out.println(user);
		return "normal/user_dashboard";
	}
}
