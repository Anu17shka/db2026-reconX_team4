package com.dbtraining.reconx.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA repository/auditing/entity-scan wiring, kept off {@code ReconxApplication}
 * itself so that {@code @WebMvcTest} slices (which use the application class as
 * their {@code @SpringBootConfiguration}) don't try to activate JPA auditing
 * without an EntityManagerFactory in the context.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.dbtraining.reconx.repository")
@EntityScan(basePackages = "com.dbtraining.reconx.repository.entity")
public class JpaConfig {
}
