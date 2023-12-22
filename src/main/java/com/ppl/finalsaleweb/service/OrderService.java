package com.ppl.finalsaleweb.service;

import com.ppl.finalsaleweb.Objects.AnalyticsData;
import com.ppl.finalsaleweb.model.Customer;
import com.ppl.finalsaleweb.model.Order;
import com.ppl.finalsaleweb.model.Product;
import com.ppl.finalsaleweb.repository.CustomerRepository;
import com.ppl.finalsaleweb.repository.OrderRepository;
import com.ppl.finalsaleweb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order orderData) throws Exception {
        Customer customer = customerRepository.findByPhoneNum(orderData.getPhoneNum());
        if (customer == null) {
            customer = new Customer();
            // Assuming Customer has fields phoneNum, fullName, and address
            customer.setPhoneNum(orderData.getPhoneNum());
            customer.setFullName(orderData.getFullName());
            customer.setAddress(orderData.getAddress());
            customerRepository.save(customer);
        }

        Order order = new Order();
        // Copying properties from orderData to order
        order.setPhoneNum(orderData.getPhoneNum());
        order.setFullName(orderData.getFullName());
        order.setAddress(orderData.getAddress());
        order.setPayMethod(orderData.getPayMethod());
        order.setProducts(orderData.getProducts()); // Deep copy if necessary
        order.setTotalAmount(orderData.getTotalAmount());
        order.setCashBack(orderData.getCashBack());
        order.setCashAmount(orderData.getCashAmount());
        order.setDate(orderData.getDate());
        order.setTotalQuantity(orderData.getTotalQuantity());

        // Update the quantities of the products in the order
        for (Order.ProductItem item : orderData.getProducts()) {
            Optional<Product> existingProductOpt = productRepository.findByBarcode(item.getValue().getBarcode());
            if (existingProductOpt.isPresent()) {
                Product existingProduct = existingProductOpt.get();
                existingProduct.setQuantity(String.valueOf(Integer.parseInt(existingProduct.getQuantity()) - item.getQuantity()));
                productRepository.save(existingProduct);
            }
        }

        // Save the order
        order = orderRepository.save(order);

        // Add the order to the customer's list of orders and save
        if (customer.getOrderIds() == null) {
            customer.setOrderIds(new ArrayList<>());
        }
        customer.getOrderIds().add(order);
        customerRepository.save(customer);

        return order;
    }


    public AnalyticsData getAnalyticsData(String timeline, Date customStartDate, Date customEndDate) {
        Date startDate;
        Date endDate;
        Calendar calendar = Calendar.getInstance();

        if ("custom".equals(timeline)) {
            // For custom range, use the provided dates
            startDate = customStartDate;
            endDate = customEndDate;
        } else {
            // For predefined timelines, calculate the start and end dates
            switch (timeline) {
                case "today":
                    startDate = new Date();
                    endDate = new Date();
                    break;
                case "yesterday":
                    calendar.add(Calendar.DATE, -1);
                    startDate = endDate = calendar.getTime();
                    break;
                case "last7days":
                    calendar.add(Calendar.DATE, -6);
                    startDate = calendar.getTime();
                    endDate = new Date();
                    break;
                case "thismonth":
                    calendar.set(Calendar.DATE, 1);
                    startDate = calendar.getTime();
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                    endDate = calendar.getTime();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid timeline parameter");
            }
        }

        List<Order> orders = orderRepository.findByDateBetween(startDate, endDate);
        double totalAmount = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        int totalOrders = orders.size();
        int totalProducts = orders.stream().mapToInt(Order::getTotalQuantity).sum();

        return new AnalyticsData(totalAmount, totalOrders, totalProducts, orders);
    }



}
