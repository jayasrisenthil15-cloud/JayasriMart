package com.jayasrimart.service;

import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.ReviewDAO;
import com.jayasrimart.dto.ReviewRequestDTO;
import com.jayasrimart.exception.DuplicateResourceException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Review;
import com.jayasrimart.service.impl.ReviewServiceImpl;
import com.jayasrimart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private ProductDAO productDAO;

    private ReviewService reviewService;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_review_service;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = ReviewServiceTest.class.getClassLoader().getResourceAsStream("schema.sql");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
            String[] queries = sb.toString().split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query);
                }
            }
        }
    }

    @AfterAll
    static void tearDownAll() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewDAO, orderDAO, productDAO);
    }

    @Test
    @DisplayName("submitReview rejects invalid input parameters")
    void testSubmitReviewInvalidInputs() {
        ReviewRequestDTO validDto = new ReviewRequestDTO(1L, 10L, 5, "Great product, loved it!");

        assertThrows(ValidationException.class, () -> reviewService.submitReview(null, validDto));
        assertThrows(ValidationException.class, () -> reviewService.submitReview(-1L, validDto));
        assertThrows(ValidationException.class, () -> reviewService.submitReview(1L, null));

        ReviewRequestDTO invalidRating = new ReviewRequestDTO(1L, 10L, 6, "Great product!");
        assertThrows(ValidationException.class, () -> reviewService.submitReview(1L, invalidRating));

        ReviewRequestDTO shortComment = new ReviewRequestDTO(1L, 10L, 5, "Hi");
        assertThrows(ValidationException.class, () -> reviewService.submitReview(1L, shortComment));
    }

    @Test
    @DisplayName("submitReview throws ResourceNotFoundException when product does not exist")
    void testSubmitReviewProductNotFound() {
        ReviewRequestDTO dto = new ReviewRequestDTO(999L, 10L, 5, "Loved this product!");
        when(productDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview throws ResourceNotFoundException when order does not exist")
    void testSubmitReviewOrderNotFound() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 999L, 5, "Loved this product!");
        when(productDAO.findById(1L)).thenReturn(Optional.of(new Product()));
        when(orderDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview throws UnauthorizedException when order belongs to different user")
    void testSubmitReviewUnauthorizedOrder() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 10L, 5, "Loved this product!");
        Product p = new Product();
        p.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(5L); // Order belongs to buyer 5, but buyer 4 is trying to review

        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview throws ValidationException when order is not DELIVERED")
    void testSubmitReviewNonDeliveredOrder() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 10L, 5, "Loved this product!");
        Product p = new Product();
        p.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.CONFIRMED); // Not DELIVERED yet

        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview throws ValidationException when product was not in order")
    void testSubmitReviewProductNotInOrder() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 10L, 5, "Loved this product!");
        Product p = new Product();
        p.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.DELIVERED);

        OrderItem otherItem = new OrderItem();
        otherItem.setProductId(2L); // Different product in order
        order.setItems(List.of(otherItem));

        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview throws DuplicateResourceException when review already exists")
    void testSubmitReviewDuplicate() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 10L, 5, "Loved this product!");
        Product p = new Product();
        p.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.DELIVERED);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        order.setItems(List.of(item));

        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));
        when(reviewDAO.findByUserProductOrder(4L, 1L, 10L)).thenReturn(Optional.of(new Review()));

        assertThrows(DuplicateResourceException.class, () -> reviewService.submitReview(4L, dto));
    }

    @Test
    @DisplayName("submitReview succeeds and updates average rating in transaction")
    void testSubmitReviewSuccess() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1L, 10L, 5, "Awesome quality and fit!");
        Product p = new Product();
        p.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.DELIVERED);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        order.setItems(List.of(item));

        when(productDAO.findById(1L)).thenReturn(Optional.of(p));
        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));
        when(reviewDAO.findByUserProductOrder(4L, 1L, 10L)).thenReturn(Optional.empty());

        Review created = new Review();
        created.setId(100L);
        created.setRating(5);
        created.setComment("Awesome quality and fit!");

        when(reviewDAO.create(any(Review.class), any(Connection.class))).thenReturn(created);
        when(reviewDAO.calculateAverageRating(eq(1L), any(Connection.class))).thenReturn(new BigDecimal("5.00"));

        Review result = reviewService.submitReview(4L, dto);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(productDAO).updateAvgRating(eq(1L), eq(new BigDecimal("5.00")), any(Connection.class));
    }

    @Test
    @DisplayName("isEligibleForReview accurately checks order status and duplicate status")
    void testIsEligibleForReview() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(4L);
        order.setStatus(OrderStatus.DELIVERED);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        order.setItems(List.of(item));

        when(orderDAO.findById(10L)).thenReturn(Optional.of(order));
        when(reviewDAO.findByUserProductOrder(4L, 1L, 10L)).thenReturn(Optional.empty());

        assertTrue(reviewService.isEligibleForReview(4L, 1L, 10L));

        // When duplicate exists -> false
        when(reviewDAO.findByUserProductOrder(4L, 1L, 10L)).thenReturn(Optional.of(new Review()));
        assertFalse(reviewService.isEligibleForReview(4L, 1L, 10L));

        // Null checks
        assertFalse(reviewService.isEligibleForReview(null, 1L, 10L));
    }

    @Test
    @DisplayName("getProductReviews delegates to reviewDAO")
    void testGetProductReviews() {
        Review r = new Review();
        r.setId(1L);
        when(reviewDAO.findByProductId(5L)).thenReturn(List.of(r));

        List<Review> reviews = reviewService.getProductReviews(5L);
        assertEquals(1, reviews.size());
        verify(reviewDAO).findByProductId(5L);
    }
}
