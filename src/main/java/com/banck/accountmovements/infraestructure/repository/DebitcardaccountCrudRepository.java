package com.banck.accountmovements.infraestructure.repository;

import com.banck.accountmovements.domain.Debitcardaccount;
import com.banck.accountmovements.infraestructure.model.dao.DebitcardaccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.banck.accountmovements.aplication.model.DebitcardaccountRepository;

/**
 *
 * @author jonavcar
 */
@Component
public class DebitcardaccountCrudRepository implements DebitcardaccountRepository {

    @Autowired
    IDebitcardaccountCrudRepository debitcardaccountRepository;

    @Override
    public Mono<Debitcardaccount> get(String debitcardaccount) {
        return debitcardaccountRepository.findById(debitcardaccount).map(this::DebitcardaccountDaoToDebitcardaccount);
    }

    @Override
    public Flux<Debitcardaccount> list() {
        return debitcardaccountRepository.findAll().map(this::DebitcardaccountDaoToDebitcardaccount);
    }

    @Override
    public Mono<Debitcardaccount> create(Debitcardaccount debitcardaccount) {
        return debitcardaccountRepository.save(DebitcardaccountToDebitcardaccountDao(debitcardaccount)).map(this::DebitcardaccountDaoToDebitcardaccount);
    }

    @Override
    public Mono<Debitcardaccount> update(String debitcardaccount, Debitcardaccount c) {
        c.setDebitcardaccount(debitcardaccount);
        return debitcardaccountRepository.save(DebitcardaccountToDebitcardaccountDao(c)).map(this::DebitcardaccountDaoToDebitcardaccount);
    }

    @Override
    public void delete(String debitcardaccount) {
        debitcardaccountRepository.deleteById(debitcardaccount).subscribe();
    }

    public Debitcardaccount DebitcardaccountDaoToDebitcardaccount(DebitcardaccountDao md) {
        Debitcardaccount m = new Debitcardaccount();
        m.setDebitcardaccount(md.getDebitcardaccount());
        m.setDebitCard(md.getDebitCard());
        m.setAccount(md.getAccount());
        m.setMain(md.isMain());
        m.setOrder(md.getOrder());
        m.setDate(md.getDate());
        m.setStatus(md.getStatus());
        return m;
    }

    public DebitcardaccountDao DebitcardaccountToDebitcardaccountDao(Debitcardaccount m) {
        DebitcardaccountDao md = new DebitcardaccountDao();
        md.setDebitcardaccount(m.getDebitcardaccount());
        md.setDebitCard(m.getDebitCard());
        md.setAccount(m.getAccount());
        md.setMain(m.isMain());
        md.setOrder(m.getOrder());
        md.setDate(m.getDate());
        md.setStatus(m.getStatus());
        return md;
    }

    @Override
    public Flux<Debitcardaccount> listByDebitCard(String debitCard) {
        return debitcardaccountRepository.findAllByDebitCard(debitCard).map(this::DebitcardaccountDaoToDebitcardaccount);
    }

    @Override
    public Mono<Debitcardaccount> getAccountMainByDebitCard(String debitCard) {
        return debitcardaccountRepository.findByDebitCardAndMain(debitCard, true).map(this::DebitcardaccountDaoToDebitcardaccount);
    }

}
