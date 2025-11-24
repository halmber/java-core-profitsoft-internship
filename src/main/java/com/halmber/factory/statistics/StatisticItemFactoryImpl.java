package com.halmber.factory.statistics;

import com.halmber.model.statistics.StatisticItem;

import java.util.Map;

public class StatisticItemFactoryImpl implements StatisticItemFactory<StatisticItem> {
    @Override
    public StatisticItem create(Map.Entry<String, Integer> entry) {
        return new StatisticItem(entry.getKey(), entry.getValue());
    }
}
