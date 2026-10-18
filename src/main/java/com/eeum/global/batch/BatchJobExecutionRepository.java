package com.eeum.global.batch;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobExecutionRepository extends JpaRepository<BatchJobExecution, Long> {

}
