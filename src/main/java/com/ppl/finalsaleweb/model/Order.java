package com.ppl.finalsaleweb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
public class Order {

    @Id
    private String id;

    private String phoneNum;
    private String fullName;
    private String address;
    private PayMethod payMethod; // Enum for payment methods
    private List<ProductItem> products;
    private Double totalAmount;
    private Double cashBack;
    private Double cashAmount;
    private Date date;
    private Integer totalQuantity;



    @Getter
    @Setter
    public static class ProductItem {
        private ProductDetails value;
        private String label;
        private Integer quantity;


    }
    @Getter
    @Setter

    public static class ProductDetails {
        private String barcode;
        private String productName;
        private String importPrice;
        private String retailPrice;
        private String quantity;
        private String category;
        private String imageUrl;
        private Date creationDate;

        // Getters and setters...
    }

    public enum PayMethod {
        Cash, Card;

    }

}
