package com.banck.accountmovements.aplication.impl;

import com.banck.accountmovements.aplication.DebitcardaccountOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.banck.accountmovements.aplication.model.DebitcardaccountRepository;
import com.banck.accountmovements.domain.Debitcardaccount;

/**
 *
 * @author jonavcar
 */
@Service
@RequiredArgsConstructor
public class DebitcardaccountOperationsImpl implements DebitcardaccountOperations {

    private final DebitcardaccountRepository debitcardaccountRepository;

    @Override
    public Flux<Debitcardaccount> list() {
        return debitcardaccountRepository.list();
    }

    @Override
    public Mono<Debitcardaccount> get(String debitcardaccount) {
        return debitcardaccountRepository.get(debitcardaccount);
    }

    @Override
    public Mono<Debitcardaccount> create(Debitcardaccount debitcardaccount) {
        return debitcardaccountRepository.create(debitcardaccount);
    }

    @Override
    public Mono<Debitcardaccount> update(String debitcardaccount, Debitcardaccount c) {
        return debitcardaccountRepository.update(debitcardaccount, c);
    }

    @Override
    public void delete(String debitcardaccount) {
        debitcardaccountRepository.delete(debitcardaccount);
    }
 
 
    @Override
    public Flux<Debitcardaccount> listByDebitCard(String debitCard) {
        return debitcardaccountRepository.listByDebitCard(debitCard);
    }

}
