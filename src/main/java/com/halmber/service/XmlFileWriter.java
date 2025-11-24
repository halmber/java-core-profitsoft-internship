package com.halmber.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.halmber.factory.statistics.StatisticItemFactory;
import com.halmber.factory.statistics.StatisticsWrapperFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic utility class for writing statistics data to an XML file.
 * <p>
 * This class is fully generic and works with any types of items {@code I} and
 * wrappers {@code W}, provided that corresponding {@link StatisticItemFactory} and
 * {@link StatisticsWrapperFactory} are supplied. It converts a map of statistics
 * into a list of items and wraps them before serializing to XML using Jackson.
 * </p>
 *
 * @param <W> the wrapper type that contains a collection of items
 * @param <I> the type of individual statistic items
 */
public class XmlFileWriter<W, I> {
    private final XmlMapper xmlMapper;
    private final StatisticItemFactory<I> itemFactory;
    private final StatisticsWrapperFactory<W, I> wrapperFactory;

    public XmlFileWriter(StatisticsWrapperFactory<W, I> wrapperFactory,
                         StatisticItemFactory<I> itemFactory) {
        this.xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.findAndRegisterModules();

        this.itemFactory = itemFactory;
        this.wrapperFactory = wrapperFactory;
    }

    /**
     * Writes the given statistics to the specified XML file.
     * <p>
     * Each entry in the map is converted to an item using {@link #itemFactory},
     * and all items are then wrapped using {@link #wrapperFactory}. The resulting
     * wrapper object is serialized to XML with pretty printing enabled.
     * </p>
     *
     * @param outputFile the file to write the XML content to; if it exists, it will be overwritten
     * @param statistics a map of statistics where the key is the statistic name and the value is the count
     * @throws IOException if an I/O error occurs while writing the XML file
     */
    public void writeStatistics(File outputFile, Map<String, Integer> statistics) throws IOException {
        List<I> entries = statistics.entrySet().stream()
                .map(itemFactory::create)
                .collect(Collectors.toList());

        W wrapper = wrapperFactory.create(entries);

        xmlMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, wrapper);
    }
}

