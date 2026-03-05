package com.serverest.factory;

import com.serverest.model.Product;
import com.serverest.utils.DataFactory;

public class ProductFactory {
    public static Product createDefault() {
        return DataFactory.createProduct();
    }
}