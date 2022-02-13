package com.lepse.orders.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.Date;

@Data
@JsonRootName("order")
public class OrderModel {

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("code")
    private String orderCode;

    @JsonProperty("date")
    private Date orderDate;
}
