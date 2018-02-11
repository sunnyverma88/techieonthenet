package org.techieonthenet.jba.controller;

import java.lang.invoke.MethodType;
import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import java.util.ArrayList;
import javax.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.techieonthenet.jba.entity.Blog;
import org.techieonthenet.jba.entity.ForgotPwd;
import org.techieonthenet.jba.entity.User;
import org.techieonthenet.jba.service.BlogService;
import org.techieonthenet.jba.service.UserService;
import org.techieonthenet.jba.utility.EmailSender;

@Controller
public class UserController {

	@Autowired
	private UserService us;
	
	@Autowired
	private BlogService bs;
        
         @Autowired
        private EmailSender es;
	
	@ModelAttribute
	public void constructModelAttributes(Model model)
	{
		User user= new User();
		Blog blog=new Blog();
		ForgotPwd fp=new ForgotPwd();
		model.addAttribute("fp", fp);
		model.addAttribute("user",user);
		model.addAttribute("blog",blog);
	}
	
	
	
	
	@RequestMapping("/users")
	public String Users(Model model)
	{
		model.addAttribute("users",us.findAll());
		return "users";
	}
	@RequestMapping("/users/{id}/{pageNumber}")
	public String details(Model model,@PathVariable int id,@PathVariable Integer pageNumber)
	{   
		
		model.addAttribute("user",us.findOneWithBlogs(id));
		Page<Blog> userblogs=bs.getBlogsbyUser(us.findOneWithBlogs(id),pageNumber);
		int current = userblogs.getNumber() + 1;
	    int begin = Math.max(1, current - 5);
	    int end = Math.min(begin + 10, userblogs.getTotalPages());
	    model.addAttribute("beginIndex", begin);
	    model.addAttribute("endIndex", end);
	    model.addAttribute("currentIndex", current);
		model.addAttribute("blogs",userblogs);
		return "account";
		
		
	}
	
	@RequestMapping("/register")
	public String showRegister()
	{
		return "user-register";
	}
	
	@RequestMapping("/user/update/{id}")
	public String showUpdate(Model model,@PathVariable("id") int id)
	{
		User user= us.findOne(id);
		model.addAttribute("user", user);
		return "editUser";
	}
	
	@RequestMapping(value="/register" , method=RequestMethod.POST)
	public String doRegister(@Valid @ModelAttribute("user")User user,BindingResult result)
	{   
		boolean response;
		
			if(result.hasErrors())
			{  
				return "user-register";
				
			}
			us.save(user);
			response=true;
		    return "redirect:/register?success="+response;
	}
		
	
	@RequestMapping(value="/users/remove/{id}")
	public String removeUser(Model model,@PathVariable("id") int id)
	{   
		us.delete(id);
		return "redirect:/users";
	}
	
	@RequestMapping(value="/user/forgotpwd")
	public String forgotPwd()
	{   
		return "forgotPwd";
	}
	
	@RequestMapping(value="/user/regeneratePwd" , method=RequestMethod.POST)
	public String regeneratePwd( @ModelAttribute("fp")ForgotPwd fp,@RequestParam("email")String email,Model model) throws MessagingException, Exception
	{   
		System.out.println(fp.getEmail());
                boolean response;
                User user=us.findOneByEmail(fp.getEmail());
                if(user!=null)
                {   
                    String newPwd=RandomStringUtils.randomAlphanumeric(10);
                    user.setPassword(newPwd);
                    us.save(user);
                    String body="<html><p>Thanks for contacting TechieOnTheNet. </p>" + "<p>Email Address - "+fp.getEmail()+"</p><p>Password :-"+ newPwd +" </p></html>";
	            EmailSender.sendEmail(fp.getEmail(),"Techieonthenet Forgot Password ",body);
                    model.addAttribute("message" , "Please check your email for the password.If you don't find it in your inbox,please check your Spam folder.");
                    response=true;
                }
                else
                {
                model.addAttribute("message" , "User does not exists");
                 response=false;
                }
             
             return "redirect:/user/forgotpwd?success="+response;
        }
	
	
}
