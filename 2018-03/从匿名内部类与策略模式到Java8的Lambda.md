# 从匿名内部类与策略模式到Java8的Lambda

> 说明：本文为[《Java8实战》](https://book.douban.com/subject/26772632/)第一部分(基础知识)的读书笔记。按书的思路以及自己的理解整理了```Lambda表达式```与函数式编相关的内容，而关于Java8的整体概览则没有阐述。

---
## 农民的苹果问题
本书用一个农夫不断变更关于筛选苹果的需求的例子来引出函数式编程。

苹果有重量和颜色两个属性，有个农夫最初希望筛选出库存中绿色的苹果。所以你写了一个苹果颜色筛选器，这很容易实现。只需要遍历库存列表，将符合颜色属性为绿色这个条件的苹果添加到一个新的容器就行了:
```java
private static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>(inventory.size());
    for (Apple apple : inventory) {
        if ("green".equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}
```
但是过了一周,农民又想要筛选出重量大于150克的苹果，所以你又写了一个苹果重量筛选器，这还是很容易实现。同样的只需要遍历库存列表，将符合重量属性大于150克这个条件的苹果添加到一个新的容器就行了:
```java
private static List<Apple> filterHeavyApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>(inventory.size());
    for (Apple apple : inventory) {
        if (apple.getWeight() > 150) {
            result.add(apple);
        }
    }
    return result;
}
```
我想你已经发现问题所在了,两个筛选器只有```if```的苹果属性的判断条件不同,却写了两个几乎一模一样的筛选器，代码就变得十分的啰嗦。如果这时候你还没有觉得繁琐，那当苹果的属性不只有重量和颜色，又多出了产地、销量、形状等等条件，这位不断更改需求的农民又要求你对这些不同的属性都做筛选，甚至要求你做组合筛选，便会出现无数个相似的```filter```方法，这肯定是不合理的。

---
## 行为参数化
我们来看这样一个问题，如果农夫的需要变更是从筛选```绿色```的苹果改成筛选```红色```的苹果,我们很容易想到为了不再反复得去修改```if```的颜色判断条件，我们会将颜色作为筛选器的参数在调用时动态的传入，使代码具有灵活性和扩展性，此时便能应付筛选各种不同颜色苹果的需求:
```java
private static List<Apple> filterApplesByColor(List<Apple> inventory, String color) {
    List<Apple> result = new ArrayList<>(inventory.size());
    for (Apple apple : inventory) {
        if (color.equals(apple.getColor())) {
            result.add(apple);
        }
    }
    return result;
}
```
类比过来，在苹果的颜色、产地、重量、销量的多个不同属性的筛选器中不同的仅仅是筛选器中```if```的判断条件，也就是这个判断的行为。我们如果可以将这个行为，或者说将整个判断条件当作筛选器的参数在调用时动态的传入，那我们就能和仅仅是筛选不同颜色的筛选器```filterApplesByColor```那样在代码实际运行时根据传入的判断条件做到灵活的筛选了。

---
## 策略设计模式
我们的筛选判断条件想要作为参数在筛选器的方法中传入就应该将其封装在一个类中，并将类作为参数传递到方法中去。书中提到这其实是策略设计模式相关的，因为之前没有接触过设计模式，去翻了[《大话设计模式》](https://book.douban.com/subject/2334288/)```策略模式```这一章，也比较简单。

在```《大话设计模式》```中是用商场收银时采取的不同促销方式来举例的，写的也很生动：
```
商场收银时如何促销，用打折还水返利，其实都是一种算法。用工厂来生成算法对象，这没有错，但算法本身是一种策略，最重要的是这些算法是随时都可能相互替换的，这就是变化点，而封装变化点是我们面向对象的一种很重要的思维方式。
```
这实质上和本文中提到的苹果筛选其实没什么不同。我们首先应该有一个苹果筛选```算法族```的抽象类或者接口来作为筛选器的参数进行传递，它里面应该封装了一个根据苹果某些属性(颜色是否是绿色、重量是否大于150克)来返回一个```boolean```值的```算法```:
```java
public interface AppleStrategy {
    boolean predicate(Apple apple);
}
```
此时我们只需要一个筛选器就能应付所有的筛选需求了:
```java
private static List<Apple> filterApples(List<Apple> inventory, AppleStrategy strategy) {
    List<Apple> result = new ArrayList<>(inventory.size());
    for (Apple apple : inventory) {
        if (strategy.predicate(apple)) {
            result.add(apple);
        }
    }
    return result;
}
```
```predicate(Apple apple)```方法是各种判断条件的抽象，想要向筛选器中传入不同的筛选条件只需要写一个类去实现```AppleStrategy```接口，用具体的判断条件重写```predicate(Apple apple)```方法就可以了。此处用筛选绿色苹果举例:

1. 写一个判断苹果是否为绿色的策略
```java
public class AppleGreenPredicate implements AppleStrategy {
    @Override
    public boolean predicate(Apple apple) {
        return "green".equals(apple.getColor());
    }
}
```
2. 在调用筛选器时将这个判断苹果是否为绿色的策略作为筛选器参数传入
```java
List<Apple> greenApples = filterApplesByStrategy(inventory, new AppleGreenPredicate());
```
其他判断条件也是类似的，只需要根据需求先写一个策略，看起来解决方案很不错，至少大幅度减少了重复的代码。但是值得注意的一点是，虽然我们将筛选器做了封装，减少到了一个，但是项目中却多了无数的策略类，每要实现一次筛选的需求就要创建一个实现了```AppleStrategy```接口的策略类。

---
## 匿名内部类
为了解决上述问题，你会想到既然不想出现这么多策略类，那我们用```匿名内部类```就好了嘛。我们可以借助匿名内部类同时声明并实例化一个具体的策略，改善了一个接口声明好几个实体类的问题：
```java
//筛选绿色的苹果
List<Apple> greenApples = filterApples(inventory, new AppleStrategy() {
    @Override
    public boolean predicate(Apple apple) {
        return "green".equals(apple.getColor());
    }
});

//筛选重量大于150的苹果
List<Apple> heavyApples = filterApples(inventory, new AppleStrategy() {
    @Override
    public boolean predicate(Apple apple) {
        return apple.getWeight() > 150;
    }
});
```
这样做的好处是我们终于不用再创建很多个具体的策略类、也不用根据不同的需求来写很多个筛选器就能应付多变的需求了。不好的地方是匿名内部类的代码仍然非常的啰嗦并且变得冗长不好读。我们需要在灵活性和简洁性之间找一个最佳的平衡点。

---
## Lambda表达式
好了好了，终于轮到```Lambda表达式```出场了。先来看看用Lambda表达式能怎样改写上述代码:
```java
//筛选绿色的苹果
List<Apple> greenApples = filterApples(inventory, apple -> "green".equals(apple.getColor()));

//筛选重量大于150的苹果
List<Apple> heavyApples = filterApples(inventory, apple -> apple.getWeight() > 150);
```
这是非常大的一次改进了，我们在```行为参数化```一节中提到过将判断条件作为筛选器的参数传递进去，这里你会发现```Lambda表达式```真的这样做到了，没有啰嗦的代码，不再需要一个创建一个实现了策略接口的类，除却了语法规定的格式以外，Lambda的主体部分和筛选器中```if```的判断条件一模一样。

接着我们来讲讲怎么使用```Lambda表达式```：

- Lambda表达式语法
```java
(Apple apple) -> "green".equals(apple.getColor())
```
Lambda表达式由参数、箭头和主体组成，箭头前部分为参数，后部分为主体。
1. 参数部分
```java
//和Java的函数参数格式相同，为一个逗号分隔，小括号包围的形参集合。
//可以忽略掉参数的数据类型；如果只有一个参数，你还可以忽略小括号;如果形参列表为空，则不能省略括号
apple -> "green".equals(apple.getColor())

() -> System.out.println("Hello Lambda");
```
2. 主体部分
```java
//要么是一个表达式，要么是一个由大括号包围语句块。
//如果 Lambda 表达式主体只有一条语句，那么可以忽略大括号。
apple -> {
    System.out.println(apple.getColor());
    System.out.println(apple.getWeight());
}
```
- Lambda表达式如何使用

我们知道在```Java8```之前想要作为函数的参数就必须是一个基本数据类型或者一个实例对象，而我们现在可以直接将一个```Lambda表达式```作为参数传递。但这并不是直接就可以这么干的，在使用```Lambda表达式```之前首先需要定义一个```函数式接口```，而我们的```Lambda表达式```可以看作那个```函数式接口```的一个实例，这样就能理解了，原来```Lambda表达式```仍然是一个实例对象。
```java
//函数式接口就是只有一个抽象方法的接口
//我们在策略模式中设计的算法族便是一个函数式接口
//@FunctionalInterface注解和@Override的注解类似，此处是标注了接口为一个函数式接口，如果接口定义了多个抽象方法则会在编译期报错
@FunctionalInterface
public interface AppleStrategy {
    boolean predicate(Apple apple);
}
```
定义完函数式接口后，我们先来看下```Lambda表达式```和这个```函数式接口```的关系：
```java
(Apple apple) -> "green".equals(apple.getColor())
```
这个```Lambda表达式```的主体部分是一个表达式，表达式的返回值类型和```函数式接口```中定义的抽象方法返回值类型是一致的，```Lambda表达式```的参数类型即是定义的抽象方法的参数类型。整个表达式就是这个抽象方法的具体实现。接下来我们只需要将这个接口作为筛选器的方法参数，在使用筛选器时就可以使用Lambda表达式来作为参数传入了。

---
## 函数式接口
实际上大部分情况下不需要我们自己去定义函数式接口，拿苹果筛选器中用到的这个```AppleStrategy```接口来说，```Java8```有一个抽象层次更高的接口```Predicate<T>```，它定义了一个```test(T t)```的抽象方法，接收泛型T对象，并返回一个```boolean```。在你需要表示一个设计类型T的布尔表达式时就可以使用这个接口，我们的苹果筛选器就可以改成下面这样:
```java
private static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> predicate) {
    List<Apple> result = new ArrayList<>(inventory.size());
    for (Apple apple : inventory) {
        if (predicate.test(apple)) {
            result.add(apple);
        }
    }
    return result;
}
```
除此之外，```Java API```还提供了更多的```函数式接口```，例如：
- Consumer
```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
```
Consumer<T>接口中定义了一个接收泛型对象T，没有返回值的```accept(T t)```方法。适用于想访问某个对象并对其做操作且不需要返回的场景。比如接收一个List，并需要打印输出其中的每个元素时就可以用这个接口。
```java
/**
* 使用Consumer接口打印一个List中的所有元素
* @param list 需要操作的list
* @param consumer 对list中的元素做特定操作的函数式接口对象实例，可以是一个Lambda表达式
* @param <T> list中元素的类型泛型
*/
private static <T> void forEach(List<T> list, Consumer<T> consumer) {
    for (T t : list) {
        consumer.accept(t);
    }
}

forEach(integerList, integer -> System.out.println(integer));
```
- Function
```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```
Function<T, R>接口中定义了一个接收泛型对象T，返回泛型R的```apply(T t)```方法。适用于将输入对象的信息映射到输出的场景，或者说是对输入对象进行转化。比如接收一个String列表，输出一个这个String列表中每个元素长度的Integer列表。
```java
/**
* 使用Function接口映射一个列表元素中的某些属性到另一个列表
* @param list 需要操作得list
* @param function 对list中的元素进行转换的函数式接口对象实例，可以是一个Lambda表达式
* @param <T> function接收的对象
* @param <R> function转换后的对象
* @return 存储了元素经过function转换后的list
*/
private static <T, R> List<R> map(List<T> list, Function<T, R> function) {
    List<R> result = new ArrayList<>(list.size());

    for (T t : list) {
        result.add(function.apply(t));
    }
    return result;
}

List<Integer> mapResult = map(stringList, s -> s.length());
```

除了本文列出的三个```函数式接口```以外，```Java8```还提供了更多的函数式接口，这里不再多说，菜鸟教程的[Java 8 函数式接口](http://www.runoob.com/java/java8-functional-interfaces.html)总结的```java.util.function```包下所有的函数式接口。

---
## 总结
虽然别的语言早就引入了```Lambda表达式```，而且也比```Java8```的```Lambda```更强大，但其作为```Java8```的新语法，给```Java```在语法层面上带来了革命性的改进，还是非常值得学习的，毕竟```Java10```都快出了。。

本来还想再写写```方法引用```的内容，但是写的实在太长了，也不方便阅读还是单开一篇来写```方法引用```有关的笔记吧。

最后，本文所有例子在2018-03/lambda目录下，有需要的可以clone之后查看。

(完 2018年3月11日)
