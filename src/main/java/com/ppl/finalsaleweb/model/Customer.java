package com.ppl.finalsaleweb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.List;

@Document(collection = "customers")
@Getter
@Setter
public class Customer {

    @Id
    private String id;

    private String phoneNum;
    private String fullName;
    private String address;

    @DBRef
    private List<Order> orderIds;


}
