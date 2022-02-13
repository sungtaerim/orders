package com.lepse.orders.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.lepse.integrations.request.BaseRequest;

import java.util.List;

@JsonRootName("request")
public class ModelRequest<OrderModel> extends BaseRequest {

    @JsonProperty("orders")
    private final List<OrderModel> model;

    public ModelRequest(String sender, String integrationName, List<OrderModel> model) {
        super(sender, integrationName);
        this.model = model;
    }

    public List<OrderModel> getModel() {
        return model;
    }
}
