package com.serverest.flow;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartFlowResult {
    private final String userToken;
    private final String cartId;
    private final String productId;
}