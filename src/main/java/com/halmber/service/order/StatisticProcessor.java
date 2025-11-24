package com.halmber.service.order;

import com.halmber.exception.InvalidAttributeException;
import com.halmber.model.Order;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticProcessor {
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

    private <T> void processStatisticAttr(T value, Map<String, Integer> statistics) {
        if (value instanceof String str) {
            Set<String> elements = getSetStringAttributes(str);
            elements.forEach(val -> statistics.merge(val, 1, Integer::sum));
            return;
        }
        statistics.merge(value.toString(), 1, Integer::sum);
    }

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
