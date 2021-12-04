/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banck.accountmovements.utils;

/**
 *
 * @author jonavcar
 */
public enum Modality {
    //VENTANILLA
    WINDOW("VT") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    //TARGETA-DEBITO
    DEBIT_CARD("TD") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    //CAJERO-AUTOMATICO
    CASHIER("CA") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    },
    //TARGETA-CREDITO
    CREDIT_CARD("TC") {
        @Override
        public boolean equals(String customerType) {
            return value.equals(customerType);
        }
    };

    public final String value;

    public boolean equals(String customerType) {
        return value.equals(customerType);
    }

    private Modality(String value) {
        this.value = value;
    }
}
