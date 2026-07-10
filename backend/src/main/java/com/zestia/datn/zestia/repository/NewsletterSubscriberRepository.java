package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Integer> {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);
}
