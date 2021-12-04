package com.banck.accountmovements.infraestructure.rest;

import com.banck.accountmovements.domain.Debitcardaccount;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import com.banck.accountmovements.aplication.DebitcardaccountOperations;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jonavcar
 */
@RestController
@RequestMapping("/credit-movement/debitcard-account")
@RequiredArgsConstructor
public class DebitcardaccountController {

    Logger logger = LoggerFactory.getLogger(DebitcardaccountController.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("America/Bogota"));
    private final DebitcardaccountOperations operations;

    @GetMapping
    public Flux<Debitcardaccount> listAll() {
        return operations.list();
    }

    @GetMapping("/{id}")
    public Mono<Debitcardaccount> get(@PathVariable("id") String id) {
        return operations.get(id);
    }

    @GetMapping("/customer/{id}/list")
    public Flux<Debitcardaccount> listByCustomer(@PathVariable("id") String id) {
        return operations.listByDebitCard(id);
    }

    @GetMapping("/account/{id}/list")
    public Flux<Debitcardaccount> listByAccount(@PathVariable("id") String id) {
        return operations.listByDebitCard(id);
    }

    @GetMapping("/customer-credit/{customer}/{credit}/list")
    public Flux<Debitcardaccount> listByCustomerAndCredit(@PathVariable("customer") String customer, @PathVariable("credit") String credit) {
        return operations.listByDebitCard(customer);
    }

    @PostMapping
    public Mono<ResponseEntity> create(@RequestBody Debitcardaccount debitCardAccountReq) {
        debitCardAccountReq.setDebitcardaccount("TDA-" + getRandomNumberString());
        debitCardAccountReq.setDate(dateTime.format(formatDate));
        return Mono.just(debitCardAccountReq).flatMap(debitCardAccount -> {

            if (Optional.ofNullable(debitCardAccount.getDebitCard()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar Targeta de Debito, Ejemplo { \"debitCard\": \"TC-000000\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.getAccount()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar la Cuenta, Ejemplo { \"account\": \"CB-000000\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.getOrder()).isEmpty() || debitCardAccount.getOrder()<=0) {
                return Mono.just(new ResponseEntity("Debe un orden, Ejemplo { \"order\": 2 }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.isMain()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar si es Principal, Ejemplo { \"main\": true }", HttpStatus.BAD_REQUEST));
            }

            debitCardAccount.setStatus("1");

            return operations.create(debitCardAccount).flatMap(scheduleRes -> {
                return Mono.just(new ResponseEntity(scheduleRes, HttpStatus.OK));
            });
        });
    }
    
    @PostMapping("/pagodebito")
    public Mono<ResponseEntity> pagodebito(@RequestBody Debitcardaccount debitCardAccountReq) {
        debitCardAccountReq.setDebitcardaccount("TDA-" + getRandomNumberString());
        debitCardAccountReq.setDate(dateTime.format(formatDate));
        return Mono.just(debitCardAccountReq).flatMap(debitCardAccount -> {

            if (Optional.ofNullable(debitCardAccount.getDebitCard()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar Targeta de Debito, Ejemplo { \"debitCard\": \"TC-000000\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.getAccount()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar la Cuenta, Ejemplo { \"account\": \"CB-000000\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.getOrder()).isEmpty() || debitCardAccount.getOrder()<=0) {
                return Mono.just(new ResponseEntity("Debe un orden, Ejemplo { \"order\": 2 }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(debitCardAccount.isMain()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar si es Principal, Ejemplo { \"main\": true }", HttpStatus.BAD_REQUEST));
            }

            debitCardAccount.setStatus("1");

            return operations.create(debitCardAccount).flatMap(scheduleRes -> {
                return Mono.just(new ResponseEntity(scheduleRes, HttpStatus.OK));
            });
        });
    }

    @PutMapping("/{id}")
    public Mono<Debitcardaccount> update(@PathVariable("id") String id, @RequestBody Debitcardaccount movement) {
        return operations.update(id, movement);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        operations.delete(id);
    }

    public static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
}
