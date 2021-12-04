package com.banck.accountmovements.aplication;

import com.banck.accountmovements.domain.Debitcardaccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author jonavcar
 */
public interface DebitcardaccountOperations {

    public Flux<Debitcardaccount> list();

    public Flux<Debitcardaccount> listByDebitCard(String debitCard);

    public Mono<Debitcardaccount> get(String movement);

    public Mono<Debitcardaccount> create(Debitcardaccount movement);

    public Mono<Debitcardaccount> update(String id, Debitcardaccount movement);

    public void delete(String id);

}
