package com.ppl.finalsaleweb.service;

import com.ppl.finalsaleweb.Objects.ProductRequest;
import com.ppl.finalsaleweb.model.Product;
import com.ppl.finalsaleweb.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository; // Replace with your actual repository

    private static final String UPLOAD_DIR = "src/main/resources/uploads/product_images";

    private final Logger logger = LoggerFactory.getLogger(ProductService.class);


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    public ResponseEntity<?> addProduct(ProductRequest request) {
        try {

            if (productRepository.existsByProductName(request.getProductName())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST).body("");

            }

            Product product = new Product();
            product.setBarcode(generateUniqueBarcode());
            product.setProductName(request.getProductName());
            product.setImportPrice(request.getImportPrice());
            product.setRetailPrice(request.getRetailPrice());
            product.setCategory(request.getCategory());
            product.setQuantity(request.getQuantity());

            // Handle product image
            MultipartFile productImage = request.getProductImage();
            if (productImage != null && !productImage.isEmpty()) {
                String uniqueFilename = UUID.randomUUID() + "_" + productImage.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);
                Path imagePath = uploadPath.resolve(uniqueFilename);

                String filePath = imagePath.toFile().getCanonicalPath();

                productImage.transferTo(new File(filePath));
                product.setImageUrl(uniqueFilename);

            }

            productRepository.save(product);


            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> updateProduct(String barcode, ProductRequest request) {
        try {
            Optional<Product> productOpt = productRepository.findByBarcode(barcode);
            if (productOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }

            Product product = productOpt.get();
            product.setProductName(request.getProductName());
            product.setImportPrice(request.getImportPrice());
            product.setRetailPrice(request.getRetailPrice());
            product.setCategory(request.getCategory());
            product.setQuantity(request.getQuantity());

            MultipartFile productImage = request.getProductImage();
            if (productImage != null && !productImage.isEmpty()) {
                // Delete the existing image if it exists
                if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                    Path existingImagePath = Paths.get(UPLOAD_DIR, product.getImageUrl());
                    Files.deleteIfExists(existingImagePath);
                }

                // Save the new image
                String uniqueFilename = UUID.randomUUID() + "_" + productImage.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);
                Path imagePath = uploadPath.resolve(uniqueFilename);
                String filePath = imagePath.toFile().getCanonicalPath();

                productImage.transferTo(new File(filePath));
                product.setImageUrl(uniqueFilename);
            }else{
                product.setImageUrl(product.getImageUrl());
            }

            productRepository.save(product);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + e.getMessage());
        }
    }

    public void deleteProduct(String barcode) throws Exception {
        Optional<Product> existingProduct = productRepository.findByBarcode(barcode);
        if (existingProduct.isEmpty()) {
            throw new Exception("Product not found");
        }

        Product product = existingProduct.get();

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            Path existingImagePath = Paths.get(UPLOAD_DIR, product.getImageUrl());
            Files.deleteIfExists(existingImagePath);
        }



        productRepository.delete(existingProduct.get());
    }

    private String generateUniqueBarcode() {
        String barcode;
        do {
            barcode = String.valueOf(generateRandomInteger(100000, 999999)); // Implement this method
        } while (productRepository.existsByBarcode(barcode));

        return barcode;
    }

    private int generateRandomInteger(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }



}
