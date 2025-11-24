package com.halmber.service.order;

import com.halmber.exception.InvalidAttributeException;
import com.halmber.model.Customer;
import com.halmber.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class StatisticProcessorTest {
    private StatisticProcessor processor;
    private Map<String, Integer> statistics;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        processor = new StatisticProcessor();
        statistics = new ConcurrentHashMap<>();

        testOrder = Order.builder()
                .id("ord-001")
                .status("NEW")
                .tags("gift, urgent")
                .paymentMethod("card")
                .amount(499.99)
                .createdAt(1731600000L)
                .customer(Customer.builder()
                        .id("cust-101")
                        .fullName("Bohdan Rohoz")
                        .email("bohdan@example.com")
                        .phone("+380501112233")
                        .city("Lviv")
                        .build())
                .build();
    }

    @Test
    void testProcessStatistic_ByCustomerId() {
        processor.processStatistic(testOrder, statistics, "id");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("cust-101"));
    }

    @Test
    void testProcessStatistic_ByStatus() {
        processor.processStatistic(testOrder, statistics, "status");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("NEW"));
    }

    @Test
    void testProcessStatistic_ByPaymentMethod() {
        processor.processStatistic(testOrder, statistics, "paymentMethod");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("card"));
    }

    @Test
    void testProcessStatistic_ByFullName() {
        processor.processStatistic(testOrder, statistics, "fullName");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("Bohdan Rohoz"));
    }

    @Test
    void testProcessStatistic_ByEmail() {
        processor.processStatistic(testOrder, statistics, "email");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("bohdan@example.com"));
    }

    @Test
    void testProcessStatistic_ByPhone() {
        processor.processStatistic(testOrder, statistics, "phone");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("+380501112233"));
    }

    @Test
    void testProcessStatistic_ByCity() {
        processor.processStatistic(testOrder, statistics, "city");

        assertEquals(1, statistics.size());
        assertEquals(1, statistics.get("Lviv"));
    }

    @Test
    void testProcessStatistic_TagsSplitByComma() {
        processor.processStatistic(testOrder, statistics, "tags");

        assertEquals(2, statistics.size());
        assertEquals(1, statistics.get("gift"));
        assertEquals(1, statistics.get("urgent"));
    }

    @Test
    void testProcessStatistic_TagsWithMultipleSeparators() {
        testOrder.setTags("gift, urgent | newCustomer");
        processor.processStatistic(testOrder, statistics, "tags");

        assertEquals(3, statistics.size());
        assertEquals(1, statistics.get("gift"));
        assertEquals(1, statistics.get("urgent"));
        assertEquals(1, statistics.get("newCustomer"));
    }

    @Test
    void testProcessStatistic_TagsWithSemicolon() {
        testOrder.setTags("gift; urgent; promo");
        processor.processStatistic(testOrder, statistics, "tags");

        assertEquals(3, statistics.size());
        assertEquals(1, statistics.get("gift"));
        assertEquals(1, statistics.get("urgent"));
        assertEquals(1, statistics.get("promo"));
    }

    @Test
    void testProcessStatistic_MultipleOrdersSameAttribute() {
        processor.processStatistic(testOrder, statistics, "status");
        processor.processStatistic(testOrder, statistics, "status");
        processor.processStatistic(testOrder, statistics, "status");

        assertEquals(1, statistics.size());
        assertEquals(3, statistics.get("NEW"));
    }

    @Test
    void testProcessStatistic_InvalidAttribute_ThrowsException() {
        InvalidAttributeException exception = assertThrows(
                InvalidAttributeException.class,
                () -> processor.processStatistic(testOrder, statistics, "invalidAttr")
        );

        assertTrue(exception.getMessage().contains("Unknown attribute"));
        assertTrue(exception.getMessage().contains("invalidAttr"));
    }

    @Test
    void testProcessStatistic_NullValue_IgnoresEntry() {
        testOrder.getCustomer().setCity(null);
        processor.processStatistic(testOrder, statistics, "city");

        assertEquals(0, statistics.size());
    }

    @Test
    void testProcessStatistic_EmptyTagsString_IgnoresEmpty() {
        testOrder.setTags("gift,  , urgent");
        processor.processStatistic(testOrder, statistics, "tags");

        assertEquals(2, statistics.size());
        assertTrue(statistics.containsKey("gift"));
        assertTrue(statistics.containsKey("urgent"));
    }

    @Test
    void testProcessStatistic_DifferentCustomers() {
        processor.processStatistic(testOrder, statistics, "id");

        Order order2 = Order.builder()
                .customer(Customer.builder().id("cust-102").build())
                .build();
        processor.processStatistic(order2, statistics, "id");

        assertEquals(2, statistics.size());
        assertEquals(1, statistics.get("cust-101"));
        assertEquals(1, statistics.get("cust-102"));
    }
}
