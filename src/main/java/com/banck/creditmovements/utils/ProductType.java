/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banck.creditmovements.utils;

/**
 *
 * @author jonavcar
 */
public enum ProductType {
    // TARGETA DEBITO
    TARGETA_DEBITO("TARGETA-DEBITO") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    // TARGETA CREDITO
    TARGETA_CREDITO("TARGETA-CREDITO") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    // PRESTAMO-PERSONAL
    PRESTAMO_PERSONAL("PRESTAMO-PERSONAL") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    // PRESTAMO EMPRESARIAL
    PRESTAMO_EMPRESARIAL("PRESTAMO-EMPRESARIAL") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    };

    public final String value;

    public boolean equals(String customerType) {
        return value.equals(customerType);
    }

    private ProductType(String value) {
        this.value = value;
    }
}
