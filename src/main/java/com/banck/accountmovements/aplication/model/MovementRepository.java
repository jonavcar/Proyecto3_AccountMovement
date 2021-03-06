package com.banck.accountmovements.aplication.model;

import com.banck.accountmovements.domain.Movement;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author jonavcar
 */
public interface MovementRepository {

    public Flux<Movement> list();

    public Flux<Movement> listByCustomer(String customer);
    
    public Flux<Movement> listByAccount(String account);
    
    public Flux<Movement> listByAccount(List<String> account);

    public Flux<Movement> listByCustomerAndAccount(String customer, String account);

    public Flux<Movement> listByCustomerAndAccountAndAccountType(String customer, String account, String accountType);

    public Mono<Movement> get(String movement);

    public Mono<Movement> create(Movement movement);

    public Mono<Movement> update(String id, Movement movement);

    public void delete(String id);
}
