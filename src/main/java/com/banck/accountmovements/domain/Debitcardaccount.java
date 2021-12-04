package com.banck.accountmovements.domain;

import lombok.Data;

/**
 *
 * @author jonavcar
 */
@Data
public class Debitcardaccount {

    public String debitcardaccount;
    public String debitCard;
    public String account;
    public String date;
    public boolean main;
    public int order;
    public String status;
}
