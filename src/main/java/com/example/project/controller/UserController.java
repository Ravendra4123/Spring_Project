package com.example.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		model.addAttribute("title","User Dashboard" );
		
		return "normal/user_dashboard";
	}
}
