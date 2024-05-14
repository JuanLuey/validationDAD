package pe.intercorp.ValidateDad.entity;

import java.util.List;

import lombok.Data;

@Data
public class Product {
    private List<Object> barcodes;
    private Object attributes;
}