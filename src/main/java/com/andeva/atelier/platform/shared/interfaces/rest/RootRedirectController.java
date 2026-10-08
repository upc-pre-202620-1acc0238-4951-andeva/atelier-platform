package com.andeva.atelier.platform.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller redirecting incoming root HTTP traffic directly to the OpenAPI / Swagger UI interface.
 * Prevents 404 Not Found responses on root URL access.
 *
 * @author Joel Huamani Estefanero
 */
@Hidden
@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}
