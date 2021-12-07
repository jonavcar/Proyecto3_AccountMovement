/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banck.accountmovements.domain;

import java.util.List;

import lombok.Data;

/**
 *
 * @author jnacarra
 */
@Data
public class ProductMovementDto {

    String product;
    List<Movement> movements;
}
