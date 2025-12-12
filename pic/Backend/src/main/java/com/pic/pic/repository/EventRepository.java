package com.pic.pic.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pic.pic.domain.Event;

@Profile("db")
public interface EventRepository extends JpaRepository<Event, Long> {}