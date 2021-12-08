package com.banck.creditmovements.infraestructure.rest;

import com.banck.creditmovements.aplication.DebitAccountOperations;
import com.banck.creditmovements.dto.CardMovementDto;
import com.banck.creditmovements.dto.DateIDateF;
import com.banck.creditmovements.domain.Movement;
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

import com.banck.creditmovements.aplication.MovementOperations;
import com.banck.creditmovements.dto.ProductMovementDto;
import com.banck.creditmovements.utils.Concept;
import com.banck.creditmovements.utils.DateValidator;
import com.banck.creditmovements.utils.DateValidatorUsingLocalDate;
import com.banck.creditmovements.utils.Modality;
import com.banck.creditmovements.utils.MovementType;
import com.banck.creditmovements.utils.ProductType;
import java.time.LocalDate;

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
@RequestMapping("/mov-credit")
@RequiredArgsConstructor
public class MovementController {

    Logger logger = LoggerFactory.getLogger(MovementController.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("America/Bogota"));
    private final MovementOperations operations;
    private final DebitAccountOperations accountOperations;

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

    @GetMapping("/product/{id}/list")
    public Flux<Movement> listByProduct(@PathVariable("id") String id) {
        return operations.listByProduct(id);
    }

    @GetMapping("/customer-product/{customer}/{product}/list")
    public Flux<Movement> listByCustomerAndCredit(@PathVariable("customer") String customer, @PathVariable("product") String product) {
        return operations.listByCustomerAndProduct(customer, product);
    }

    @GetMapping("/card/last-10-movements")
    public Flux<CardMovementDto> reportLast10CardMovements() {
        return operations.listLast10CardMovements()
                .filter(fm -> Optional.ofNullable(fm.getProduct()).isPresent()
                && Optional.ofNullable(fm.getProductType()).isPresent())
                .filter(f2m -> f2m.getProductType().equals(ProductType.TARGETA_CREDITO.value)
                || f2m.getProductType().equals(ProductType.TARGETA_DEBITO.value))
                .groupBy(gb -> gb.getProduct())
                .flatMap(gm -> {
                    return gm.takeLast(10).collectList().map(lm -> {
                        CardMovementDto cm = new CardMovementDto();
                        cm.setCard(gm.key());
                        cm.setMovements(lm);
                        return cm;
                    });
                });
    }

    @PostMapping("/product/movement/{customer}/list")
    public Flux<ProductMovementDto> ProductMovementByCustomerAndDate(@PathVariable("customer") String customer, @RequestBody DateIDateF didf) {

        DateValidator validator = new DateValidatorUsingLocalDate(formatDate);

        if (!validator.isValid(didf.getDateI())) {
            Throwable t = new Throwable();
            return Flux.error(t, true);
        }

        if (!validator.isValid(didf.getDateF())) {
            Throwable t = new Throwable();
            return Flux.error(t, false);
        }

        return operations.listProductMovementBetweenDatesAndCustomer(customer, didf.getDateI(), didf.getDateF())
                .filter(fm -> Optional.ofNullable(fm.getProduct()).isPresent()
                && Optional.ofNullable(fm.getDate()).isPresent()
                && !Optional.ofNullable(fm.getDate()).isEmpty())
                .filter(fm -> isDateRange(didf.getDateI(), didf.getDateF(), fm.getDate()))
                .groupBy(gb -> gb.getProduct())
                .flatMap(gm -> {
                    return gm.collectList().map(lm -> {
                        ProductMovementDto cm = new ProductMovementDto();
                        cm.setProduct(gm.key());
                        cm.setMovements(lm);
                        return cm;
                    });
                });
    }

    @PostMapping
    public Mono<ResponseEntity> create(@RequestBody Movement movem) {
        movem.setMovement(getRandomNumberString());
        movem.setDate(dateTime.format(formatDate));
        movem.setHour(dateTime.format(formatTime));
        return Mono.just(movem).flatMap(movement -> {

            String msgTipoMovimiento = ""
                    + "Abono = { \"movementType\": \"ABONO\" }\n"
                    + "Cargo = { \"movementType\": \"CARGO\" }";

            String msgTipoProducto = ""
                    + "Targeta Debito = { \"productType\": \"TARGETA-DEBITO\" }\n"
                    + "Targeta Credito = { \"productType\": \"TARGETA-CREDITO\" }";

            String msgConcepto = ""
                    + "Pagar targeta = { \"concept\": \"PAGO-TARGETA\" }\n"
                    + "Retiro con targeta = { \"concept\": \"RETIRO-TARGETA\"";

            if (Optional.ofNullable(movement.getMovementType()).isEmpty()) {
                return Mono.just(
                        new ResponseEntity("Debe ingresar Tipo Movimiento, Ejemplo { \"movementType\": \"ABONO\" }",
                                HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getConcept()).isEmpty()) {
                return Mono.just(new ResponseEntity(
                        "Debe ingresar el Concepto, Ejemplo { \"concept\": \"PAGO-TARGETA\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getProduct()).isEmpty()) {
                return Mono.just(new ResponseEntity(
                        "Debe ingresar el producto, Ejemplo { \"product\": \"TD-00000\" }", HttpStatus.BAD_REQUEST));
            }
            if (Optional.ofNullable(movement.getCustomer()).isEmpty()) {
                return Mono.just(new ResponseEntity(
                        "Debe ingresar el cliente, Ejemplo { \"customer\": \"10101010\" }", HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getProductType()).isEmpty()) {
                return Mono.just(new ResponseEntity("Debe ingresar el tipo producto, Ejemplo { \"productType\": \"TARGETA-DEBITO\" }", HttpStatus.BAD_REQUEST));
            }

            boolean isMovementType = false;
            for (MovementType tc : MovementType.values()) {
                if (movement.getMovementType().toUpperCase().equals(tc.value)) {
                    isMovementType = true;
                }
            }

            boolean isConcept = false;
            for (Concept tc : Concept.values()) {
                if (movement.getConcept().toUpperCase().equals(tc.value)) {
                    isConcept = true;
                }
            }

            boolean isProductType = false;
            for (ProductType tc : ProductType.values()) {
                if (movement.getProductType().equals(tc.value)) {
                    isProductType = true;
                }
            }

            if (!isMovementType || Optional.ofNullable(movement.getMovementType()).isEmpty()) {
                return Mono.just(new ResponseEntity(""
                        + "Solo existen estos Movimiento: \n"
                        + msgTipoMovimiento, HttpStatus.BAD_REQUEST));
            }
            if (!isProductType || Optional.ofNullable(movement.getProductType()).isEmpty()) {
                return Mono.just(new ResponseEntity(""
                        + "Solo existen estos Codigos de Tipo Producto: \n"
                        + msgTipoProducto, HttpStatus.BAD_REQUEST));
            }
            if (!isConcept || Optional.ofNullable(movement.getConcept()).isEmpty()) {
                return Mono.just(new ResponseEntity(""
                        + "Solo existen estos Codigos Concepto: \n"
                        + msgConcepto, HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getAmount()).isEmpty() || movement.getAmount() <= 0) {
                return Mono.just(new ResponseEntity("Debe ingresar el monto, Ejemplo { \"amount\": 240 }",
                        HttpStatus.BAD_REQUEST));
            }

            movement.setMovement(getRandomNumberString());
            movement.setObservation("Amortizacion de cuota");
            movement.setState(true);

            return operations.create(movement).flatMap(scheduleRes -> {
                return Mono.just(new ResponseEntity(scheduleRes, HttpStatus.OK));
            });
        });
    }

    @PostMapping("/debit-card/payment")
    public Mono<ResponseEntity> debitCardPayment(@RequestBody Movement movementReq) {

        return Mono.just(movementReq).flatMap(movement -> {

            if (Optional.ofNullable(movement.getCustomer()).isEmpty()) {
                return Mono.just(
                        new ResponseEntity("Debe ingresar el cliente, Ejemplo { \"customer\": \"10101010\" }",
                                HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getProduct()).isEmpty()) {
                return Mono.just(
                        new ResponseEntity("Debe el producto a debitar, Ejemplo { \"product\": \"TD-00000\" }",
                                HttpStatus.BAD_REQUEST));
            }
            if (Optional.ofNullable(movement.getThirdProduct()).isEmpty()) {
                return Mono.just(
                        new ResponseEntity("Debe el producto a abonar, Ejemplo { \"thirdProduct\": \"TD-00002\" }",
                                HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getAmount()).isEmpty() || movement.getAmount() <= 0) {
                return Mono.just(new ResponseEntity("Debe ingresar el monto, Ejemplo { \"amount\": 240 }",
                        HttpStatus.BAD_REQUEST));
            }

            if (Optional.ofNullable(movement.getObservation()).isEmpty()) {
                return Mono.just(
                        new ResponseEntity("Debe una descripcion, Ejemplo { \"observation\": \"Pago de targeta de credito con targeta de debito\" }",
                                HttpStatus.BAD_REQUEST));
            }

            //Insertar cargo en las cuentas asociadas a la Targeta de Debito
            return accountOperations.debitCardPayment(movement.getProduct(), movement.getAmount()).flatMap(tr -> {

                if (tr.getCode().equals("1")) {

                    //Insertar un Cargo  al producto Origen
                    if (movement.getAmount() >= 0) {
                        movement.setAmount(movement.getAmount() * -1);
                    }
                    movementReq.setMovement(getRandomNumberString());
                    movement.setProduct(movementReq.getProduct());
                    movement.setModality(Modality.VENTANILLA.value);
                    movement.setMovementType(MovementType.CARGO.value);
                    movement.setConcept(Concept.PAGO_TARGETA.value);

                    movement.setObservation("Envio a la targeta " + movement.getThirdProduct());

                    movement.setProductType(ProductType.TARGETA_DEBITO.value);
                    movement.setThirdClient("");
                    movement.setDate(dateTime.format(formatDate));
                    movement.setHour(dateTime.format(formatTime));
                    movement.setState(true);

                    return operations.create(movement).flatMap(movR -> {

                        //Insertar un Abono al producto Destino
                        if (movement.getAmount() <= 0) {
                            movement.setAmount(movement.getAmount() * -1);
                        }

                        Movement movement1 = movement;
                        movement1.setMovement(getRandomNumberString());
                        movement1.setProduct(movement.getThirdProduct());
                        movement1.setProductType(ProductType.TARGETA_CREDITO.value);
                        movement1.setCustomer(movement.getThirdClient());
                        movement1.setThirdProduct(movR.getProduct());
                        movement1.setThirdClient(movR.getCustomer());

                        movement1.setModality(Modality.VENTANILLA.value);
                        movement1.setMovementType(MovementType.ABONO.value);
                        movement1.setConcept(Concept.ABONO_TARGETA.value);

                        movement1.setObservation("Recibio un abono desde la targeta " + movR.getProduct());

                        movement1.setDate(dateTime.format(formatDate));
                        movement1.setHour(dateTime.format(formatTime));
                        movement1.setState(true);

                        return operations.create(movement1).flatMap(movR2 -> {
                            return Mono.just(new ResponseEntity(movR, HttpStatus.OK));
                        });
                    });
                } else {
                    return Mono.just(new ResponseEntity(tr.getMessage(), HttpStatus.BAD_REQUEST));
                }
            }).onErrorReturn(new ResponseEntity("Ocurrio un error en el servidor de Movimientos en Cuentas",
                    HttpStatus.BAD_REQUEST));
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
        int number = rnd.nextInt(999999999);
        return String.format("%09d", number);
    }

    public boolean isDateRange(String strDateI, String strDateF, String strDateC) {
        LocalDate dateI = LocalDate.parse(strDateI, formatDate);
        LocalDate dateF = LocalDate.parse(strDateF, formatDate);
        LocalDate dateC = LocalDate.parse(strDateC, formatDate);
        return ((dateC.isAfter(dateI) || dateC.isEqual(dateI)) && (dateC.isBefore(dateF) || dateC.isEqual(dateF)));
    }
}
