package com.ppl.finalsaleweb.repository;

import com.ppl.finalsaleweb.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    boolean existsByBarcode(String barcode);



    Optional<Product> findByBarcode(String barcode);

    boolean existsByProductName(String productName);
}
