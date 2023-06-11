package com.contact.manager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.contact.manager.dao.ContactRepository;
import com.contact.manager.dao.OrderRepository;
import com.contact.manager.dao.UserRepository;
import com.contact.manager.entities.Contact;
import com.contact.manager.entities.PaymentOrder;
import com.contact.manager.entities.User;
import com.contact.manager.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		
		model.addAttribute("user",user);
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact - Smart Contact Manager");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String username = principal.getName();
			User user = this.userRepository.getUserByUserName(username);
			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}else {
				//upload file to folder and name to contact
				File saveFile = new ClassPathResource("static/img").getFile();
				String fileName = String.valueOf(System.currentTimeMillis()) + "_" + file.getOriginalFilename();
				contact.setImage(fileName);
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			contact.setUser(user);
			
			user.getContacts().add(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your Contact is added !","success"));
		} catch (Exception e) {
			System.out.println("Error: "+e.getMessage());
			session.setAttribute("message", new Message("Something went wrong!","danger"));
			e.printStackTrace();
		}
		return "normal/add_contact";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal) {
		model.addAttribute("title","Your Contacts - Smart Contact Manager");
		try {
			String username = principal.getName();
			User user = this.userRepository.getUserByUserName(username);
			
			Pageable pageable =  PageRequest.of(page, 5);
			Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
			if(contacts!=null && !contacts.isEmpty()) {
				model.addAttribute("contacts",contacts);  
				model.addAttribute("currentPage",page);
				model.addAttribute("totalPages",contacts.getTotalPages());	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "normal/show_contacts";
	}
	
	@GetMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model,Principal principal) {
		try {
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			
			Optional<Contact> contactOpt = this.contactRepository.findById(cid);
			
			Contact contact = contactOpt.get();
			if(user.getId()==contact.getUser().getId()) {
				model.addAttribute("title",contact.getName() + " - Smart Contact Manager");
				model.addAttribute("contact",contact);
			}else {
				model.addAttribute("title","Not Found - Smart Contact Manager");
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("title","Not Found - Smart Contact Manager");
		}
		return "normal/contact_details";
	}

	@GetMapping("/contact/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal, HttpSession session) {
		try {
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			
			Optional<Contact> contactOpt = this.contactRepository.findById(cid);
			Contact contact = contactOpt.get();
			
			if(user.getId()==contact.getUser().getId()) {
				user.getContacts().remove(contact);
				this.userRepository.save(user);
				
				session.setAttribute("message", new Message("Contact deleted succesfuly..","success"));
			}else {
				session.setAttribute("message", new Message("Not able to delete contact..","danger"));
			}
		} catch (Exception e) {
			session.setAttribute("message", new Message("Not able to delete contact..","danger"));
			e.printStackTrace();
		}
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid")Integer cid,Model model) {
		model.addAttribute("title","Update Contact - Smart Contact Manager");	
		try {
			Contact contact = this.contactRepository.findById(cid).get();
			model.addAttribute("contact",contact);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "normal/update_contact";
	}
	
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				//delete old file 
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldContact.getImage()); 
				file1.delete();
				
				//upload new file to folder
				File saveFile = new ClassPathResource("static/img").getFile();
				String fileName = String.valueOf(System.currentTimeMillis()) + "_" + file.getOriginalFilename();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(fileName);
			}else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated!","success"));
			
		} catch (Exception e) {
			System.out.println("Error: "+e.getMessage());
			session.setAttribute("message", new Message("Something went wrong!","danger"));
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		model.addAttribute("title","Profle - Smart Contact Manager");
		User user = this.userRepository.getUserByUserName(principal.getName());
		model.addAttribute("user",user);
		
		return "normal/my_profile";
	}
	
	
	@GetMapping("/settings")
	public String openSettings(Model model,Principal principal) {
		model.addAttribute("title","Settings - Smart Contact Manager");
		User user = this.userRepository.getUserByUserName(principal.getName());
		model.addAttribute("user",user);
		
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword, HttpSession session,Principal principal) {
		User user = this.userRepository.getUserByUserName(principal.getName());
		System.out.println(oldPassword +" "+ newPassword);
		if(oldPassword==newPassword) {
			session.setAttribute("message", new Message("New and Old Passwords are same! Try again","warning"));			
		}
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password changed succesfuly","success"));
		}else {
			session.setAttribute("message", new Message("Old Password is wrong!","danger"));
		}
		
		return "normal/settings";
	}
	
	
	// Payment handlers
	
	@PostMapping("/create_order")
	@ResponseBody
	public String handelPayment(@RequestBody Map<String,Object> data, Principal principal) throws RazorpayException {
		try {
			int amt = Integer.parseInt(data.get("amount").toString());
			RazorpayClient razorpayClient = new RazorpayClient("rzp_test_V8DGMsJKerCd7z", "6fc2SbdhXTEHXUQhVOqjkjzo");
			
			JSONObject options = new JSONObject();
			options.put("amount", amt*100);
			options.put("currency", "INR");
			options.put("receipt", "txn_123456");
			Order order = razorpayClient.orders.create(options);
			
			//saving in database
			PaymentOrder pOrder = new PaymentOrder();
			pOrder.setAmount(amt +"");
			pOrder.setOrderId(order.get("id"));
			pOrder.setPaymentId(null);
			pOrder.setStatus("created");
			pOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
			pOrder.setReceipt(order.get("receipt"));
			
			this.orderRepository.save(pOrder);
			
			return order.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} 
		
	}

	
	@PostMapping("/update_order")
	public ResponseEntity<?> updatePayment(@RequestBody Map<String,Object> data) {
		try {
			PaymentOrder order = this.orderRepository.findByOrderId(data.get("order_id").toString());
			order.setPaymentId(data.get("payment_id").toString());
			order.setStatus(data.get("status").toString());
			this.orderRepository.save(order);
			
			return ResponseEntity.ok(Map.of("msg","updated"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}
}
