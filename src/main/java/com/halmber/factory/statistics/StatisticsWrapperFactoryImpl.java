package com.halmber.factory.statistics;

import com.halmber.model.statistics.StatisticItem;
import com.halmber.model.statistics.StatisticsWrapper;

import java.util.List;

public class StatisticsWrapperFactoryImpl implements StatisticsWrapperFactory<StatisticsWrapper, StatisticItem> {
    @Override
    public StatisticsWrapper create(List<StatisticItem> items) {
        return new StatisticsWrapper(items);
    }
}