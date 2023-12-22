package com.ppl.finalsaleweb.repository;

import com.ppl.finalsaleweb.model.Order;
import com.ppl.finalsaleweb.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface OrderRepository extends MongoRepository<Order, String> {


    List<Order> findByDateBetween(Date startDate, Date endDate);
}
