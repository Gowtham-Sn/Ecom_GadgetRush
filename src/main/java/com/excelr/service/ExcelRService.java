package com.excelr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.excelr.model.AllProducts;
import com.excelr.model.Cameras;
import com.excelr.model.Headphones;
import com.excelr.model.Laptops;
import com.excelr.model.Mobiles;
import com.excelr.model.User;
import com.excelr.model.Watches;
import com.excelr.repo.AllProductsRepo;
import com.excelr.repo.CamerasRepo;
import com.excelr.repo.HeadphonesRepo;
import com.excelr.repo.LaptopsRepo;
import com.excelr.repo.MobilesRepo;
import com.excelr.repo.UserRepository;
import com.excelr.repo.WatchesRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.io.IOException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;


   @Service
public class ExcelRService {
   @Autowired
   private UserRepository userRepository;
   @Autowired
   private LaptopsRepo laptopsRepo;
   @Autowired
   private MobilesRepo mobilesRepo;
   @Autowired
   private HeadphonesRepo headphonesRepo;
   @Autowired
   private WatchesRepo watchesRepo;
   @Autowired
   private CamerasRepo camerasRepo;
   @Autowired
   private AllProductsRepo allProductsRepo;
   
   @Value("${aws.s3.bucket.name}")
	 private String bucketName;
	    
	    
	 @Value("${aws.accessKeyId}")
	 private String accessKeyId;

	 @Value("${aws.secretAccessKey}")
	 private String secretAccessKey;
	 
	//getting reference of s3 buckets
		 private final S3Client s3Client = S3Client.builder()
		            .region(Region.EU_NORTH_1)
		            .credentialsProvider(StaticCredentialsProvider.create(
		                AwsBasicCredentials.create("AKIARSU7KU2M7HZO2MQR", "ey9uY5pixuHEryf8u4FMLj/+tuAxQEID8BYFADv/")
		            ))
		            .build();
	 
			public Laptops saveLaptop(String name, int cost, int quantity, String description, MultipartFile file)
					throws IOException {
				String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
				try {
					// Upload to S3
					s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

							.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
				} catch (Exception e) {
					throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
				} 

				// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
				// bucketName, Region.US_EAST_1.id(), fileName);

				String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
						fileName);
				System.out.println("File uploaded successfully. File URL: " + fileUrl);

				try {
					// Save to database
					Laptops laptop = new Laptops();
					laptop.setName(name);
					laptop.setCost(cost);
					laptop.setImage(fileUrl);
					laptop.setDescription(description);
					laptop.setQuantity(quantity);
					System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

					return laptopsRepo.save(laptop);
				} catch (Exception e) {
					throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
				}
			}
			public Laptops updateLaptop(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
		        Laptops existingLaptop = laptopsRepo.findById(id)
		                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

		        // Update fields if provided
		        if (name != null && !name.isEmpty()) {
		            existingLaptop.setName(name);
		        }
		        if (cost != null && cost > 0) {
		            existingLaptop.setCost(cost);
		        }
		        if (quantity != null && quantity > 0) {
		            existingLaptop.setQuantity(quantity);
		        }
		        if (description != null && !description.isEmpty())
		        	existingLaptop.setDescription(description);

		        // Handle file update if a new file is provided
		        if (file != null && !file.isEmpty()) {
		            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		            try {
		                s3Client.putObject(
		                    PutObjectRequest.builder()
		                        .bucket(bucketName)
		                        .key(fileName)
		                        .contentType(file.getContentType())
		                        .build(),
		                    RequestBody.fromBytes(file.getBytes())
		                );
		            } catch (Exception e) {
		                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
		            }

		            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
		            existingLaptop.setImage(fileUrl);
		        }

		        return laptopsRepo.save(existingLaptop);
		    }
			
			 public void deleteLaptop(Long id) {
			        // Fetch the laptop from the database
			        Laptops laptop = laptopsRepo.findById(id)
			                .orElseThrow(() -> new RuntimeException("Laptop not found"));

			        String fileUrl = laptop.getImage();
			        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

			        // Delete the file from S3
			        try {
			            s3Client.deleteObject(
			            		 DeleteObjectRequest.builder()
			                    .bucket(bucketName)
			                    .key(fileName)
			                    .build()
			            );
			        } catch (Exception e) {
			            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
			        }

			        // Delete the laptop from the database
			        laptopsRepo.deleteById(id);
			    }
			
			 public Mobiles saveMobile(String name, int cost, int quantity, String description, MultipartFile file)
						throws IOException {
					String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
					try {
						// Upload to S3
						s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

								.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
					} catch (Exception e) {
						throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
					} 

					// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
					// bucketName, Region.US_EAST_1.id(), fileName);

					String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
							fileName);
					System.out.println("File uploaded successfully. File URL: " + fileUrl);

					try {
						// Save to database
						Mobiles mobile = new Mobiles();
						mobile.setName(name);
						mobile.setCost(cost);
						mobile.setImage(fileUrl);
						mobile.setDescription(description);
						mobile.setQuantity(quantity);
						System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

						return mobilesRepo.save(mobile);
					} catch (Exception e) {
						throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
					}
				}
				public Mobiles updateMobile(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
			        Mobiles existingMobile = mobilesRepo.findById(id)
			                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

			        // Update fields if provided
			        if (name != null && !name.isEmpty()) {
			        	existingMobile.setName(name);
			        }
			        if (cost != null && cost > 0) {
			        	existingMobile.setCost(cost);
			        }
			        if (quantity != null && quantity > 0) {
			        	existingMobile.setQuantity(quantity);
			        }
			        if (description != null && !description.isEmpty())
			        	existingMobile.setDescription(description);

			        // Handle file update if a new file is provided
			        if (file != null && !file.isEmpty()) {
			            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			            try {
			                s3Client.putObject(
			                    PutObjectRequest.builder()
			                        .bucket(bucketName)
			                        .key(fileName)
			                        .contentType(file.getContentType())
			                        .build(),
			                    RequestBody.fromBytes(file.getBytes())
			                );
			            } catch (Exception e) {
			                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
			            }

			            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
			            existingMobile.setImage(fileUrl);
			        }

			        return mobilesRepo.save(existingMobile);
			    }
				
				 public void deleteMobile(Long id) {
				        // Fetch the laptop from the database
				        Mobiles mobile = mobilesRepo.findById(id)
				                .orElseThrow(() -> new RuntimeException("Laptop not found"));

				        String fileUrl = mobile.getImage();
				        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

				        // Delete the file from S3
				        try {
				            s3Client.deleteObject(
				                DeleteObjectRequest.builder()
				                    .bucket(bucketName)
				                    .key(fileName)
				                    .build()
				            );
				        } catch (Exception e) {
				            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
				        }

				        // Delete the laptop from the database
				        mobilesRepo.deleteById(id);
				    }
				 public Headphones saveHeadphone(String name, int cost, int quantity, String description, MultipartFile file)
							throws IOException {
						String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
						try {
							// Upload to S3
							s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

									.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
						} catch (Exception e) {
							throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
						} 

						// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
						// bucketName, Region.US_EAST_1.id(), fileName);

						String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
								fileName);
						System.out.println("File uploaded successfully. File URL: " + fileUrl);

						try {
							// Save to database
							Headphones headphone = new Headphones();
							headphone.setName(name);
							headphone.setCost(cost);
							headphone.setImage(fileUrl);
							headphone.setDescription(description);
							headphone.setQuantity(quantity);
							System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

							return headphonesRepo.save(headphone);
						} catch (Exception e) {
							throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
						}
					}
					public Headphones updateHeadphone(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
				        Headphones existingHeadphone = headphonesRepo.findById(id)
				                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

				        // Update fields if provided
				        if (name != null && !name.isEmpty()) {
				        	existingHeadphone.setName(name);
				        }
				        if (cost != null && cost > 0) {
				        	existingHeadphone.setCost(cost);
				        }
				        if (quantity != null && quantity > 0) {
				        	existingHeadphone.setQuantity(quantity);
				        }
				        if (description != null && !description.isEmpty())
				        	existingHeadphone.setDescription(description);

				        // Handle file update if a new file is provided
				        if (file != null && !file.isEmpty()) {
				            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
				            try {
				                s3Client.putObject(
				                    PutObjectRequest.builder()
				                        .bucket(bucketName)
				                        .key(fileName)
				                        .contentType(file.getContentType())
				                        .build(),
				                    RequestBody.fromBytes(file.getBytes())
				                );
				            } catch (Exception e) {
				                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
				            }

				            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
				            existingHeadphone.setImage(fileUrl);
				        }

				        return headphonesRepo.save(existingHeadphone);
				    }
					
					 public void deleteHeadphone(Long id) {
					        // Fetch the laptop from the database
					        Headphones headphone = headphonesRepo.findById(id)
					                .orElseThrow(() -> new RuntimeException("Laptop not found"));

					        String fileUrl = headphone.getImage();
					        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

					        // Delete the file from S3
					        try {
					            s3Client.deleteObject(
					                DeleteObjectRequest.builder()
					                    .bucket(bucketName)
					                    .key(fileName)
					                    .build()
					            );
					        } catch (Exception e) {
					            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
					        }

					        // Delete the laptop from the database
					        headphonesRepo.deleteById(id);
					    }
					
					 public Watches saveWatch(String name, int cost, int quantity, String description, MultipartFile file)
								throws IOException {
							String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
							try {
								// Upload to S3
								s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

										.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
							} catch (Exception e) {
								throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
							} 

							// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
							// bucketName, Region.US_EAST_1.id(), fileName);

							String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
									fileName);
							System.out.println("File uploaded successfully. File URL: " + fileUrl);

							try {
								// Save to database
								Watches watch = new Watches();
								watch.setName(name);
								watch.setCost(cost);
								watch.setImage(fileUrl);
								watch.setDescription(description);
								watch.setQuantity(quantity);
								System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

								return watchesRepo.save(watch);
							} catch (Exception e) {
								throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
							}
						}
						public Watches updateWatch(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
					        Watches existingWatch =watchesRepo.findById(id)
					                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

					        // Update fields if provided
					        if (name != null && !name.isEmpty()) {
					        	existingWatch.setName(name);
					        }
					        if (cost != null && cost > 0) {
					        	existingWatch.setCost(cost);
					        }
					        if (quantity != null && quantity > 0) {
					        	existingWatch.setQuantity(quantity);
					        }
					        if (description != null && !description.isEmpty())
					        	existingWatch.setDescription(description);

					        // Handle file update if a new file is provided
					        if (file != null && !file.isEmpty()) {
					            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
					            try {
					                s3Client.putObject(
					                    PutObjectRequest.builder()
					                        .bucket(bucketName)
					                        .key(fileName)
					                        .contentType(file.getContentType())
					                        .build(),
					                    RequestBody.fromBytes(file.getBytes())
					                );
					            } catch (Exception e) {
					                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
					            }

					            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
					            existingWatch.setImage(fileUrl);
					        }

					        return watchesRepo.save(existingWatch);
					    }
						
						 public void deleteWatch(Long id) {
						        // Fetch the laptop from the database
						        Watches watch = watchesRepo.findById(id)
						                .orElseThrow(() -> new RuntimeException("Laptop not found"));

						        String fileUrl = watch.getImage();
						        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

						        // Delete the file from S3
						        try {
						            s3Client.deleteObject(
						                DeleteObjectRequest.builder()
						                    .bucket(bucketName)
						                    .key(fileName)
						                    .build()
						            );
						        } catch (Exception e) {
						            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
						        }

						        // Delete the laptop from the database
						        watchesRepo.deleteById(id);
						    }
						
						 public Cameras saveCamera(String name, int cost, int quantity, String description, MultipartFile file)
									throws IOException {
								String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
								try {
									// Upload to S3
									s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

											.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
								} catch (Exception e) {
									throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
								} 

								// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
								// bucketName, Region.US_EAST_1.id(), fileName);

								String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
										fileName);
								System.out.println("File uploaded successfully. File URL: " + fileUrl);

								try {
									// Save to database
									Cameras camera = new Cameras();
									camera.setName(name);
									camera.setCost(cost);
									camera.setImage(fileUrl);
									camera.setDescription(description);
									camera.setQuantity(quantity);
									System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

									return camerasRepo.save(camera);
								} catch (Exception e) {
									throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
								}
							}
							public Cameras updateCamera(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
						        Cameras existingCamera =camerasRepo.findById(id)
						                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

						        // Update fields if provided
						        if (name != null && !name.isEmpty()) {
						        	existingCamera.setName(name);
						        }
						        if (cost != null && cost > 0) {
						        	existingCamera.setCost(cost);
						        }
						        if (quantity != null && quantity > 0) {
						        	existingCamera.setQuantity(quantity);
						        }
						        if (description != null && !description.isEmpty())
						        	existingCamera.setDescription(description);

						        // Handle file update if a new file is provided
						        if (file != null && !file.isEmpty()) {
						            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
						            try {
						                s3Client.putObject(
						                    PutObjectRequest.builder()
						                        .bucket(bucketName)
						                        .key(fileName)
						                        .contentType(file.getContentType())
						                        .build(),
						                    RequestBody.fromBytes(file.getBytes())
						                );
						            } catch (Exception e) {
						                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
						            }

						            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
						            existingCamera.setImage(fileUrl);
						        }

						        return camerasRepo.save(existingCamera);
						    }
							
							 public void deleteCamera(Long id) {
							        // Fetch the laptop from the database
							        Cameras watch = camerasRepo.findById(id)
							                .orElseThrow(() -> new RuntimeException("Laptop not found"));

							        String fileUrl = watch.getImage();
							        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

							        // Delete the file from S3
							        try {
							            s3Client.deleteObject(
							                DeleteObjectRequest.builder()
							                    .bucket(bucketName)
							                    .key(fileName)
							                    .build()
							            );
							        } catch (Exception e) {
							            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
							        }

							        // Delete the laptop from the database
							        camerasRepo.deleteById(id);
							    }
							 
							 public AllProducts saveProduct(String name, int cost, int quantity, String description, MultipartFile file)
										throws IOException {
									String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
									try {
										// Upload to S3
										s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(fileName)

												.contentType("image/jpeg").build(), RequestBody.fromBytes(file.getBytes()));
									} catch (Exception e) {
										throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
									} 

									// String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
									// bucketName, Region.US_EAST_1.id(), fileName);

									String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(),
											fileName);
									System.out.println("File uploaded successfully. File URL: " + fileUrl);

									try {
										// Save to database
										AllProducts product = new AllProducts();
										product.setName(name);
										product.setCost(cost);
										product.setImage(fileUrl);
										product.setDescription(description);
										product.setQuantity(quantity);
										System.out.println("Saving Laptop: name=" + name + ", cost=" + cost + ", pimage=" + fileUrl);

										return allProductsRepo.save(product);
									} catch (Exception e) {
										throw new RuntimeException("Error saving laptop to database: " + e.getMessage());
									}
								}
								public AllProducts updateProduct(Long id, String name, Integer cost, Integer quantity,String description, MultipartFile file) throws IOException {
							        AllProducts existingProduct =allProductsRepo.findById(id)
							                .orElseThrow(() -> new IllegalArgumentException("Laptop not found"));

							        // Update fields if provided
							        if (name != null && !name.isEmpty()) {
							        	existingProduct.setName(name);
							        }
							        if (cost != null && cost > 0) {
							        	existingProduct.setCost(cost);
							        }
							        if (quantity != null && quantity > 0) {
							        	existingProduct.setQuantity(quantity);
							        }
							        if (description != null && !description.isEmpty())
							        	existingProduct.setDescription(description);

							        // Handle file update if a new file is provided
							        if (file != null && !file.isEmpty()) {
							            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
							            try {
							                s3Client.putObject(
							                    PutObjectRequest.builder()
							                        .bucket(bucketName)
							                        .key(fileName)
							                        .contentType(file.getContentType())
							                        .build(),
							                    RequestBody.fromBytes(file.getBytes())
							                );
							            } catch (Exception e) {
							                throw new RuntimeException("Error uploading new file to S3: " + e.getMessage());
							            }

							            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, Region.EU_NORTH_1.id(), fileName);
							            existingProduct.setImage(fileUrl);
							        }

							        return allProductsRepo.save(existingProduct);
							    }
								
								 public void deleteProduct(Long id) {
								        // Fetch the laptop from the database
								        AllProducts product = allProductsRepo.findById(id)
								                .orElseThrow(() -> new RuntimeException("Laptop not found"));

								        String fileUrl = product.getImage();
								        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

								        // Delete the file from S3
								        try {
								            s3Client.deleteObject(
								                DeleteObjectRequest.builder()
								                    .bucket(bucketName)
								                    .key(fileName)
								                    .build()
								            );
								        } catch (Exception e) {
								            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
								        }

								        // Delete the laptop from the database
								        allProductsRepo.deleteById(id);
								    }

								 
   public List<User> getAllUsers() {
   return userRepository.findAll();
   }	 
   public User saveUser(User user) {
	   return userRepository.save(user);
   }
   public List<Laptops> getLaptops(){
	   return laptopsRepo.findAll();
   }
   public List<Mobiles> getMobiles(){
	   return mobilesRepo.findAll();
   }
   public List<Headphones> getHeadphones(){
	   return headphonesRepo.findAll();
   }
   public List<Watches> getWatches(){
	   return watchesRepo.findAll();
   }
   public List<Cameras> getCameras(){
	   return camerasRepo.findAll();
   }
   public List<AllProducts> getAllProducts(){
	   return allProductsRepo.findAll();
   }
   public Optional<Laptops> getLaptopById(Long id){
	   return laptopsRepo.findById(id);
   }
   public Optional<Mobiles> getMobileById(Long id){
	   return mobilesRepo.findById(id);
   }
   public Optional<Headphones> getHeadphoneById(Long id){
	   return headphonesRepo.findById(id);
   }
   public Optional<Watches> getWatchById(Long id){
	   return watchesRepo.findById(id);
   }
   public Optional<Cameras> getCameraById(Long id){
	   return camerasRepo.findById(id);
   }
   public Optional<AllProducts> getProductById(Long id){
	   return allProductsRepo.findById(id);
   }
}