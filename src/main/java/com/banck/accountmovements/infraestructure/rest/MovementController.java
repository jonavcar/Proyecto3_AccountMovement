package com.banck.accountmovements.infraestructure.rest;

import com.banck.accountmovements.aplication.AccountOperations;
import com.banck.accountmovements.aplication.DebitcardaccountOperations;
import com.banck.accountmovements.domain.Movement;
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
import com.banck.accountmovements.aplication.MovementOperations;
import com.banck.accountmovements.domain.AnyDto;
import com.banck.accountmovements.utils.Concept;
import com.banck.accountmovements.utils.MovementType;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jonavcar
 */
@RestController
@RequestMapping("/mov-account")
@RequiredArgsConstructor
public class MovementController {

    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("America/Bogota"));
    private final MovementOperations operations;
    private final DebitcardaccountOperations debitcardaccountOperations;
    private final AccountOperations accountOperations;

    @GetMapping
    public Flux<Movement> listAll() {
        return operations.list();
    }

    @GetMapping("/{id}")
    public Mono<Movement> get(@PathVariable("id") String id) {
        return operations.get(id);
    }

    @GetMapping("/customer/{id}/list")
    public Flux<Movement> listByCustomer(@PathVariable("id") String id) {
        return operations.listByCustomer(id);
    }

    @GetMapping("/account/{id}/list")
    public Flux<Movement> listByAccount(@PathVariable("id") String id) {
        return operations.listByAccount(id);
    }

    @GetMapping("/customer-account/{customer}/{account}/list")
    public Flux<Movement> listByCustomerAndAccount(@PathVariable("customer") String customer, @PathVariable("account") String account) {
        return operations.listByCustomerAndAccount(customer, account);
    }

    @PostMapping
    public Mono<ResponseEntity> create(@RequestBody Movement rqMovement) {
        rqMovement.setMovement(rqMovement.getCustomer() + "-" + getRandomNumberString());
        rqMovement.setDate(dateTime.format(formatDate));
        rqMovement.setTime(dateTime.format(formatTime));
        rqMovement.setCorrect(true);
        return Mono.just(rqMovement).flatMap(movement -> {

            if (Optional.ofNullable(movement.getAccount()).isEmpty()) {
                return Mono.just(ResponseEntity.ok("Debe ingresar la cuenta, Ejemplo: { \"account\": \"78345212-653\" }"));
            }

            return validateAccount(movement.getAccount()).flatMap(isValidAccount -> {
                if (isValidAccount) {
                    String msgConceptos = ""
                            + "Deposito = {\"concept\": \"DP\"}\n"
                            + "Retiro = {\"concept\": \"RT\"}";

                    if (Optional.ofNullable(movement.getConcept()).isEmpty()) {
                        return Mono.just(ResponseEntity.ok("Debe ingresar Concepto, Ejemplo:\n" + msgConceptos));
                    }

                    boolean isConcept = false;
                    for (Concept tc : Concept.values()) {
                        if (movement.getConcept().equals(tc.value)) {
                            isConcept = true;
                        }
                    }

                    if (!isConcept) {
                        return Mono.just(ResponseEntity.ok("Los codigos de Concepto son: \n" + msgConceptos));
                    }

                    if (Optional.ofNullable(movement.getCustomer()).isEmpty()) {
                        return Mono.just(ResponseEntity.ok("Debe ingresar su Identificacion, Ejemplo: { \"customer\": \"78345212\" }"));
                    }

                    if (Optional.ofNullable(movement.getAmount()).isEmpty() || movement.getAmount() == 0) {
                        return Mono.just(ResponseEntity.ok("Debe ingresar el monto diferente de cero, Ejemplo: { \"amount\": \"300.50\" }"));
                    }

                    if (Concept.CHARGE.equals(movement.getConcept())) {
                        if (movement.getAmount() > 0) {
                            movement.setAmount(-1 * movement.getAmount());
                        }

                        movement.setMovementType(MovementType.CHARGE.value);
                        movement.setObservations("Retiro por la suma de " + movement.getAmount());
                    }

                    if (Concept.PAYMENT.equals(movement.getConcept())) {
                        if (movement.getAmount() < 0) {
                            movement.setAmount(-1 * movement.getAmount());
                        }
                        movement.setMovementType(MovementType.PAYMENT.value);
                        movement.setObservations("Deposito por la suma de " + movement.getAmount());
                    }

                    return operations.listByAccount(movement.getAccount()).collect(Collectors.summingDouble(ui -> ui.getAmount())).flatMap(balance -> {
                        if ((balance + movement.getAmount()) < 0) {
                            return Mono.just(ResponseEntity.ok("El movimiento a efectuar sobrepasa el saldo disponible."));
                        } else {
                            movement.setTransferAccount("");
                            movement.setTransferCustomer("");
                            return operations.create(movement).flatMap(mCG -> {
                                return Mono.just(ResponseEntity.ok(mCG));
                            });
                        }
                    });
                } else {
                    return Mono.just(ResponseEntity.ok("¡¡La cuenta " + movement.getAccount() + ", No existe!!"));
                }

            }).onErrorReturn(ResponseEntity.ok("¡¡Ocurrio un  Error en el Servicio de Cuentas o no esta disponible, porfavor verifique!!"));
        });
    }

    @PostMapping("/transfer/other-account")
    public Mono<ResponseEntity> transferOtherAccounts(@RequestBody Movement rqMovement) {
        return Mono.just(rqMovement).flatMap(movement -> {

            if (Optional.ofNullable(movement.getAccount()).isEmpty()) {
                return Mono.just(ResponseEntity.ok("Debe ingresar la cuenta de Origen, Ejemplo: { \"account\": \"78345212-653\" }"));
            }

            if (Optional.ofNullable(movement.getTransferAccount()).isEmpty()) {
                return Mono.just(ResponseEntity.ok("Debe ingresar la cuenta de Destino, Ejemplo: { \"transferAccount\": \"78345212-653\" }"));
            }

            return validateAccount(movement.getAccount()).flatMap(isValidAccount -> {
                if (isValidAccount) {
                    return validateAccount(movement.getTransferAccount()).flatMap(isValidAccountTransfer -> {
                        if (isValidAccountTransfer) {
                            if (Optional.ofNullable(movement.getCustomer()).isEmpty()) {
                                return Mono.just(ResponseEntity.ok("Debe ingresar su Identificacion, Ejemplo: { \"customer\": \"78345212\" }"));
                            }

                            if (Optional.ofNullable(movement.getTransferCustomer()).isEmpty()) {
                                return Mono.just(ResponseEntity.ok("Debe ingresar Identificacion Beneficiario, Ejemplo: { \"transferCustomer\": \"78345212\" }"));
                            }

                            if (Optional.ofNullable(movement.getAmount()).isEmpty() || movement.getAmount() == 0) {
                                return Mono.just(ResponseEntity.ok("Debe ingresar el monto diferente de cero, Ejemplo: { \"amount\": \"300.50\" }"));
                            }

                            movement.setConcept(Concept.TRANSFER.value);

                            if (Concept.TRANSFER.equals(movement.getConcept())) {
                                if (movement.getAmount() > 0) {
                                    movement.setAmount(-1 * movement.getAmount());
                                }

                                movement.setMovementType(MovementType.CHARGE.value);
                                movement.setObservations("Transferencia a la cuenta " + movement.getTransferAccount() + " por la suma de " + movement.getAmount() * -1);
                            }

                            return operations.listByAccount(movement.getAccount()).collect(Collectors.summingDouble(ui -> ui.getAmount())).flatMap(balance -> {
                                if ((balance + movement.getAmount()) < 0) {
                                    return Mono.just(ResponseEntity.ok("El movimiento a efectuar sobrepasa el saldo disponible."));
                                } else {

                                    rqMovement.setMovement(rqMovement.getCustomer() + "-" + getRandomNumberString());
                                    rqMovement.setDate(dateTime.format(formatDate));
                                    rqMovement.setTime(dateTime.format(formatTime));
                                    rqMovement.setCorrect(true);

                                    return operations.create(movement).flatMap(mCG -> {
                                        if (Concept.TRANSFER.equals(movement.getConcept())) {
                                            if (movement.getAmount() < 0) {
                                                movement.setAmount(-1 * movement.getAmount());
                                            }

                                            movement.setMovementType(MovementType.PAYMENT.value);
                                            movement.setObservations("Transferencia desde la cuenta " + mCG.getAccount() + " por la suma de " + movement.getAmount());
                                        }
                                        movement.setAccount(movement.getTransferAccount());
                                        movement.setCustomer(movement.getTransferCustomer());
                                        movement.setTransferAccount(mCG.getAccount());
                                        movement.setTransferCustomer(mCG.getCustomer());

                                        rqMovement.setMovement(movement.getCustomer() + "-" + getRandomNumberString());
                                        rqMovement.setDate(dateTime.format(formatDate));
                                        rqMovement.setTime(dateTime.format(formatTime));
                                        rqMovement.setCorrect(true);

                                        return operations.create(movement).flatMap(mAB -> {
                                            return Mono.just(ResponseEntity.ok(mCG));
                                        });
                                    });
                                }
                            });
                        } else {
                            return Mono.just(ResponseEntity.ok("¡¡La cuenta destino " + movement.getTransferAccount() + ", No existe!!"));
                        }

                    }).onErrorReturn(ResponseEntity.ok("¡¡Ocurrio un  Error en el Servicio de Cuentas o no esta disponible, porfavor verifique!!"));
                } else {
                    return Mono.just(ResponseEntity.ok("¡¡La cuenta origen " + movement.getAccount() + ", No existe!!"));
                }

            }).onErrorReturn(ResponseEntity.ok("¡¡Ocurrio un  Error en el Servicio de Cuentas o no esta disponible, porfavor verifique!!"));
        });
    }

    @PostMapping("/debit-card-payment")
    public Mono<ResponseEntity> debitCardPayment(@RequestBody AccuntCardDebit accuntCardDebit) {
        AnyDto api = new AnyDto();

        if (Optional.ofNullable(accuntCardDebit.getDebitCard()).isEmpty()) {
            return Mono.just(new ResponseEntity("Debe ingresar la targeta de debito, Ejemplo: { \"debitCard\": \"TD-78345212-653\" }", HttpStatus.BAD_REQUEST));
        }

        if (Optional.ofNullable(accuntCardDebit.getAmount()).isEmpty() || accuntCardDebit.getAmount() == 0) {
            return Mono.just(new ResponseEntity("Debe ingresar el monto diferente de cero, Ejemplo: { \"amount\": \"300.50\" }", HttpStatus.BAD_REQUEST));
        }

        return operations.createMovementWithDebitCard(accuntCardDebit.getDebitCard(), accuntCardDebit.getAmount()).flatMap(rr -> {
            if (rr.getCode().equals("1")) {
                return Mono.just(new ResponseEntity(rr, HttpStatus.OK));
            } else {
                return Mono.just(new ResponseEntity(rr, HttpStatus.OK));
            }

        });
    }

    @PostMapping("/transfer/my-account")
    public Mono<ResponseEntity> transferMyAccounts(@RequestBody Movement rqMovement) {
        return Mono.just(rqMovement).flatMap(movement -> {
            if (Optional.ofNullable(movement.getAccount()).isEmpty()) {
                return Mono.just(ResponseEntity.ok("Debe ingresar la cuenta de Origen, Ejemplo: { \"account\": \"78345212-653\" }"));
            }

            if (Optional.ofNullable(movement.getTransferAccount()).isEmpty()) {
                return Mono.just(ResponseEntity.ok("Debe ingresar la cuenta de Destino, Ejemplo: { \"transferAccount\": \"78345212-653\" }"));
            }
            return validateAccount(movement.getAccount()).flatMap(isValidAccount -> {
                if (isValidAccount) {

                    return validateAccount(movement.getTransferAccount()).flatMap(isValidAccountTransfer -> {
                        if (isValidAccountTransfer) {

                            if (Optional.ofNullable(movement.getCustomer()).isEmpty()) {
                                return Mono.just(ResponseEntity.ok("Debe ingresar su Identificacion, Ejemplo: { \"customer\": \"78345212\" }"));
                            }

                            if (Optional.ofNullable(movement.getAmount()).isEmpty() || movement.getAmount() == 0) {
                                return Mono.just(ResponseEntity.ok("Debe ingresar el monto diferente de cero, Ejemplo: { \"amount\": \"300.50\" }"));
                            }

                            movement.setConcept(Concept.TRANSFER.value);

                            if (Concept.TRANSFER.equals(movement.getConcept())) {
                                if (movement.getAmount() > 0) {
                                    movement.setAmount(-1 * movement.getAmount());
                                }

                                movement.setMovementType(MovementType.CHARGE.value);
                                movement.setObservations("Transferencia a la cuenta " + movement.getTransferAccount() + " por la suma de " + movement.getAmount() * -1);
                            }

                            return operations.listByAccount(movement.getAccount()).collect(Collectors.summingDouble(ui -> ui.getAmount())).flatMap(balance -> {
                                if ((balance + movement.getAmount()) < 0) {
                                    return Mono.just(ResponseEntity.ok("El movimiento a efectuar sobrepasa el saldo disponible."));
                                } else {
                                    rqMovement.setTransferCustomer(rqMovement.getCustomer());
                                    rqMovement.setMovement(rqMovement.getCustomer() + "-" + getRandomNumberString());
                                    rqMovement.setDate(dateTime.format(formatDate));
                                    rqMovement.setTime(dateTime.format(formatTime));
                                    rqMovement.setCorrect(true);

                                    return operations.create(movement).flatMap(mCG -> {
                                        if (Concept.TRANSFER.equals(movement.getConcept())) {
                                            if (movement.getAmount() < 0) {
                                                movement.setAmount(-1 * movement.getAmount());
                                            }

                                            movement.setMovementType(MovementType.PAYMENT.value);
                                            movement.setObservations("Transferencia desde la cuenta " + mCG.getAccount() + " por la suma de " + movement.getAmount());
                                        }
                                        rqMovement.setAccount(mCG.getTransferAccount());
                                        rqMovement.setTransferAccount(mCG.getAccount());
                                        rqMovement.setMovement(movement.getCustomer() + "-" + getRandomNumberString());
                                        rqMovement.setDate(dateTime.format(formatDate));
                                        rqMovement.setTime(dateTime.format(formatTime));
                                        rqMovement.setCorrect(true);

                                        return operations.create(movement).flatMap(mAB -> {
                                            return Mono.just(ResponseEntity.ok(mCG));
                                        });
                                    });
                                }
                            });

                        } else {
                            return Mono.just(ResponseEntity.ok("¡¡La cuenta destino " + movement.getTransferAccount() + ", No existe!!"));
                        }

                    }).onErrorReturn(ResponseEntity.ok("¡¡Ocurrio un  Error en el Servicio de Cuentas o no esta disponible, porfavor verifique!!"));
                } else {
                    return Mono.just(ResponseEntity.ok("¡¡La cuenta origen " + movement.getAccount() + ", No existe!!"));
                }

            }).onErrorReturn(ResponseEntity.ok("¡¡Ocurrio un  Error en el Servicio de Cuentas o no esta disponible, porfavor verifique!!"));
        });
    }

    @PutMapping("/{id}")
    public Mono<Movement> update(@PathVariable("id") String id, @RequestBody Movement movement) {
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

    public Mono<Boolean> validateAccount(String account) {
        return accountOperations.getAccount(account).flatMap(accountR -> {
            return Mono.just(true);
        }).switchIfEmpty(Mono.just(false));
    }

}

class AccuntCardDebit {

    String debitCard;
    double amount;

    public String getDebitCard() {
        return debitCard;
    }

    public void setDebitCard(String debitCard) {
        this.debitCard = debitCard;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
