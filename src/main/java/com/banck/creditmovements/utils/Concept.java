/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banck.creditmovements.utils;

/**
 *
 * @author jonavcar
 */
public enum Concept {
    //PAGO-TARGETA
    PAGO_TARGETA("PAGO-TARGETA") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    ABONO_TARGETA("ABONO-TARGETA") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    //RETIRO-TARGETA
    RETIRO_TARGETA("RETIRO-TARGETA") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    };

    public final String value;

    public boolean equals(String customerType) {
        return value.equals(customerType);
    }

    private Concept(String value) {
        this.value = value;
    }
}
