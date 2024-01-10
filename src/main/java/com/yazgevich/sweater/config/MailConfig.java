package com.yazgevich.sweater.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    @Value("${spring.mail.host}")
    String host;

    @Value("${spring.mail.username}")
    String userName;

    @Value("${spring.mail.password}")
    String password;

    @Value("${spring.mail.port}")
    int port;

    @Value("${spring.mail.protocol}")
    String protocol;

    @Value("${mail.debug}")
    String debug;

    @Bean
    public JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setUsername(userName);
        mailSender.setPassword(password);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);

        Properties javaMailProperties = mailSender.getJavaMailProperties();
        javaMailProperties.setProperty("mai.debug", debug);
        return mailSender;
    }
}
