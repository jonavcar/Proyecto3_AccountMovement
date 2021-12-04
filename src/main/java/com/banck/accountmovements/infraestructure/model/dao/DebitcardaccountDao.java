package com.banck.accountmovements.infraestructure.model.dao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author jonavcar
 */
@Data
@Document("debitcardaccount")
public class DebitcardaccountDao {

    @Id
    public String debitcardaccount;
    public String debitCard;
    public String account;
    public String date;
    public boolean main;
    public int order;
    public String status;
}
