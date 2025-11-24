package com.halmber.factory.statistics;

import java.util.Map;

public interface StatisticItemFactory<I> {
    I create(Map.Entry<String, Integer> entry);
}
