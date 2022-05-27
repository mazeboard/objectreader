## object-reader (experimental)

create class instance from object

this module is used to implement config-reader and json-reader, and we are planning to
implement other readers (ie. Kafka-reader, serializer/deserializer, ...)

This work is experimental and it is work in progress.

Users of object-reader must implement at least `getValue`;

Currently the documentation is very poor, thus for first aid look at the examples config-reader and json-reader
implementation.

The error handling should be improved (any suggestions are welcome)

In the interface there is two unfortunate functions: newInstance and getReaderTypeTag; we do
not know how to get the type tag of the reader object and to create a new instance.

We probably should add getArray, getSet, and maybe other methods

##### object-reader interface

```
  class Missing(e: Throwable) extends Throwable(e)

  class InvalidObject(e: Throwable) extends Throwable(e)

  class ReaderNotFound extends Throwable("reader not found")

  class NoMatch extends Throwable("no match")

  def newInstance[T](obj: X): T with ObjectReader[X] = {
    throw new UnsupportedOperationException("newInstance is not defined")
  }

  def getReaderTypeTag: TypeTag[_] = {
    throw new UnsupportedOperationException("getReaderTypeTag is not defined")
  }

  def reader[T](obj: X)(implicit ttag: TypeTag[T]): T = throw new ReaderNotFound

  def unwrap(obj: X): AnyRef = obj.asInstanceOf[AnyRef]

  def getList(obj: X): List[X] = throw new UnsupportedOperationException("getList is not defined")

  def getMap[T](obj: X)(implicit ttag: TypeTag[T]): Map[_, X] = throw new UnsupportedOperationException("getMap is not defined")

  def getValue(name: String, obj: X): X
```

## config-reader

Like [PureConfig](https://github.com/pureconfig/pureconfig), config-reader loads Typesafe Config configurations.

Compared to PureConfig, config-reader has less boilerplate with a simple implementation based on object-reader.

##### examples

```
  abstract class Animal(val name: String) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: Animal => x.name == this.name
        case _ => false
      }
    }
  }

  object Animal {
    def apply(name: String): Animal = {
      name match {
        case _ if List("Hummingbird", "Parrot", "Heron").contains(name) => new Bird(name)
        case _ if List("Pygmy", "Vervet", "Squirrel").contains(name) => new Monkey(name)
        case _ => new UnknownAnimal(name)
      }
    }
  }

  class Bird(name: String) extends Animal(name)

  class Monkey(name: String) extends Animal(name)

  class UnknownAnimal(name: String) extends Animal(name)

    assert(configFromString(
      """{"Toto": "Anything",
        |"Hummingbird": {"food": "worms"},
        |"Parrot": "Broccoli",
        |"Squirrel": "banana"}""".stripMargin)[Map[Animal, Food]] ==
      Map(
        new UnknownAnimal("Toto") -> Food("Anything"),
        new Bird("Hummingbird") -> Food("worms"),
        new Bird("Parrot") -> Food("Broccoli"),
        new Monkey("Squirrel") -> Food("banana")))


val config = new ConfigReader(ConfigFactory.parseString("{foo:{a:1}}"))
val myObj = config.foo[MyObj]
assert(myObj == MyObj(a = 1, b = 0))

val myFoo = config[Foo]
assert(myFoo == Foo(foo = MyObj(a = 1, b = 0)))

val myBar = config[Map[Bar, MyObj]]
assert(myBar == Map(Bar("foo") -> MyObj(a = 1, b = 0)))

case class MyObj(a:Int, b:Int = 0)
case class Foo(foo:MyObj)
case class Bar(value: String)
```

To create a SparkConf there is two alternatives:

```
  val sparkConf: MySparkConf = new ConfigReader(ConfigFactory.parseString("{spark.master: local[2], spark.app.name: test}"))[MySparkConf]

  class MySparkConf(sparkConf: Map[String, String]) extends SparkConf(false) {
    assert(sparkConf.contains("spark.app.name"))
    this.setAll(sparkConf)

    override def toString(): String = {
      s"MySparkConf(${sparkConf})"
    }
  }
```

Or by extending ConfigReader and adding a new SparkConf reader

```
  class MyConfigReader(config: Config) extends ConfigReader(config) {
    override def reader[T: TypeTag](obj: Config): T = {
      (typeOf[T] match {
        case t if t =:= typeOf[SparkConf] => {
          val m = getMap[String](obj).mapValues(x => unwrap(x).toString)
          new SparkConf().setAll(m)
        }
        case _ => super.reader(obj)
      }).asInstanceOf[T]
    }
  }
  
  val sparkConf: SparkConf = new MyConfigReader(ConfigFactory.parseString("{spark.master: local[2], spark.app.name: test}"))[SparkConf]
```

## json-reader

load class instance from a json string

##### example

```
val config = new JsonReader("{a:1, b:[1,2,3]}")
assert(config[MyObj] == MyObj(a = 1, b = List(1, 2, 3)))

case class MyObj(a: Int, b: List[Int])
```


