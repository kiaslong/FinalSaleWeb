package com.ppl.finalsaleweb.Objects;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRequest {
    private String productName;
    private String importPrice;
    private String retailPrice;
    private String category;
    private String quantity;
    private MultipartFile productImage;
}
