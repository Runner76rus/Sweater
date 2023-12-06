package com.yazgevich.sweater.repos;

import com.yazgevich.sweater.data.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface MessageRepository extends CrudRepository<Message, Long> {


    List<Message> findByTag(String tag);
}
