package com.yazgevich.sweater.service;

import com.yazgevich.sweater.model.Role;
import com.yazgevich.sweater.model.User;
import com.yazgevich.sweater.repository.UserRepository;
import com.yazgevich.sweater.util.ControllerUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, MailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }


    public void addUser(User user) {
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sendActivationCode(user);

        userRepository.save(user);
    }

    public Map<String, String> getErrors(BindingResult bindingResult, User user, String password2) {
        Map<String, String> errors = new HashMap<>();
        if (password2 == null) {
            errors.put("password2Error", "Password confirmation cannot be empty");
        } else if (user.getPassword() != null && !user.getPassword().equals(password2)) {
            errors.put("passwordError", "Passwords are different");
        }
        if (bindingResult.hasErrors()) {
            errors.putAll(ControllerUtils.getErrors(bindingResult));
        } else if (exists(user)) {
            errors.put("usernameError", "User already exists");
        }
        return errors;
    }

    private boolean exists(User user) {
        User userFromDb = userRepository.findByUsername(user.getUsername());
        return userFromDb != null;
    }

    private void sendActivationCode(User user) {
        user.setActivationCode(UUID.randomUUID().toString());

        if (!ObjectUtils.isEmpty(user.getEmail())) {
            String message = String.format("""
                            Hello, %s!
                            Please, visit next link:
                             <a href="http://localhost:8080/activate/%s">Confirm your account</a>\s
                             """,
                    user.getUsername(),
                    user.getActivationCode());
            try {
                mailSender.send(user.getEmail(), "Activation code", message);
            } catch (MessagingException e) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    public boolean activeUser(String code) {
        User user = userRepository.findByActivationCode(code);

        if (user == null) return false;

        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save(String username, Map<String, String> form, User user) {
        user.setUsername(username);
        user.getRoles().clear();
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }

        userRepository.save(user);
    }

    public void updateProfile(User user, String password, String email) {
        if (password != null && !password.equals(user.getPassword())) {
            user.setPassword(password);
        }

        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            sendActivationCode(user);
        }

        userRepository.save(user);
    }
}
