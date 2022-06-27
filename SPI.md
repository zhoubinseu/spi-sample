# Service Provider Interfaces

## Introduction

### 定义

SPI是供第三方实现或扩展的API，用于框架暴露出扩展点实现组件的替换，使程序有良好的扩展性。

- Service：一组接口或类(通常为抽象类)

- Service Provider：Service的特定实现

- Service Loader：加载Service Provider

### 案例

JDBC：JDK定义了接口(Service)，数据库厂商实现这些接口提供自己的服务(Service Provider)，服务调用方通过Service Loader加载Service Provider，实现对数据库的访问。

### 优点

符合开闭原则，框架的处理逻辑只依赖抽象，具体的实现通过Service Provider实现，当系统需要支持某种服务的另一种实现时，不用修改系统本身，增加一种具体实现（服务提供者）即可，增强了系统的可扩展性。

## JDK中的SPI

### ServiceLoader

通过`ServiceLoader`类解析META-INF/service/目录下以接口全限定名命名的文件，加载给文件中指定的接口实现类。

### SPI Sample

#### 定义接口

```java
package com.spi.service;

public interface NameService {
    String getName();
}
```



#### Service Provider提供服务的具体实现

provider1：

```java
package com.spi.provider;

import com.spi.service.NameService;

public class HahaNameService implements NameService {

    @Override
    public String getName() {
        return "haha";
    }
}
```

创建一个services目录，目录中创建文件com.spi.service.NameService，文件内容为`com.spi.provider.HahaNameService`，必须是UTF-8编码。生成jar时需要将services/com.spi.service.NameService文件添加到META-INF中。

provider2：

```java
package com.ahahah.spi.provider;

import com.spi.service.NameService;

public class AhNameService implements NameService {
    @Override
    public String getName() {
        return "ahahah";
    }
}
```

创建一个services目录，目录中创建文件com.spi.service.NameService，文件内容为`com.spi.provider.AhNameService`，必须是UTF-8编码。生成jar时需要将services/com.spi.service.NameService文件添加到META-INF中。



#### 服务调用方

调用方基于接口实现代码逻辑，将具体的服务实现的jar引入到calsspath，通过`ServiceLoader`加载具体的实现

```java
package com.spi.app;

import com.spi.service.NameService;

import java.util.ServiceLoader;

public class DisplayName {
    public static void main(String[] args) {
        ServiceLoader<NameService> nameServices = ServiceLoader.load(NameService.class);
        for (NameService nameService : nameServices) {
            System.out.println(nameService.getClass()+" "+nameService.getName());
        }
    }
}
```

### JDBC使用SPI

```java
public class DriverManager {
    ...
    static {
        loadInitialDrivers();
        println("JDBC DriverManager initialized");
    }
    ...
```

```java
private static void loadInitialDrivers() {
    ...
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
        public Void run() {

            ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
            Iterator<Driver> driversIterator = loadedDrivers.iterator();

            try{
                while(driversIterator.hasNext()) {
                    driversIterator.next();
                }
            } catch(Throwable t) {
            // Do nothing
            }
            return null;
        }
    });
    ...
}
```



## Spring中的SPI

沿用Java SPI设计思想，可以不修改Spring源码实现对Spring框架的扩展开发。与JDK的SPI主要的差异在资源文件的命名和内容格式。

### SpringFactoriesLoader

解析META-INF/spring.factories文件，文件必须是`Properties`格式，其中key表示接口或抽象类的全路径名称（即SPI中的service），value是逗号分隔的实现类全路径名称列表（即SPI中的service provider）。



程序启动时会加载服务的实现

```java
public class SpringApplication {
    
    public ConfigurableApplicationContext run(String... args) {
        ...
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
		...
	}
    
    private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		ClassLoader classLoader = getClassLoader();
		// Use names and ensure unique to protect against duplicates
		Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
		List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
		AnnotationAwareOrderComparator.sort(instances);
		return instances;
	}
}

```

### Springboot中对SPI机制的使用

参考spring-boot、spring-boot-autoconfiguration等的spring.factories文件

### SPI Sample

基于Spring的SPI机制可以对Spring框架实现扩展、自定义SDK并实现自动配置、提供Spring Boot Starter……

#### Sample1：自定义一个Springboot扩展，支持新的配置文件格式

Springboot定义了接口`PropertySourceLoader`,并提供了`PropertiesPropertySourceLoader`和`YamlPropertySourceLoader`两种实现。

在spring-boot的spring.factories文件中，有以下配置，即Spring Boot默认支持的两种配置文件：

```properties
# PropertySource Loaders
org.springframework.boot.env.PropertySourceLoader=\
org.springframework.boot.env.PropertiesPropertySourceLoader,\
org.springframework.boot.env.YamlPropertySourceLoader
```



简单实现一个自定义的`PropertySourceLoader`，使程序可以解析以husky为后缀的文件

##### 接口实现

```java
package com.example;

public class HuskyPropertySourceLoader implements PropertySourceLoader {
    @Override
    public String[] getFileExtensions() {
        return new String[] {"husky"};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        Map<String, ?> properties = loadProperties(resource);
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections
                .singletonList(new OriginTrackedMapPropertySource(name, Collections.unmodifiableMap(properties), true));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, ?> loadProperties(Resource resource) throws IOException {
        String filename = resource.getFilename();
        Properties props = new Properties();
        InputStream is = resource.getInputStream();
        try {
            props.load(is);
        }
        finally {
            is.close();
        }

        return (Map) props;
    }
}
```

重写`getFileExtensions`方法指定文件后缀，重写`load`方法自定义解析文件的方法。

##### 在resources目录创建META-INF/spring.factories文件

```properties
org.springframework.boot.env.PropertySourceLoader=\
com.example.HuskyPropertySourceLoader
```

##### 使用自定义的HuskyPropertySourceLoader

将上述代码引入到Springboot应用中，在resources目录创建一个配置文件application.husky，程序可以成功获取文件中的配置值

#### Sample2：自定义一个Springboot组件，并支持自动配置

##### 实现组件功能

将0~9的阿拉伯数字转成对应汉字

```java
package com.example.service;

/**
 * @author zhoubin
 */
public class Arabic2ChineseUtil {

    public static String getChinese(int number) {
        switch (number) {
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            case 0:
                return "零";
            default:
                return "不要超过九";
        }
    }

}
```

```java
package com.example.service;

/**
 * @author zhoubin
 */
public class Arabic2ChineseService {
    public String getChinese(int number) {
        return Arabic2ChineseUtil.getChinese(number);
    }
}
```

##### 提供自动装配类

```java
package com.example.configuration;

import com.example.service.Arabic2ChineseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhoubin
 */
@Configuration
public class Arabic2ChineseConfiguration {

    @Bean
    Arabic2ChineseService arabic2ChineseService() {
        return new Arabic2ChineseService();
    }

}
```

##### 在resources目录创建META-INF/spring.factories文件

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.configuration.Arabic2ChineseConfiguration
```

##### 使用自定义的组件

将上述代码引入到Springboot应用中，由于已经实现了自动装配，可以直接注入需要的Bean

```java
package com.example;

import com.example.service.Arabic2ChineseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhoubin
 */
@RestController
public class TestController {

    @Autowired
    private Arabic2ChineseService arabic2ChineseService;

    @RequestMapping("/test/{number}")
    public String getChinese(@PathVariable("number") int number) {
        return arabic2ChineseService.getChinese(number);
    }
}
```