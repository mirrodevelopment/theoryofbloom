package com.theoryofbloom.config;

import com.theoryofbloom.model.Product;
import com.theoryofbloom.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class ProductDataInitializer {

    @Bean
    @SuppressWarnings("null")
    public CommandLineRunner initProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("Daily Elixir");
                p1.setShortDescription("Your daily ritual for metabolic spark and vitality.");
                p1.setPrice(new BigDecimal("1299"));
                p1.setCategory("Morning");
                p1.setImageUrl("/images/daily-elixir.jpg");
                p1.setFeatured(true);
                p1.setStockQuantity(100);

                Product p2 = new Product();
                p2.setName("Iron & Glow");
                p2.setShortDescription("Botanical infusion for natural radiance and inner strength.");
                p2.setPrice(new BigDecimal("1499"));
                p2.setCategory("Skin & Glow");
                p2.setImageUrl("/images/iron-glow.jpg");
                p2.setFeatured(true);
                p2.setStockQuantity(100);

                Product p3 = new Product();
                p3.setName("Lunar Calm");
                p3.setShortDescription("A soothing blend for deep rest and nightly restoration.");
                p3.setPrice(new BigDecimal("1199"));
                p3.setCategory("Sleep & Calm");
                p3.setImageUrl("/images/lunar-calm.jpg");
                p3.setFeatured(true);
                p3.setStockQuantity(100);

                Product p4 = new Product();
                p4.setName("Mind Garden");
                p4.setShortDescription("Sharp focus and mental clarity for your most creative hours.");
                p4.setPrice(new BigDecimal("1399"));
                p4.setCategory("Focus & Clarity");
                p4.setImageUrl("/images/mind-garden.jpg");
                p4.setFeatured(true);
                p4.setStockQuantity(100);

                Product p5 = new Product();
                p5.setName("Flora Shield");
                p5.setShortDescription("Defend your bloom with nature's most potent immunity botanicals.");
                p5.setPrice(new BigDecimal("1599"));
                p5.setCategory("Immunity");
                p5.setImageUrl("/images/flora-shield.jpg");
                p5.setFeatured(true);
                p5.setStockQuantity(100);

                Product p6 = new Product();
                p6.setName("Masculine Vitality");
                p6.setShortDescription("Strength and endurance crafted for the modern man.");
                p6.setPrice(new BigDecimal("1699"));
                p6.setCategory("Masculine");
                p6.setImageUrl("/images/masculine-vitality.jpg");
                p6.setFeatured(false);
                p6.setStockQuantity(100);

                Product p7 = new Product();
                p7.setName("Skin Nectar");
                p7.setShortDescription("Hydrate and glow from within with floral essences.");
                p7.setPrice(new BigDecimal("1450"));
                p7.setCategory("Skin & Glow");
                p7.setImageUrl("/images/skin-nectar.jpg");
                p7.setFeatured(false);
                p7.setStockQuantity(100);

                Product p8 = new Product();
                p8.setName("Metabolic Spark");
                p8.setShortDescription("Ignite your day with a blend that supports energy levels.");
                p8.setPrice(new BigDecimal("1250"));
                p8.setCategory("Morning");
                p8.setImageUrl("/images/metabolic-spark.jpg");
                p8.setFeatured(false);
                p8.setStockQuantity(100);

                productRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));
            }
        };
    }
}
