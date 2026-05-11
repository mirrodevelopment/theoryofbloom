package com.theoryofbloom.controller;

import com.theoryofbloom.model.ContactMessage;
import com.theoryofbloom.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
            
        ContactMessage cm = new ContactMessage();
        cm.setName(name);
        cm.setEmail(email);
        cm.setMessage(message);
        contactMessageRepository.save(cm);
        
        redirectAttributes.addFlashAttribute("success", "Your message has been received. Our botanical consultants will reach out shortly.");
        return "redirect:/contact";
    }

}
