package com.banck.creditmovements.aplication;

import com.banck.creditmovements.dto.AnyDto;
import reactor.core.publisher.Mono;

/**
 *
 * @author jonavcar
 */
public interface DebitAccountOperations {

    public Mono<AnyDto> debitCardPayment(String debitCard, double amount);

}
