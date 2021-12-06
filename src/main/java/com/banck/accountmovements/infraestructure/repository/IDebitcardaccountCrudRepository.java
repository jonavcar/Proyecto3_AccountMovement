package com.banck.accountmovements.infraestructure.repository;

import com.banck.accountmovements.infraestructure.model.dao.DebitcardaccountDao;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author jonavcar
 */
public interface IDebitcardaccountCrudRepository extends ReactiveCrudRepository<DebitcardaccountDao, String> {

    Flux<DebitcardaccountDao> findAllByDebitCard(String debitCard);

    Flux<DebitcardaccountDao> findAllByDebitCardAndAccount(String debitCard, String account);

    Mono<DebitcardaccountDao> findByDebitCardAndMain(String debitCard, boolean main);
}
