package com.ppl.finalsaleweb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;

@Document(collection = "products")
@Getter
@Setter
public class Product {

    @Id
    private String id;

    @Field("barcode")
    private String barcode;

    @Field("productName")
    private String productName;

    @Field("importPrice")
    private String importPrice;

    @Field("retailPrice")
    private String retailPrice;

    @Field("quantity")
    private String quantity;

    @Field("category")
    private String category;

    @Field("creationDate")
    private Date creationDate = new Date();

    @Field("imageUrl")
    private String imageUrl;


}
