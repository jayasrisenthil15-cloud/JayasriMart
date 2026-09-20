-- =============================================================================
-- Migration V2: Seed Data
-- =============================================================================

INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
(1, 'System Administrator', 'admin@jayasrimart.com', '$2a$10$.aswYW4uqk.AwWY1Ukdta.HL07.kzrX1tvRTdCY4ItMdJnjuozBhC', 'ADMIN', CURRENT_TIMESTAMP),
(2, 'Apex Trends', 'seller1@jayasrimart.com', '$2a$10$/TALY77cVz82DADObDOIZ.cWf6DYKazPQ6k9j.uDqqEypqcoeXEOi', 'SELLER', CURRENT_TIMESTAMP),
(3, 'Modern Living', 'seller2@jayasrimart.com', '$2a$10$/TALY77cVz82DADObDOIZ.cWf6DYKazPQ6k9j.uDqqEypqcoeXEOi', 'SELLER', CURRENT_TIMESTAMP),
(4, 'Aarav Sharma', 'buyer1@jayasrimart.com', '$2a$10$POzg/h5HviOxVSAMUyPo.uhjsjmfE/5AkFvku/PSiLXJ43BlvQ0BS', 'BUYER', CURRENT_TIMESTAMP),
(5, 'Diya Patel', 'buyer2@jayasrimart.com', '$2a$10$POzg/h5HviOxVSAMUyPo.uhjsjmfE/5AkFvku/PSiLXJ43BlvQ0BS', 'BUYER', CURRENT_TIMESTAMP);

INSERT INTO products (id, seller_id, name, description, price, stock_qty, category, image_url, avg_rating, active, created_at) VALUES
(1, 2, 'Men''s Slim-Fit Cotton Oxford Shirt', 'Premium 100% breathable combed cotton button-down shirt ideal for casual and business attire.', 1299.00, 50, 'Fashion', 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&auto=format&fit=crop&q=80', 5.00, TRUE, CURRENT_TIMESTAMP),
(2, 2, 'Women''s Floral Summer A-Line Dress', 'Lightweight chiffon midi dress with vibrant floral print and comfortable waist tie.', 1899.00, 35, 'Fashion', 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600&auto=format&fit=crop&q=80', 4.80, TRUE, CURRENT_TIMESTAMP),
(3, 2, 'Classic Denim Trucker Jacket', 'Heavyweight washed denim jacket featuring metallic button closures and double chest flap pockets.', 2499.00, 20, 'Fashion', 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600&auto=format&fit=crop&q=80', 4.20, TRUE, CURRENT_TIMESTAMP),
(4, 2, 'Lightweight Running Sports Shoes', 'Breathable knit mesh upper with responsive EVA cushioning for daily running and training.', 1999.00, 40, 'Fashion', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80', 4.60, TRUE, CURRENT_TIMESTAMP),
(5, 3, 'Wireless Noise-Cancelling Headphones', 'Over-ear Bluetooth headphones with active noise cancellation, 40mm drivers, and 30-hour battery.', 4999.00, 25, 'Electronics', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80', 4.70, TRUE, CURRENT_TIMESTAMP),
(6, 3, 'Smart Fitness Tracker with Heart Rate Monitor', 'Sleek OLED fitness band with 24/7 heart rate monitoring, SpO2 sensor, and 50m water resistance.', 2499.00, 60, 'Electronics', 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=600&auto=format&fit=crop&q=80', 4.00, TRUE, CURRENT_TIMESTAMP),
(7, 3, '10000mAh Ultra-Compact Fast Charging Power Bank', 'Dual USB-A and Type-C Power Delivery fast charging power bank with multi-protect safety system.', 1199.00, 80, 'Electronics', 'https://images.unsplash.com/photo-1609592426508-cc02cb06e121?w=600&auto=format&fit=crop&q=80', 4.50, TRUE, CURRENT_TIMESTAMP),
(8, 3, 'Mechanical Gaming Keyboard RGB', 'Tenkeyless mechanical gaming keyboard with tactile blue switches and customizable RGB lighting.', 3299.00, 15, 'Electronics', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80', 4.90, TRUE, CURRENT_TIMESTAMP),
(9, 2, 'Ceramic Non-Stick Induction Frying Pan', 'Durable hard-anodized aluminium body with toxin-free ceramic non-stick coating and stay-cool handle.', 1499.00, 30, 'Home', 'https://images.unsplash.com/photo-1584990347449-39726207a759?w=600&auto=format&fit=crop&q=80', 4.40, TRUE, CURRENT_TIMESTAMP),
(10, 2, 'Handcrafted Wooden Bedside Table Lamp', 'Natural solid pine wood base paired with a textured beige linen shade for warm ambient lighting.', 1799.00, 18, 'Home', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600&auto=format&fit=crop&q=80', 4.60, TRUE, CURRENT_TIMESTAMP),
(11, 2, '10000 Pure Egyptian Cotton Bath Towel Set', 'Set of 4 plush 600 GSM combed Egyptian cotton bath towels offering exceptional softness and absorbency.', 999.00, 45, 'Home', 'https://images.unsplash.com/photo-1616046229478-9901c5536a45?w=600&auto=format&fit=crop&q=80', 4.70, TRUE, CURRENT_TIMESTAMP),
(12, 3, 'Vitamin C Brightening Facial Serum 30ml', 'Formulated with 15% pure ethyl ascorbic acid, hyaluronic acid, and ferulic acid for radiant skin.', 699.00, 50, 'Beauty', 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', 4.80, TRUE, CURRENT_TIMESTAMP),
(13, 3, 'Organic Cold-Pressed Argan Hair Oil 100ml', 'Pure Moroccan argan oil rich in vitamin E and essential fatty acids for deep hydration and frizz control.', 849.00, 40, 'Beauty', 'https://images.unsplash.com/photo-1608248597359-bb436ef85db4?w=600&auto=format&fit=crop&q=80', 4.50, TRUE, CURRENT_TIMESTAMP),
(14, 3, 'Hydrating Aloe Vera Gel & Daily Moisturizer', '99% organic pure aloe vera soothing gel enriched with green tea extract for all skin types.', 399.00, 90, 'Beauty', 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', 4.30, TRUE, CURRENT_TIMESTAMP),
(15, 2, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Essential reading for software engineers by Robert C. Martin, focusing on writing readable, maintainable code.', 899.00, 25, 'Books', 'https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=600&auto=format&fit=crop&q=80', 4.90, TRUE, CURRENT_TIMESTAMP),
(16, 2, 'Atomic Habits by James Clear', 'A proven framework for improving every day through tiny changes that yield remarkable long-term results.', 599.00, 70, 'Books', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80', 4.80, TRUE, CURRENT_TIMESTAMP),
(17, 2, 'Designing Data-Intensive Applications', 'Martin Kleppmann''s definitive guide to principles of reliable, scalable, and maintainable data systems.', 1499.00, 15, 'Books', 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=600&auto=format&fit=crop&q=80', 5.00, TRUE, CURRENT_TIMESTAMP),
(18, 3, 'Premium Roasted California Almonds 500g', 'Slow-roasted crunchy California whole almonds, vacuum-sealed for fresh aroma and crunch.', 649.00, 100, 'Food', 'https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=600&auto=format&fit=crop&q=80', 4.70, TRUE, CURRENT_TIMESTAMP),
(19, 3, 'Organic Darjeeling First Flush Whole Leaf Tea 250g', 'Hand-plucked orthodox black tea with delicate muscatel flavor and golden liquor.', 499.00, 60, 'Food', 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80', 4.60, TRUE, CURRENT_TIMESTAMP),
(20, 3, '100% Raw Wildflower Forest Honey 500g', 'Unfiltered, unpasteurized raw honey harvested ethically from untouched wild forest apiaries.', 379.00, 75, 'Food', 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80', 4.80, TRUE, CURRENT_TIMESTAMP),
(21, 2, 'Genuine Leather Bi-Fold Slim Wallet', 'Handcrafted top-grain leather minimalist wallet with RFID blocking layer and 8 card slots.', 899.00, 40, 'Accessories', 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&auto=format&fit=crop&q=80', 4.50, TRUE, CURRENT_TIMESTAMP),
(22, 2, 'Polarized UV400 Aviator Sunglasses', 'Classic metal frame aviators with scratch-resistant polarized lenses providing 100% UV protection.', 1299.00, 30, 'Accessories', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600&auto=format&fit=crop&q=80', 4.40, TRUE, CURRENT_TIMESTAMP),
(23, 3, 'Water-Resistant Laptop Backpack 15.6 Inch', 'Multi-compartment commuter backpack with padded laptop sleeve, USB charging port, and anti-theft pocket.', 1999.00, 35, 'Accessories', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80', 4.70, TRUE, CURRENT_TIMESTAMP),
(24, 3, 'Minimalist Stainless Steel Analog Watch', 'Japanese quartz movement timepiece with sapphire-coated glass, 3 ATM water resistance, and mesh strap.', 2999.00, 20, 'Accessories', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80', 4.60, TRUE, CURRENT_TIMESTAMP);

INSERT INTO orders (id, buyer_id, total_amount, delivery_charge, status, payment_method, name, phone, address, city, pincode, created_at) VALUES
(1, 4, 3798.00, 0.00, 'DELIVERED', 'UPI', 'Aarav Sharma', '9876543210', 'Flat 402, Green Meadows, Anna Nagar', 'Chennai', '600040', CURRENT_TIMESTAMP),
(2, 5, 1199.00, 0.00, 'SHIPPED', 'CARD', 'Diya Patel', '9876543211', '12B, Lake View Residency, R.S. Puram', 'Coimbatore', '641002', CURRENT_TIMESTAMP),
(3, 4, 1499.00, 0.00, 'CONFIRMED', 'COD', 'Aarav Sharma', '9876543210', 'Flat 402, Green Meadows, Anna Nagar', 'Chennai', '600040', CURRENT_TIMESTAMP);

INSERT INTO order_items (id, order_id, product_id, seller_id, quantity, unit_price, created_at) VALUES
(1, 1, 1, 2, 1, 1299.00, CURRENT_TIMESTAMP),
(2, 1, 6, 3, 1, 2499.00, CURRENT_TIMESTAMP),
(3, 2, 7, 3, 1, 1199.00, CURRENT_TIMESTAMP),
(4, 3, 9, 2, 1, 1499.00, CURRENT_TIMESTAMP);

INSERT INTO reviews (id, user_id, product_id, order_id, rating, comment, created_at) VALUES
(1, 4, 1, 1, 5, 'Outstanding shirt! The cotton quality is exceptional and the fit is tailored perfectly. Arrived in two days.', CURRENT_TIMESTAMP),
(2, 4, 6, 1, 4, 'Very accurate step counter and heart rate monitor. Battery lasts almost 5 days on a single charge.', CURRENT_TIMESTAMP);

ALTER TABLE users ALTER COLUMN id RESTART WITH 6;
ALTER TABLE products ALTER COLUMN id RESTART WITH 25;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 4;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 5;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 3;
ALTER TABLE cart_items ALTER COLUMN id RESTART WITH 1;
