package com.excelr.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.excelr.model.*;
import com.excelr.repo.UserRepository;
import com.excelr.service.ExcelRService;
import com.excelr.service.EmailService;
import com.excelr.util.JwtUtil;

import jakarta.mail.MessagingException;

@RestController
@CrossOrigin(origins = "http://localhost:3000/")
public class ExcelRController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ExcelRService excelRService;

    // User Authentication
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginData) throws IOException {
        String username = loginData.get("username");
        String email = loginData.get("email");
        String password = loginData.get("password");

        if (username == null || username.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("login", "fail");
            errorResponse.put("message", "Username, email, and password must be provided.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password) && user.get().getEmail().equals(email)) {
            Map<String, String> response = new HashMap<>();
            String token = jwtUtil.generateToken(username);  // Generate JWT token
            response.put("login", "success");
            response.put("token", token);
            response.put("role", user.get().getRole()); // Send role back to frontend

            return ResponseEntity.ok(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("login", "fail");
            errorResponse.put("message", "Invalid username, email, or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // User Registration
    @PostMapping("/register")
    public User register(@RequestBody User user) throws IOException {
        // Register the user without sending a registration email
        return excelRService.saveUser(user);
    }

    // Get all Users
    @GetMapping("/get/users")
    public List<User> getAllUsers() {
        return excelRService.getAllUsers();
    }

    // General Category Endpoints
    @GetMapping("/products/{category}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String category) {
        switch (category.toLowerCase()) {
            case "laptops":
                return ResponseEntity.ok(excelRService.getLaptops());
            case "mobiles":
                return ResponseEntity.ok(excelRService.getMobiles());
            case "headphones":
                return ResponseEntity.ok(excelRService.getHeadphones());
            case "watches":
                return ResponseEntity.ok(excelRService.getWatches());
            case "cameras":
                return ResponseEntity.ok(excelRService.getCameras());
            default:
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid Category");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/products/{category}/{id}")
    public ResponseEntity<?> getSingleProduct(@PathVariable String category, @PathVariable Long id) {
        switch (category.toLowerCase()) {
            case "laptops":
                return ResponseEntity.of(excelRService.getLaptopById(id));
            case "mobiles":
                return ResponseEntity.of(excelRService.getMobileById(id));
            case "headphones":
                return ResponseEntity.of(excelRService.getHeadphoneById(id));
            case "watches":
                return ResponseEntity.of(excelRService.getWatchById(id));
            case "cameras":
                return ResponseEntity.of(excelRService.getCameraById(id));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category.");
        }
    }

    // Upload Product (All categories)
    @PostMapping("/admin/upload/{category}")
    public ResponseEntity<?> uploadProduct(@PathVariable String category,
                                           @RequestParam String name, 
                                           @RequestParam int cost,
                                           @RequestParam int quantity, 
                                           @RequestParam String description,
                                           @RequestParam MultipartFile file) {
        if (name == null || name.isEmpty() || cost <= 0 || file == null || file.isEmpty() || quantity <= 0 || description == null || description.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input parameters");
        } 
        
        try {
            switch (category.toLowerCase()) {
                case "laptops":
                    return ResponseEntity.ok(excelRService.saveLaptop(name, cost, quantity, description, file));
                case "mobiles":
                    return ResponseEntity.ok(excelRService.saveMobile(name, cost, quantity, description, file));
                case "headphones":
                    return ResponseEntity.ok(excelRService.saveHeadphone(name, cost, quantity, description, file));
                case "watches":
                    return ResponseEntity.ok(excelRService.saveWatch(name, cost, quantity, description, file));
                case "cameras":
                    return ResponseEntity.ok(excelRService.saveCamera(name, cost, quantity, description, file));
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while uploading product.");
        }
    }

    // Update Product (All categories)
    @PutMapping("/admin/update/product/{category}/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String category,
                                           @PathVariable Long id,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) Integer cost,
                                           @RequestParam(required = false) Integer quantity,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) MultipartFile file) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product ID is required for updating");
        }

        try {
            switch (category.toLowerCase()) {
                case "laptops":
                    return ResponseEntity.ok(excelRService.updateLaptop(id, name, cost, quantity, description, file));
                case "mobiles":
                    return ResponseEntity.ok(excelRService.updateMobile(id, name, cost, quantity, description, file));
                case "headphones":
                    return ResponseEntity.ok(excelRService.updateHeadphone(id, name, cost, quantity, description, file));
                case "watches":
                    return ResponseEntity.ok(excelRService.updateWatch(id, name, cost, quantity, description, file));
                case "cameras":
                    return ResponseEntity.ok(excelRService.updateCamera(id, name, cost, quantity, description, file));
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while updating product.");
        }
    }

    // Delete Product (All categories)
    @DeleteMapping("/admin/delete/product/{category}/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String category, @PathVariable Long id) {
        try {
            switch (category.toLowerCase()) {
                case "laptops":
                    excelRService.deleteLaptop(id);
                    break;
                case "mobiles":
                    excelRService.deleteMobile(id);
                    break;
                case "headphones":
                    excelRService.deleteHeadphone(id);
                    break;
                case "watches":
                    excelRService.deleteWatch(id);
                    break;
                case "cameras":
                    excelRService.deleteCamera(id);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category.");
            }
            return ResponseEntity.ok(category + " deleted successfully");
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the exception stack trace to the console or log file
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception stack trace to the console or log file
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting product: " + e.getMessage());
        }
    }
    
 // Get all laptops
    @GetMapping("/laptops")
    public ResponseEntity<?> getLaptops() {
        try {
            return ResponseEntity.ok(excelRService.getLaptops());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching laptops.");
        }
    }

    // Get all mobiles
    @GetMapping("/mobiles")
    public ResponseEntity<?> getMobiles() {
        try {
            return ResponseEntity.ok(excelRService.getMobiles());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching mobiles.");
        }
    }

    // Get all watches
    @GetMapping("/watches")
    public ResponseEntity<?> getWatches() {
        try {
            return ResponseEntity.ok(excelRService.getWatches());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching watches.");
        }
    }

    // Get all headphones
    @GetMapping("/headphones")
    public ResponseEntity<?> getHeadphones() {
        try {
            return ResponseEntity.ok(excelRService.getHeadphones());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching headphones.");
        }
    }

    // Get all cameras
    @GetMapping("/cameras")
    public ResponseEntity<?> getCameras() {
        try {
            return ResponseEntity.ok(excelRService.getCameras());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching cameras.");
        }
    }
    
    // Get All Products
    @GetMapping("/allproducts")
    public List<AllProducts> getAllProducts() {
        return excelRService.getAllProducts();
    }
    
    @GetMapping("/allproducts/{id}")
    public Optional<AllProducts> getSingleProduct(@PathVariable Long id) {
        return excelRService.getProductById(id);
    }
    
    // Upload All Products (admin endpoint)
    @PostMapping("/admin/upload/allproducts")
    public ResponseEntity<?> uploadAllProduct(@RequestParam String name, 
                                              @RequestParam int cost, 
                                              @RequestParam int quantity, 
                                              @RequestParam String description, 
                                              @RequestParam MultipartFile file) {
        if (name == null || name.isEmpty() || cost <= 0 || file == null || file.isEmpty() || quantity <= 0 || description == null || description.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input parameters");
        }

        try {
            AllProducts savedProduct = excelRService.saveProduct(name, cost, quantity, description, file);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while uploading product.");
        }
    }
}