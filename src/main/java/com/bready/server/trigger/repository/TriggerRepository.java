package com.bready.server.trigger.repository;

import com.bready.server.trigger.domain.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TriggerRepository extends JpaRepository<Trigger, Long> {
}
