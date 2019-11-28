package com.company;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

public class StorageItem implements Serializable {
    String name;
    BigInteger code;
    int quantity;
    LocalDate expDate;

    public StorageItem(String name, BigInteger code, int quantity, LocalDate expDate) {
        this.name = name;
        this.code = code;
        this.quantity = quantity;
        this.expDate = expDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getCode() {
        return code;
    }

    public void setCode(BigInteger code) {
        this.code = code;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    @Override
    public String toString() {

        System.out.format("%20s%30s%20s%20s", name, code, quantity,expDate);
        System.out.println("");
        return"";
    }
}
