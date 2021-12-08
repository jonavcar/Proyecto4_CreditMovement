/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banck.creditmovements.dto;

import com.banck.creditmovements.domain.Movement;
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
