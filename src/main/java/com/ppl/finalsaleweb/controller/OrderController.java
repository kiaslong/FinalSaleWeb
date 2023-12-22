package com.ppl.finalsaleweb.controller;

import com.ppl.finalsaleweb.Objects.AnalyticsData;
import com.ppl.finalsaleweb.model.Order;
import com.ppl.finalsaleweb.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            // Log the exception here (optional)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalyticsData(@RequestParam String timeline,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        try {
            AnalyticsData data = orderService.getAnalyticsData(timeline, start, end);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity<?> addOrder(@RequestBody Order orderData) {
        try {
            Order order = orderService.createOrder(orderData);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }




}
