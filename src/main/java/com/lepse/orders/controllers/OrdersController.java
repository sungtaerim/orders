package com.lepse.orders.controllers;

import com.lepse.orders.models.ModelRequest;
import com.lepse.orders.models.OrderModel;
import com.lepse.orders.service.FindOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/order"})
public class OrdersController {

    private final FindOrder findOrder;

    @Autowired
    public OrdersController(FindOrder findOrder) {
        this.findOrder = findOrder;
    }

    @GetMapping({"/{code}"})
    public String findOrder(@PathVariable String code) {
        return findOrder.changeProperties(code);
    }

    @PostMapping({"/update/{orderId}"})
    public String update(@RequestBody ModelRequest<OrderModel> orderModel, @PathVariable String orderId) {
        try {
            System.out.println(orderModel.getModel().get(0).getOrderCode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Something went wrong";
    }
}
