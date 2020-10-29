# 从Spring与Mybatis整合看Spring的两个扩展点

## 一、mybatis核心原理

我们都知道，实际开发中我们只需要写Mapper接口，并不需要提供对应的实现类，那为什么能直接注入并运行？要解答这个问题，我们从不与spring整合的mybatis下手,这样能方便我们研究源码。首先我们来写一个demo

```java
public class WithoutSpringRunner {

    public static void main(String[] args) {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:file:./testDB", "root", "root");

        TransactionFactory transactionFactory =
                new JdbcTransactionFactory();
        Environment environment =
                new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(UserMapper.class);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            //这里能够执行findAll说明userMapper是一个实例，那一定是在getMapper中发生了实例化
            userMapper.findAll().forEach(System.out::println);
        }
    }
}
```

从上面的例子的`getMapper()`方法中点进去，一直往里跟，最后会走到`org.apache.ibatis.binding.MapperRegistry#getMapper`这个方法。

```java
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```

这里可以看到我们的UserMapper是从`knownMappers`这个map里拿出来的，那必然是有地方放进去的，发现是通过`configuration.addMapper(UserMapper.class);`这行代码将UserMapper放到knownMappers中。这里实际调用的是`org.apache.ibatis.binding.MapperRegistry#addMapper`。

```java
  public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {
      if (hasMapper(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        knownMappers.put(type, new MapperProxyFactory<>(type));
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
```

可以看到mybatis为我们的mapper封装了一个`MapperProxyFactory`对象，我们的mapper接口信息最终存在了这个对象的`mapperInterface`属性中，在`getMapper()`时，通过jdk动态代理生成代理对象，这个代理对象里面完成了对JDBC的封装，执行了真正的数据库操作。

## 二、如何和spring整合

从上文可以知道，mybatis会为每个mapper生成一个代理对象，那么问题来了，如果要和spring整合，怎么才能把这个代理对象交给spring管理？有一种方式是通过@Bean注解将生成的代理对象交给spring。

```java
@org.springframework.context.annotation.Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        return JdbcConnectionPool.create("jdbc:h2:file:./testDB", "root", "root");
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        return factoryBean.getObject();
    }

    @Bean
    public UserMapper userMapperFactory() throws Exception {
        MapperFactoryBean<UserMapper> userMapperFactory = new MapperFactoryBean<>(UserMapper.class);
        userMapperFactory.setSqlSessionFactory(sqlSessionFactory());
        Configuration configuration = sqlSessionFactory().getConfiguration();
        configuration.addMapper(UserMapper.class);
        return userMapperFactory.getObject();
    }
    
    /*
    @Bean
    public XXXMapper xxxMapperFactory() throws Exception {
        MapperFactoryBean<XXXMapper> xxxMapperFactory = new MapperFactoryBean<>(XXXMapper.class);
        xxxMapperFactory.setSqlSessionFactory(sqlSessionFactory());
        Configuration configuration = sqlSessionFactory().getConfiguration();
        configuration.addMapper(XXXMapper.class);
        return xxxMapperFactory.getObject();
    }
     */
}
```

到此我们就完成了mybatis和spring的整合，只是还存在一点缺陷，这样做的问题就是，以后又来了一个XXXMapper、YYYMapper的时候都需要写一遍。

## 三、@MapperScan原理分析

为了解决第二章中出现的问题，避免每加一个mapper接口都要用@Bean去注册一波，mybatis-spring提供了@MapperScan的注解来解决这个问题。
> 注意: 其实也可以通过org.apache.ibatis.session.Configuration#addMappers(java.lang.String)的API来解决这个问题，但本文其实想说的是Spring的扩展点，所以这里就不讨论addMappers的实现方案了。

只需要在spring的配置类上加`@MapperScan`的注解就可以完成所有mapper的扫描，并将mapper对应的代理对象加到spring环境中去。

```java
@Configuration
@MapperScan("com.github.oneone1995.mybatis.mapper")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        return JdbcConnectionPool.create("jdbc:h2:file:./testDB", "root", "root");
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        return factoryBean.getObject();
    }
}
```

点进去@MapperScan这个注解。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MapperScannerRegistrar.class)
@Repeatable(MapperScans.class)
public @interface MapperScan {
    //....省略
}
```

可以看到这个注解上加了@Import(MapperScannerRegistrar.class)，这里可以理解成将`MapperScannerRegistrar`这个类导入到当前配置类，即让`MapperScannerRegistrar`也生效。再来看`MapperScannerRegistrar`的代码。

```java
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
            .fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
        if (mapperScanAttrs != null) {
        registerBeanDefinitions(importingClassMetadata, mapperScanAttrs, registry,generateBaseBeanName(importingClassMetadata, 0));
    }
  }

   void registerBeanDefinitions(AnnotationMetadata annoMeta, AnnotationAttributes annoAttrs,
      BeanDefinitionRegistry registry, String beanName) {

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
    
    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

  }
}
```

### ImportBeanDefinitionRegistra扩展点

首先`MapperScannerRegistrar`实现了`ImportBeanDefinitionRegistrar`接口，复写了`registerBeanDefinitions()`方法。这个方法是干什么的呢？

这里简单说一下spring bean初始化的流程，简单地说spring会根据特定的规则去扫描包路径，并将符合规则的类转化成对应的`BeanDefinition`对象，放在`BeanDefinition Map`中，后续遍历这个map来实例化。而`ImportBeanDefinitionRegistrar`接口则是提供了一个供程序员手动注册`BeanDefinition`到`BeanDefinition Map`中的扩展点。这里多啰嗦一句，你直接写这么一个类，spring自然是不会执行你这个类里的方法的。。所以这里还需要把这个类通过@Import注解和配置类整合在一起，这样的话spring在做包扫描处理配置类(@Configuration)的时候会将配置类上使用@Import导入的`ImportBeanDefinitionRegistrar`的子类收集到一个map中，并执行这个子类的Aware方法(这里不懂的话就直接跳过这句话好了，在2.0.5这个版本里是没有任何真实的Aware方法的)，最终会遍历这个map并执行`ImportBeanDefinitionRegistrar`的方法。

在mybatis-spring的这个例子中，便是执行MapperScannerRegistrar的重写方法，最终目的是将`MapperScannerConfigurer`转换成BeanDefinition并放到bd map中。这里跟我们想的不太一样，按正常人的思维，这里只要遍历来自注解中配置的mapper路径，将mapper接口对应的代理对象都注册成BeanDefinition就可以了。但是mybatis-spring这里仅仅是将`MapperScannerConfigurer`这个类给放到bd map中。

> 其实在mybatis-spring 较老版本中的确就是按我们的想法去完成mapper扫描的。本文的版本为2.0.5。据我所知2.0.0的版本就是按上述在`registerBeanDefinitions()`方法中直接扫描Mapper的方式。

> 插曲: 别的框架也利用了这个扩展点来做实现自己的组件被spring加载。例如dubbo的@DubboScan

### BeanDefinitionRegistryPostProcessor扩展点

说到这里，我们仍然没说到mybatis-spring是如何把Mapper交给spring管理的，在`ImportBeanDefinitionRegistra`扩展点中被放到`BeanDefinition Map`中的`MapperScannerConfigurer`到底是什么，他到底干了什么事?我们这里继续点开源码。

```java
public class MapperScannerConfigurer
    implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // left intentionally blank
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0.2
   */
  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    if (this.processPropertyPlaceHolders) {
      processPropertyPlaceHolders();
    }

    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    scanner.setAddToConfig(this.addToConfig);
    scanner.setAnnotationClass(this.annotationClass);
    scanner.setMarkerInterface(this.markerInterface);
    scanner.setSqlSessionFactory(this.sqlSessionFactory);
    scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
    scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
    scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
    scanner.setResourceLoader(this.applicationContext);
    scanner.setBeanNameGenerator(this.nameGenerator);
    scanner.setMapperFactoryBeanClass(this.mapperFactoryBeanClass);
    if (StringUtils.hasText(lazyInitialization)) {
      scanner.setLazyInitialization(Boolean.valueOf(lazyInitialization));
    }
    scanner.registerFilters();
    scanner.scan(
        StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
  }

  /*
   * BeanDefinitionRegistries are called early in application startup, before BeanFactoryPostProcessors. This means that
   * PropertyResourceConfigurers will not have been loaded and any property substitution of this class' properties will
   * fail. To avoid this, find any PropertyResourceConfigurers defined in the context and run them on this class' bean
   * definition. Then update the values.
   */
  private void processPropertyPlaceHolders() {
    Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class,
        false, false);

    //如果是AnnotationConfigApplicationContext(我们的例子就是)，这里是不会进这个if条件的
    //他方法里的beanFactory也是在初始化spring context的时候就已经存在了。
    if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
      BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
          .getBeanDefinition(beanName);

      // PropertyResourceConfigurer does not expose any methods to explicitly perform
      // property placeholder substitution. Instead, create a BeanFactory that just
      // contains this mapper scanner and post process the factory.
      DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
      factory.registerBeanDefinition(beanName, mapperScannerBean);

      for (PropertyResourceConfigurer prc : prcs.values()) {
        prc.postProcessBeanFactory(factory);
      }

      PropertyValues values = mapperScannerBean.getPropertyValues();

      this.basePackage = updatePropertyValue("basePackage", values);
      this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
      this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
      this.lazyInitialization = updatePropertyValue("lazyInitialization", values);
    }
    this.basePackage = Optional.ofNullable(this.basePackage).map(getEnvironment()::resolvePlaceholders).orElse(null);
    this.sqlSessionFactoryBeanName = Optional.ofNullable(this.sqlSessionFactoryBeanName)
        .map(getEnvironment()::resolvePlaceholders).orElse(null);
    this.sqlSessionTemplateBeanName = Optional.ofNullable(this.sqlSessionTemplateBeanName)
        .map(getEnvironment()::resolvePlaceholders).orElse(null);
    this.lazyInitialization = Optional.ofNullable(this.lazyInitialization).map(getEnvironment()::resolvePlaceholders)
        .orElse(null);
  }
}

```

这个`MapperScannerConfigurer`又利用了spring的另一个重要扩展点`BeanDefinitionRegistryPostProcessor`。

这个扩展点接口继承自`BeanFactoryPostProcessor`，在`BeanFactoryPostProcessor`的基础上新增了`postProcessBeanDefinitionRegistry()`方法。Spring完成bean扫描功能的类(`ConfigurationClassPostProcessor`)就是实现了这个接口，并在该方法内部实现了bean扫描的逻辑。也包括了上文中提到的处理@Import之类等等逻辑。这个扩展点的执行机制特别早，spring内部通过代码控制了`BeanDefinitionRegistryPostProcessor`优先于`BeanFactoryPostProcessor`执行，且spring自身提供的`BeanDefinitionRegistryPostProcessor`扩展优先级最高。这里仔细想一下就很好理解，因为总是要先完成Bean的扫描，才能对扫描出来的Bean做其他的处理。

> 之后再专门写一篇spring容器初始化的文章来专门解释`ConfigurationClassPostProcessor`这个类

我们以mybatis提供的`MapperScannerConfigurer`如何被执行为例，从源码角度来看一下`BeanDefinitionRegistryPostProcessor`扩展点的具体的加载时机与工作流程。

1. 容器初始化，走到refresh()方法，调用`invokeBeanFactoryPostProcessors()`方法来执行Bean工厂的后置处理器。spring初始化容器都是通过`BeanFactory的后置处理器`这个概念来完成的，包括Bean的扫描、对Bean完成CGlib代理。所谓的后置处理器就是干预`BeanFactory`的工作流程。

    ```java
        public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
                //...省略无关代码...
                // Invoke factory processors registered as beans in the context.
                //这行代码最终会调到 PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors()方法
                invokeBeanFactoryPostProcessors(beanFactory);
                //...省略无关代码...
            }
        }
    ```

2. invokeBeanFactoryPostProcessors()细节,这里直接看我写的注释吧。

    ```java
    	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

        //这里在当前项目下一定会进这个if，beanFactory是DefaultListableBeanFactory
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

            //这里不会进，beanFactoryPostProcessors如果没有手动add的话必然是空的
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}
            //临时保存了正在执行的后置处理器
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
            //第一次执行BeanDefinitionRegistryPostProcessors扩展点
            //这里一定是只有一个的，也就是ConfigurationClassPostProcessor
            //关于这个类又是什么时候被放进去的呢。。。就不在本文讨论了
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
            //执行完ConfigurationClassPostProcessor的扩展方法，实际上就是完成Bean扫描，包括我们上面的分析
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
            //注意这里把正在执行的处理器集合给clear了
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
            //第二次执行BeanDefinitionRegistryPostProcessor扩展点
            //因为ConfigurationClassPostProcessor完成了Bean扫描
            //所以容器中(准确说应该是bd map)有可能又有了新的实现了BeanDefinitionRegistryPostProcessor扩展点的类

            //mybatis-spring实现扩展点的类MapperScannerConfigurer在这时已经被扫描出来放在了bd map中
            //所以执行完这行代码后，postProcessorNames中应该包括了MapperScannerConfigurer了
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
            //但是这里有一个判断，实现的扩展点必须还要实现Ordered接口才会执行，所以这里还是不会执行
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
            //最后一次执行BeanDefinitionRegistryPostProcessor扩展点
            //这里面没有任何要求，所以我们的MapperScannerConfigurer自然也在这里被执行
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}
        //....省略了一些无关代码.......
        //....省略了一些无关代码.......
        //....省略了一些无关代码.......
	}
    ```
3. 代码跳回`MapperScannerConfigurer`类，借助`ClassPathMapperScanner`完成了扫描，将指定package下的Mapper交给spring管理。

自此整个过程分析完毕。

> 我目前看主流框架源码中，只有mybatis-spring实现了这个扩展点来实现Mapper扫描的功能。可能作者是为了炫技吧。。不过这种方式来实现扫描的话确实更符合spring的设计初衷。因为spring


## 四、总结

本文以mybatis与spring的整合作为切入点，从源码级别分析了mybatis-spring的原理，从而学习了spring的ImportBeanDefinitionRegistra与BeanDefinitionRegistryPostProcessor两个扩展点。项目源码在mybatis-spring-demo中。