package com.gograffer.watertracker.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gograffer.watertracker.model.AppUser;
import com.gograffer.watertracker.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/authenticate")
    public String authenticate(@RequestParam("email") String email, 
                              @RequestParam("password") String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        
        if (userService.authenticate(email, password)) {
            AppUser user = userService.findByEmail(email).get();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getName());
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute AppUser user,
                              @RequestParam("confirmPassword") String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        
        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addAttribute("error", "Email adresa već postoji");
            return "redirect:/register";
        }
        
        // Check if passwords match
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "Lozinke se ne podudaraju");
            return "redirect:/register";
        }
        
        // Save the user
        userService.saveUser(user);
        
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
