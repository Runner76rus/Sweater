package com.yazgevich.sweater.controller;

import com.yazgevich.sweater.dto.CaptchaResponseDto;
import com.yazgevich.sweater.model.User;
import com.yazgevich.sweater.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {

    public static final String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
    public static final double SCORE_LIMIT = 0.5f;

    UserService userService;
    RestTemplate restTemplate;

    @Value("${recaptcha.secret}")
    String secret;

    @Autowired
    public RegistrationController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@Valid User user,
                          BindingResult bindingResult,
                          Model model,
                          @RequestParam String password2,
                          @RequestParam("g-recaptcha-response") String grecaptcha) {
//        check errors
        Map<String, String> errors = userService.getErrors(bindingResult, user, password2);
        if (!errors.isEmpty()) {
            model.mergeAttributes(errors);
        } else {
//            check reCaptcha
            String url = CAPTCHA_URL.formatted(secret, grecaptcha);
            CaptchaResponseDto captchaResponseDto = restTemplate.postForObject(url, Collections.EMPTY_LIST, CaptchaResponseDto.class);
            if (captchaResponseDto != null) {
                if (captchaResponseDto.getErrors() != null
                        && !captchaResponseDto.getErrors().isEmpty()) {
                    model.addAttribute("captchaError", """
                            reCAPTCHA error
                            error code : %s
                            """.formatted(captchaResponseDto.getErrors().toArray()[0]));
                } else if (captchaResponseDto.getScore() <= SCORE_LIMIT) {
                    model.addAttribute("captchaError", """
                            <p>reCAPTCHA thinks you`re a bot :(</p>
                            <p>If you use IP hiding tools, try disabling them</p>
                            """);
                } else {
//                    if all right
                    userService.addUser(user);
                    return "redirect:/login";
                }
            }
        }
        model.addAttribute("user", user);
        return "registration";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activeUser(code);
        if (isActivated) {
            model.addAttribute("activationSuccess", "User successfully activated");
        } else {
            model.addAttribute("activationError", "Activation code not found");
        }
        return "login";
    }
}
