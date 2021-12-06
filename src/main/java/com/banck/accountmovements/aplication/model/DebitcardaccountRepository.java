package com.banck.accountmovements.aplication.model;

import com.banck.accountmovements.domain.Debitcardaccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author jonavcar
 */
public interface DebitcardaccountRepository {

    public Flux<Debitcardaccount> list();

    public Flux<Debitcardaccount> listByDebitCard(String debitCard);

    public Mono<Debitcardaccount> get(String id);

    public Mono<Debitcardaccount> getAccountMainByDebitCard(String debitCard);

    public Mono<Debitcardaccount> create(Debitcardaccount d);

    public Mono<Debitcardaccount> update(String id, Debitcardaccount d);

    public void delete(String id);
}
