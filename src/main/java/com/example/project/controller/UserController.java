package com.example.project.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.project.dao.ContactRepository;
import com.example.project.dao.UserRepository;
import com.example.project.entity.Contact;
import com.example.project.entity.User;
import com.example.project.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		
		String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		System.out.println(user);
		
	}
	
	@GetMapping("/dashboard")
	public String dashboard(Model model,Principal pricipal) {
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
	@RequestMapping("/add-contact")
	public String openContectForm(Model model) {
		model.addAttribute("title", "Add Contect");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}	
	
	//Processing add contact form
	@PostMapping("/process-contact")
    public String processContact(@Valid @ModelAttribute Contact contact, @RequestParam("processImage") MultipartFile multipartFile, BindingResult bindingResult,
                                 Model model, Principal principal, HttpSession session) {

        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("contact", contact);
                return "normal/add_contact_form";
            }

            String userName = principal.getName();
            User user = this.userRepository.getUserByUserName(userName);

           

            // processing and uploading file
            if (multipartFile.isEmpty()) {
                //
                System.out.println("File not Uploaded");
                model.addAttribute("contact", contact);
                session.setAttribute("message", new Message("Please Select a Photo", "alert-danger"));
                return "normal/add_contact_form";

            } else {
            	Date date=new Date();
                contact.setImageUrl(date+multipartFile.getOriginalFilename());
                
                // image save to static folder
                File saveFile = new ClassPathResource("static/images/").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File is Uploaded");
            }
            contact.setUser(user);

            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Data: " + contact);
            model.addAttribute("contact", new Contact());

            /*Message Success*/
            session.setAttribute("message", new Message("Contact added Successfully!! ", "alert-success"));

            return "normal/add_contact_form"; 
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("contact", contact);
            /* Message Success for error */
            session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
        }

        return "normal/add_contact_form";

    }
	//Show Contacts
	@GetMapping("/show-conatacts")
	public String showContacts(Model model,Principal principal) {
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		List<Contact> contacts=this.contactRepository.findContactsByUser(user.getId());
		
		model.addAttribute("contacts", contacts);
		
		model.addAttribute("title", "View Contacts"); 
		return "normal/show_contacts";
	}
}
