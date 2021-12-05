package com.banck.accountmovements.aplication.impl;

import com.banck.accountmovements.domain.Movement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.banck.accountmovements.aplication.MovementOperations;
import com.banck.accountmovements.aplication.model.DebitcardaccountRepository;
import com.banck.accountmovements.aplication.model.MovementRepository;
import com.banck.accountmovements.domain.AnyDto;
import com.banck.accountmovements.domain.DebitcardaccountDto;
import com.banck.accountmovements.utils.Concept;
import com.banck.accountmovements.utils.MovementType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jonavcar
 */
@Service
@RequiredArgsConstructor
public class MovementOperationsImpl implements MovementOperations {

    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("America/Bogota"));
    private final MovementRepository movementRepository;
    private final DebitcardaccountRepository debitcardaccountRepository;

    @Override
    public Flux<Movement> list() {
        return movementRepository.list();
    }

    @Override
    public Mono<Movement> get(String movement) {
        return movementRepository.get(movement);
    }

    @Override
    public Mono<Movement> create(Movement movement) {
        return movementRepository.create(movement);
    }

    @Override
    public Mono<Movement> update(String movement, Movement c) {
        return movementRepository.update(movement, c);
    }

    @Override
    public void delete(String movement) {
        movementRepository.delete(movement);
    }

    @Override
    public Flux<Movement> listByCustomer(String customer) {
        return movementRepository.listByCustomer(customer);
    }

    @Override
    public Flux<Movement> listByCustomerAndAccount(String customer, String account) {
        return movementRepository.listByCustomerAndAccount(customer, account);
    }

    @Override
    public Flux<Movement> listByCustomerAndAccountAndAccountType(String customer, String account, String accountType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Flux<Movement> listByAccount(String account) {

        return movementRepository.listByAccount(account);
    }

    @Override
    public Flux<Movement> listByAccount(List<String> customer) {
        return movementRepository.listByAccount(customer);
    }

    @Override
    public Mono<AnyDto> createMovementWithDebitCard(String debitCard, double quota) {
        return debitcardaccountRepository.listByDebitCard(debitCard).flatMap(mp -> {
            return movementRepository.listByAccount(mp.getAccount()).collect(Collectors.summingDouble(k -> k.getAmount())).map(amount -> {
                DebitcardaccountDto sm = new DebitcardaccountDto();
                sm.setDebitCard(mp.getDebitCard());
                sm.setAccount(mp.getAccount());
                sm.setMain(mp.isMain());
                sm.setAmount(amount);
                sm.setOrder(mp.getOrder());
                return sm;
            });
        }).filter(p -> p.getAmount() > 0)
                .collect(Collectors.toList()).map(lDA -> analyzeBalances(lDA, quota))
                .flatMap(ro -> {
                    AnyDto adt = new AnyDto();
                    if (ro.isStatus()) {

                        Movement rqMovement = new Movement();
                        rqMovement.setMovement(rqMovement.getCustomer() + "-" + getRandomNumberString());
                        rqMovement.setDate(dateTime.format(formatDate));
                        rqMovement.setTime(dateTime.format(formatTime));
                        rqMovement.setConcept(Concept.CHARGE.value);
                        rqMovement.setMovementType(MovementType.CHARGE.value);
                        rqMovement.setObservations("Cargo con targeta de Debito " + debitCard);
                        rqMovement.setCustomer("");
                        rqMovement.setTransferAccount("");
                        rqMovement.setTransferCustomer("");
                        rqMovement.setCorrect(true);
                        return Flux.fromStream(ro.cuentaSaldos.stream()).flatMap(q -> {
                            rqMovement.setAccount(q.getAccount());
                            rqMovement.setAmount(q.getSaldo() * -1);
                            return movementRepository.create(rqMovement).map(rt -> {
                                return rt.getAccount();
                            });
                        }).collect(Collectors.toList()).flatMap(q -> {
                            adt.setCode("1");
                            adt.setMessage("Se insertaron " + q.size() + " cuentas");
                            return Mono.just(adt);
                        });
                    } else {
                        adt.setMessage(ro.getMsg());
                        adt.setCode("0");
                        return Mono.just(adt);
                    }

                });
    }

    public static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public Mono<ResponseEntity> validateAccount(List<String> account) {
        return Mono.just(ResponseEntity.ok(account));
    }

    public LAccountSaldo analyzeBalances(List<DebitcardaccountDto> rr, Double monto) {
        double monto1 = monto;
        AtomicReference<Double> saldo = new AtomicReference(0);
        saldo.set(monto);
        AtomicInteger sta = new AtomicInteger(0);
        sta.set(0);
        List<CuentaSaldo> listAccountSaldo = new ArrayList();
        rr.stream().sorted((o1, o2) -> {
            return o1.getOrder() - o2.getOrder();
        }).forEachOrdered(action -> {
            CuentaSaldo mp = new CuentaSaldo();
            if (action.isMain() && action.getAmount() >= monto1) {
                mp.setAccount(action.getAccount());
                mp.setSaldo(monto1);
                listAccountSaldo.add(mp);
                saldo.set(0.00);
                sta.set(1);
            }
            if (saldo.get() > 0 && saldo.get() >= action.getAmount()) {
                mp.setAccount(action.getAccount());
                mp.setSaldo(action.getAmount());
                listAccountSaldo.add(mp);
                saldo.set(saldo.get() - action.getAmount());
            } else if (saldo.get() > 0 && saldo.get() < action.getAmount()) {
                mp.setAccount(action.getAccount());
                mp.setSaldo(saldo.get());
                listAccountSaldo.add(mp);
                saldo.set(0.00);
            } else {
            }
        });

        LAccountSaldo mpp = new LAccountSaldo();
        mpp.setCuentaSaldos(listAccountSaldo);
        if (saldo.get() > 0) {
            mpp.setStatus(false);
            mpp.setMsg("No hay saldo en las cuentas");
        } else {
            mpp.setStatus(true);
            mpp.setMsg("Si hay saldo en las cuentas");
        }
        return mpp;
    }

}

class CuentaSaldo {

    String account;
    double saldo;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}

class LAccountSaldo {

    List<CuentaSaldo> cuentaSaldos;
    String msg;
    boolean status;

    public List<CuentaSaldo> getCuentaSaldos() {
        return cuentaSaldos;
    }

    public void setCuentaSaldos(List<CuentaSaldo> cuentaSaldos) {
        this.cuentaSaldos = cuentaSaldos;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
