package com.banck.creditmovements.aplication.impl;

import com.banck.creditmovements.domain.Movement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.banck.creditmovements.aplication.MovementOperations;
import com.banck.creditmovements.aplication.model.MovementRepository;

/**
 *
 * @author jonavcar
 */
@Service
@RequiredArgsConstructor
public class MovementOperationsImpl implements MovementOperations {

    private final MovementRepository movementRepository;

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
    public Flux<Movement> listLast10CardMovements() {
        return movementRepository.list();
    }

    @Override
    public Flux<Movement> listProductMovementBetweenDatesAndCustomer(String customer, String dateI, String dateF) {
        return movementRepository.listByCustomer(customer);
    }

    @Override
    public Flux<Movement> listByProduct(String product) {
        return movementRepository.listByProduct(product);
    }

    @Override
    public Flux<Movement> listByCustomerAndProduct(String customer, String product) {
       return movementRepository.listByCustomerAndProduct(customer, product);
    }

}
