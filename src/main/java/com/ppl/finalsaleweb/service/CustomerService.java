package com.ppl.finalsaleweb.service;

import com.ppl.finalsaleweb.model.Customer;
import com.ppl.finalsaleweb.model.Order;
import com.ppl.finalsaleweb.repository.CustomerRepository;
import com.ppl.finalsaleweb.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public List<Order> getCustomerPurchaseHistory(String customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        return customerOpt.map(Customer::getOrderIds).orElse(new ArrayList<>());
    }
}
