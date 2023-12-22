package com.ppl.finalsaleweb.Objects;

import com.ppl.finalsaleweb.model.Order;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnalyticsData {
    private double totalAmount;
    private int totalOrders;
    private int totalProducts;
    private List<Order> orders;

    // Constructor
    public AnalyticsData(double totalAmount, int totalOrders, int totalProducts, List<Order> orders) {
        this.totalAmount = totalAmount;
        this.totalOrders = totalOrders;
        this.totalProducts = totalProducts;
        this.orders = orders;
    }


}
