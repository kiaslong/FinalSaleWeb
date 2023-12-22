package com.ppl.finalsaleweb.repository;

import com.ppl.finalsaleweb.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    Customer findByPhoneNum(String phoneNum);
}
