# Order Statistics Processor

A multithreaded Java program for processing JSON files with Orders and generating statistics in XML format.

## 📋 Contents

- [Description](#Description)
- [Basic entities](#Basic-entities)
- [File examples](#File-examples)
- [Available attributes](#Available-attributes)
- [Installation and Run](#Installation-and-Run)
- [Project architecture](#Project-architecture)

## Description

The program processes a set of JSON files with orders, collects statistics for the selected attribute, and generates an
XML report,
sorted from largest to smallest.

### Features:

- ✅ Multithreaded file processing
- ✅ Support for 8 attributes for statistics
- ✅ Separation of tags by delimiters (`,` `|` `;` `#`)
- ✅ Sorting results in descending order
- ✅ Thread-safe processing with ConcurrentHashMap
- ✅ Input data validation

## Basic entities

### 1. **Order**

The main business entity of the system.

```java
public class Order {
    private String id;
    private Customer customer;
    private String status;
    private String tags;
    private String paymentMethod;
    private double amount;
    private long createdAt;
}
```

### 2. **Customer**

Contains the customer's data.

```java
public class Customer {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String city;
}
```

### 3. **StatisticItem**

Represents a single statistics record.

```java
public class StatisticItem {
    private String value;    // Attribute value (e.g., “Lviv”)
    private Integer count;   // Number of occurrences
}
```

### 4. **StatisticsWrapper**

The root element of an XML document.

```java

@JacksonXmlRootElement(localName = "statistics")
public class StatisticsWrapper {
    @JacksonXmlElementWrapper(localName = "items")
    @JacksonXmlProperty(localName = "item")
    private List<StatisticItem> items;  // List of all statistical records
}
```

### 5. **ApplicationConfig**

Saves the program settings.

```java
public class ApplicationConfig {
    private String inputDirectory;
    private String outputDirectory;
    private String attribute;
    private int threadPoolSize;
}
```

## File examples

### Input file (orders.json)

```json
[
  {
    "id": "ord-001",
    "customer": {
      "id": "cust-101",
      "fullName": "Vasyl Cotop",
      "email": "vasyl@example.com",
      "phone": "+380501112233",
      "city": "Lviv"
    },
    "status": "NEW",
    "tags": "gift, urgent",
    "paymentMethod": "card",
    "amount": 499.99,
    "createdAt": 1731600000
  },
  {
    "id": "ord-002",
    "customer": {
      "id": "cust-102",
      "fullName": "Petro Poroh",
      "email": "petro@example.com",
      "phone": "+380501112233",
      "city": "Kyiv"
    },
    "status": "DONE",
    "tags": "gift",
    "paymentMethod": "card",
    "amount": 499.99,
    "createdAt": 1731600000
  },
  {
    "id": "ord-003",
    "customer": {
      "id": "cust-103",
      "fullName": "Zelya Boba",
      "email": "zelya@example.com",
      "phone": "+380501112233",
      "city": "Kyiv"
    },
    "status": "NEW",
    "tags": "gift, urgent, newCustomer",
    "paymentMethod": "card",
    "amount": 499.99,
    "createdAt": 1731600000
  }
]
```

### Output file (statistics_by_city.xml)

```xml
<?xml version='1.0' encoding='UTF-8'?>
<statistics>
    <items>
        <item>
            <value>Kyiv</value>
            <count>2</count>
        </item>
        <item>
            <value>Lviv</value>
            <count>1</count>
        </item>
    </items>
</statistics>
```

### Example of tag statistics (statistics_by_tags.xml)

```xml
<?xml version='1.0' encoding='UTF-8'?>
<statistics>
    <items>
        <item>
            <value>gift</value>
            <count>3</count>
        </item>
        <item>
            <value>urgent</value>
            <count>2</count>
        </item>
        <item>
            <value>newCustomer</value>
            <count>1</count>
        </item>
    </items>
</statistics>
```

### Example of status statistics (statistics_by_status.xml)

```xml
<?xml version='1.0' encoding='UTF-8'?>
<statistics>
    <items>
        <item>
            <value>NEW</value>
            <count>2</count>
        </item>
        <item>
            <value>DONE</value>
            <count>1</count>
        </item>
    </items>
</statistics>
```

## Available attributes

| Attribute       | Desc              | Example             |
|-----------------|-------------------|---------------------|
| `id`            | Customer ID       | cust-101            |
| `status`        | Order status      | NEW, DONE, CANCELED |
| `tags`          | Tags (separated)  | gift, urgent, promo |
| `paymentMethod` | Payment method    | card, cash, PayPal  |
| `fullName`      | Customer fullName | Vasyl Cotop         |
| `email`         | Customer email    | vasyl@example.com   |
| `phone`         | Customer phone    | +380501112233       |
| `city`          | Customer city     | Lviv                |

## Installation and Run

### Requirements

- JDK 21+
- Maven 3.8+

### Project build

```bash
mvn clean package
```

### Run

```bash
java -jar target/order-statistics-1.0-SNAPSHOT.jar
```

### Interactive configuration

After launching the project, follow the instructions in the console.

```
====== Order Statistics Configuration ======

Available attributes: id | status | tags | paymentMethod | fullName | email | phone | city | 
Enter input directory path (default: src/main/resources/): 
Enter attribute name (default example: id): city
Enter threads pool size (default: 8): 8

Configuration set:
  Input directory: src/main/resources/
  Attribute: city
  Output directory: src/main/resources/outputFiles
  Threads count: 8

====== END Order Statistics Configuration END ======
```

## Project architecture

### Main components

```
com.halmber
├── config
│   ├── ApplicationConfig         # Config
│   └── ConsoleInputHandler       # Console Input Handler
├── exception
│   └── InvalidAttributeException # Exception for Invalid Attributes
├── factory
│   └── statistics
│       ├── StatisticItemFactory          # Factory for StatisticItem
│       └── StatisticsWrapperFactory      # Factory for StatisticsWrapper
├── model
│   ├── Customer                  # Customer Model
│   ├── Order                     # Order Model
│   └── statistics
│       ├── StatisticItem         # Statistic Item
│       └── StatisticsWrapper     # XML Statistics Wrapper
└── service
    ├── FileService               # File Management Service
    ├── JsonFileReader            # JSON Reading
    ├── XmlFileWriter             # XML sorted Writing
    └── order
        ├── ProcessingService     # Multi-threaded Processing 
        ├── StatisticProcessor    # Statistic Processing
        └── StatisticsService     # Orchestrator

```

### Data flow

```
Input JSON Files
       ↓
FileService (list files)
       ↓
ProcessingService (ExecutorService with N threads)
       ↓
JsonFileReader (parse JSON)
       ↓
StatisticProcessor (aggregate statistics)
       ↓
ConcurrentHashMap (thread-safe storage)
       ↓
XmlFileWriter (sort + serialize)
       ↓
Output XML File
```

## Testing

```bash
mvn test
```

## License

MIT License

## Author

halmber