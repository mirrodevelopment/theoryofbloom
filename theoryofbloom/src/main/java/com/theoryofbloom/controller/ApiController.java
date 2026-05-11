package com.theoryofbloom.controller;

import com.theoryofbloom.model.Product;
import com.theoryofbloom.model.SiteContent;
import com.theoryofbloom.service.ProductService; // IDE trigger
import com.theoryofbloom.service.SiteContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SiteContentService siteContentService;

    @GetMapping("/filters")
    public ResponseEntity<List<String>> getFilters() {
        SiteContent sc = siteContentService.load();
        String cats = sc.getShop().getFilterCategories();
        List<String> list = new java.util.ArrayList<>();
        if (cats != null && !cats.isBlank()) {
            for (String s : cats.split(",")) {
                list.add(s.trim());
            }
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(required = false) String filter) {
        if (filter == null || filter.isEmpty() || filter.equalsIgnoreCase("All Blends")) {
            return ResponseEntity.ok(productService.getAllProducts());
        }
        return ResponseEntity.ok(productService.getProductsByCategory(filter));
    }
}
