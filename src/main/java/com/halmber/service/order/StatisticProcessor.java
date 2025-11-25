package com.halmber.service.order;

import com.halmber.exception.InvalidAttributeException;
import com.halmber.model.Order;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for processing order attributes and aggregating statistics.
 * <p>
 * Handles various attributes of {@link Order} such as "status", "tags" and so on.
 */
public class StatisticProcessor {

    /**
     * Processes the specified attribute of an order and updates the statistics map.
     *
     * @param order      the order to process
     * @param statistics map to store aggregated statistics
     * @param attribute  the attribute to aggregate (e.g., "status", "city")
     * @throws InvalidAttributeException if the attribute is unknown
     */
    public void processStatistic(Order order, Map<String, Integer> statistics, String attribute) {
        switch (attribute) {
            case "id" -> processStatisticAttr(order.getCustomer().getId(), statistics);
            case "status" -> processStatisticAttr(order.getStatus(), statistics);
            case "tags" -> processStatisticAttr(order.getTags(), statistics);
            case "paymentMethod" -> processStatisticAttr(order.getPaymentMethod(), statistics);
            case "fullName" -> processStatisticAttr(order.getCustomer().getFullName(), statistics);
            case "email" -> processStatisticAttr(order.getCustomer().getEmail(), statistics);
            case "phone" -> processStatisticAttr(order.getCustomer().getPhone(), statistics);
            case "city" -> processStatisticAttr(order.getCustomer().getCity(), statistics);
            default -> throw new InvalidAttributeException(String.format("Unknown attribute: %s%n", attribute));
        }
    }

    /**
     * Processes a single attribute value and aggregates it in the statistics map.
     *
     * @param value      the attribute value
     * @param statistics map to store aggregated results
     * @param <T>        type of the attribute value
     */
    private <T> void processStatisticAttr(T value, Map<String, Integer> statistics) {
        if (value == null) {
            return;
        }
        if (value instanceof String str) {
            Set<String> elements = getSetStringAttributes(str);
            elements.forEach(val -> statistics.merge(val, 1, Integer::sum));
            return;
        }
        statistics.merge(value.toString(), 1, Integer::sum);
    }

    /**
     * Splits a string attribute into a set of individual values using separators ",", "#", "|", ";".
     *
     * @param strAttribute string attribute to split
     * @return set of individual string values
     */
    private Set<String> getSetStringAttributes(String strAttribute) {
        String regEx = "[,#|;]";
        if (strAttribute.matches(".*" + regEx + ".*")) {
            return Arrays.stream(strAttribute.split(regEx))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } else return Set.of(strAttribute);
    }
}
