package com.theoryofbloom.controller;

import com.theoryofbloom.model.BlogPost;
import com.theoryofbloom.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogRepository blogRepository;

    @GetMapping
    public String blogList(Model model) {
        model.addAttribute("posts", blogRepository.findByOrderByCreatedAtDesc());
        return "blog";
    }

    @SuppressWarnings("null")
    @GetMapping("/{id}")
    public String blogDetail(@PathVariable Long id, Model model) {
        BlogPost post = blogRepository.findById(id).orElse(null);
        if (post == null) return "redirect:/blog";
        model.addAttribute("post", post);
        return "blog-detail";
    }
}
