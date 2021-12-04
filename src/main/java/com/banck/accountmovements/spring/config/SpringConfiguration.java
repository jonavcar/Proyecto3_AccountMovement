package com.banck.accountmovements.spring.config;

import com.banck.accountmovements.aplication.model.DebitcardaccountRepository;
import com.banck.accountmovements.infraestructure.repository.MovementCrudRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.banck.accountmovements.aplication.model.MovementRepository;
import com.banck.accountmovements.infraestructure.repository.DebitcardaccountCrudRepository;

/**
 *
 * @author jonavcar
 */
@Configuration
public class SpringConfiguration {
    
    @Bean
    public MovementRepository movementRepository() {
        return new MovementCrudRepository();
    }
    
    @Bean
    public DebitcardaccountRepository debitcardaccountRepository() {
        return new DebitcardaccountCrudRepository();
    }
}
