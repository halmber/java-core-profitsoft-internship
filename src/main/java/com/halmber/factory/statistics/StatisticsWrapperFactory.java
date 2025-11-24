package com.halmber.factory.statistics;

import java.util.List;

public interface StatisticsWrapperFactory<W, I> {
    W create(List<I> items);
}