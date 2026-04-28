package com.hj.ajouToday;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // "/quote"로 들어오면 "quote.html"을 보여줘라!
        registry.addViewController("/quote").setViewName("forward:/quote.html");
        registry.addViewController("/game").setViewName("forward:/game.html");
        registry.addViewController("/admin").setViewName("forward:/admin.html");
    }
}