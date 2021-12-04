package com.banck.accountmovements.domain;

import lombok.Data;

/**
 *
 * @author jonavcar
 */
@Data
public class DebitcardaccountDto {
    
    public String debitCard;
    public String account;
    public boolean main;
    public int order;
    public double amount;
    public String status;
}
