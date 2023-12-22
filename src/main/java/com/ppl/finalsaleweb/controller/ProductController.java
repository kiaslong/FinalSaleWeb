package com.ppl.finalsaleweb.controller;

import com.ppl.finalsaleweb.Objects.ProductRequest;
import com.ppl.finalsaleweb.model.Product;
import com.ppl.finalsaleweb.service.ProductService;
import com.ppl.finalsaleweb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/product_images";



    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @GetMapping("/product_images/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            if (!Files.exists(file) || !Files.isReadable(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource fileSystemResource = new FileSystemResource(file);
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream"; // default content type
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileSystemResource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("importPrice") String importPrice,
            @RequestParam("retailPrice") String retailPrice,
            @RequestParam("category") String category,
            @RequestParam("quantity") String quantity,
            @RequestParam("productImage") MultipartFile productImage) {

        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName(productName);
        productRequest.setImportPrice(importPrice);
        productRequest.setRetailPrice(retailPrice);
        productRequest.setCategory(category);
        productRequest.setQuantity(quantity);
        productRequest.setProductImage(productImage);

        try {
            return productService.addProduct(productRequest);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PutMapping("/{barcode}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String barcode,
            @RequestParam("productName") String productName,
            @RequestParam("importPrice") String importPrice,
            @RequestParam("retailPrice") String retailPrice,
            @RequestParam("category") String category,
            @RequestParam("quantity") String quantity,
            @RequestParam(value = "productImage", required = false) MultipartFile productImage) {



        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName(productName);
        productRequest.setImportPrice(importPrice);
        productRequest.setRetailPrice(retailPrice);
        productRequest.setCategory(category);
        productRequest.setQuantity(quantity);
        productRequest.setProductImage(productImage);

        try {
            return productService.updateProduct(barcode, productRequest);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }
    @DeleteMapping("/{barcode}")
    public ResponseEntity<?> deleteProduct(@PathVariable String barcode) {
        try {
            productService.deleteProduct(barcode);
            return ResponseEntity.ok().body("Product deleted successfully");
        } catch (Exception e) {
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
