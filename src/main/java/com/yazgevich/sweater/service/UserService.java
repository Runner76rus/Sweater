package com.yazgevich.sweater.service;

import com.yazgevich.sweater.model.Role;
import com.yazgevich.sweater.model.User;
import com.yazgevich.sweater.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MailSender mailSender;

    @Autowired
    public UserService(UserRepository userRepository, MailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public boolean addUser(User user) {
        User userFromDb = userRepository.findByUsername(user.getUsername());
        if (userFromDb != null) {
            return false;
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());

        if (!ObjectUtils.isEmpty(user.getEmail())) {
            String message = String.format("Hello, %s!\n" +
                            "Please, visit next link:\n" +
                            " http://localhost:8080/activate/%s ",
                    user.getUsername(), user.getActivationCode());
            mailSender.send(user.getEmail(), "Activation code", message);
        }

        userRepository.save(user);
        return true;
    }

    public boolean activeUser(String code) {
        User user = userRepository.findByActivationCode(code);

        if (user == null) return false;

        user.setActivationCode(null);
        userRepository.save(user);
        return true;
    }
}
