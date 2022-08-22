package com.example.project.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	private UserRepository userRepository2;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		System.out.println(user);

	}

	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal pricipal) {
		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	@RequestMapping("/add-contact")
	public String openContectForm(Model model) {
		model.addAttribute("title", "Add Contect");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// Processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact,
			@RequestParam("processImage") MultipartFile multipartFile, BindingResult bindingResult, Model model,
			Principal principal, HttpSession session) {

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
				contact.setImageUrl("contact.jpg");
				model.addAttribute("contact", contact);
				session.setAttribute("message", new Message("Please Select a Photo", "alert-danger"));
				return "normal/add_contact_form";

			} else {

				contact.setImageUrl(multipartFile.getOriginalFilename());

				// image save to static folder
				File saveFile = new ClassPathResource("static/images/").getFile();

				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is Uploaded");
			}
			contact.setUser(user);

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			contact.setDate(dtf.format(now));
			user.getContacts().add(contact);

			this.userRepository.save(user);
			System.out.println("Data: " + contact);
			model.addAttribute("contact", new Contact());

			/* Message Success */
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

	// Show Contacts
	// per page=n[5]
	// current page=0
	@GetMapping("/show-conatacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 2);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);

		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "View Contacts");
		return "normal/show_contacts";
	}

	// show particular contact details
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println(cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// update contact detail's form open
	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Model model) {

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("title", "Update Contact");
		model.addAttribute("contact", contact);

		return "normal/update_conatact";
	}

	// update contact form process
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateProcess(@ModelAttribute Contact contact,
			@RequestParam("processImage") MultipartFile multipartFile, Model model, Principal principal,
			HttpSession session) {

		try {
			// old contact details
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			
			// processing and uploading file
			if (!multipartFile.isEmpty()) {

				// delete old photo
				File deleteFile = new ClassPathResource("static/images/").getFile();
				File file1 = new File(deleteFile, oldContact.getImageUrl());
				boolean isdelete = file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/images/").getFile();
				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImageUrl(multipartFile.getOriginalFilename());
			} else {
				contact.setImageUrl(oldContact.getImageUrl());
				contact.setDate(oldContact.getDate());
				System.out.println(oldContact.getDate());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		User user = this.userRepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		this.contactRepository.save(contact);

		/* Message Success */
		session.setAttribute("message", new Message("Your contact is updated... ", "alert-success"));
		System.out.println(contact.getName());
		System.out.println("id"+contact.getcId());
		System.out.println(contact.getDate());
		
		return "redirect:/user/contact/"+contact.getcId();
		
	}

	// delete contact detail's
	@GetMapping("/delete-contact/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		if (contactOptional.isPresent()) {
			Contact contact = contactOptional.get();
			// get current user
			String username = principal.getName();
			User user = this.userRepository.getUserByUserName(username);

			// get contact only current user
			if (user.getId() == contact.getUser().getId()) {

				user.getContacts().remove(contact);

				this.userRepository.save(user);
				System.out.println("deleted");
				session.setAttribute("message", new Message("Contact deleted Successfully...", "alert-success"));
			}
		}

		return "redirect:/user/show-conatacts/0";
	}

	// user profile detail's
	@GetMapping("/user-profile")
	public String userProfile(Model model) {

		model.addAttribute("title", "User Profile");
		return "normal/user_profile";
	}
	
	//update user profile
	@PostMapping("/update-profile/{id}")
	public String userProfileUpdate(@PathVariable("id")Integer id,Model model) {
		User user = this.userRepository.findById(id).get();
		model.addAttribute("title", "Update Profile");
		model.addAttribute("user",user );
		
		return"normal/update_user_profile";
	
	}
	
	// update contact form process
	@RequestMapping(value = "/process-update-user", method = RequestMethod.POST)
	public String process_update_UserProfile(@ModelAttribute User user,
			@RequestParam("processImage") MultipartFile file, Model model, Principal principal,
			HttpSession session) {

		try {
			// old user details
			User oldUser = this.userRepository.findById(user.getId()).get();
			
			// processing and uploading file
			if (!file.isEmpty()) {

				// delete old photo
				File deleteFile = new ClassPathResource("static/images/").getFile();
				File file1 = new File(deleteFile, oldUser.getImageUrl());
				boolean isdelete = file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/images/").getFile();
				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				user.setImageUrl(file.getOriginalFilename());
			} else {
				user.setImageUrl(oldUser.getImageUrl());
//				contact.setDate(oldContact.getDate());
//				System.out.println(oldContact.getDate());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		User user1 = this.userRepository.getUserByUserName(principal.getName());
		
		this.userRepository.save(user1);

		/* Message Success */
		session.setAttribute("message", new Message("Your contact is updated... ", "alert-success"));
		System.out.println(user.getName());
		System.out.println("id"+user.getId());
		System.out.println(user.getDate());
		
		return "redirect:/user/user-profile";
		
	}

}
