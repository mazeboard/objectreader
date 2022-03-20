package ankbot.config

import ankbot.reader.config.ConfigReader
import ankbot.reader.config.ConfigReader._
import ankbot.config.ObjectTests._
import com.typesafe.config._
import org.apache.spark.SparkConf
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Try

class ConfigReaderSpec
  extends AnyFlatSpec
  with Matchers {

  // TODO add java.nio.file.Path
  // TODO add java.util.regex.Pattern
  // TODO add scala.util.matching.Regex
  // TODO scala.math.BigDecimal and java.math.BigDecimal

  behavior of "ConfigReader"

  it should "pass BigDecimal (1)" in {
    assert(configFromString("""{x: "234.123"}""").x[scala.math.BigDecimal].compareTo(scala.math.BigDecimal("234.123")) == 0)
  }
  it should "pass BigDecimal (2)" in {
    assert(configFromString("""{x: "234.123"}""").x[java.math.BigDecimal].compareTo(new java.math.BigDecimal("234.123")) == 0)
  }
  it should "pass enum" in {
    assert(configFromString("""{level: "HIGH"}""").level[Level] == Level.HIGH)
  }
  it should "pass uuid" in {
    assert(configFromString("""{uuid: "123e4567-e89b-12d3-a456-556642440000"}""").uuid[java.util.UUID] ==
      java.util.UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))
  }
  it should "pass path" in {
    assert(configFromString("""{file: "/tmp/foo.txt"}""").file[java.io.File] == new java.io.File("/tmp/foo.txt"))
  }
  it should "pass BigInt" in {
    assert(configFromString("""{x: "123456789"}""").x[scala.math.BigInt] == scala.math.BigInt(123456789))
  }
  it should "pass BigInteger" in {
    assert(configFromString("""{x: "123456789"}""").x[java.math.BigInteger] == new java.math.BigInteger("123456789"))
  }
  it should "pass Foo" in {
    assert(configFromString("{x: foo}")[Foo] == Foo(3))
  }
  it should "pass Option[Double]" in {
    assert(configFromString("{x: 1.0}").x[Option[Double]] == Some(1.0))
  }
  it should "pass foo[Double] default" in {
    assert(configFromString("{x: 12.5}").foo[Double](1.0) == 1.0)
  }
  it should "pass Point x,y" in {
    assert(configFromString("{x: 2.7, y:3.1}")[Point] == Point(2.7, 3.1))
  }
  it should "pass Double x,y" in {
    assert(configFromString("{x: 2.7, y:3.1}").x[Double] == 2.7)
  }
  it should "pass MyTestDefault" in {
    assert(configFromString("{bar: 3.4}")[MyTestDefault] ==
      MyTestDefault(3.4, 10))
  }
  it should "pass MyTestDefaultFloat" in {
    assert(configFromString("{bar: 1.5F}")[MyTestDefaultFloat] ==
      MyTestDefaultFloat(1.5F, 100))
  }
  it should "pass Bytes" in {
    assert(configFromString("{x: 1k}").x[Bytes] == 1024L)
  }
  it should "pass Point failure" in {
    assert(Try(configFromString("{x: 2.5}")[Point]).isFailure)
  }
  it should "pass list bytes" in {
    assert(configFromString("{x: [1k, 2k]}").x[List[Bytes]] ==
      List(1024L, 2048L))
  }
  it should "pass map String, Int" in {
    assert(configFromString("{foo: 1, bar: 2}")[Map[String, Int]] ==
      Map("foo" -> 1, "bar" -> 2))
  }
  it should "pass map String, Bytes" in {
    assert(configFromString("{x: {a:1k, b:2k}}").x[Map[String, Bytes]] ==
      Map("b" -> 2048L, "a" -> 1024L))
  }
  it should "pass Port" in {
    assert(configFromString("{port: 8080}").port[Port] == Port(8080))
  }
  it should "pass TestA" in {
    assert(configFromString("""{adtlist:[{bytes = "3k", maps = [{u:[1k,2k], v:[3k,4k]}]}]}""")[TestA] ==
      TestA(List(MyTestA(
        3072L.asInstanceOf[Bytes],
        Some(List(Map(
          "u" -> List(1024L.asInstanceOf[Bytes], 2048L.asInstanceOf[Bytes]),
          "v" -> List(3072L.asInstanceOf[Bytes], 4096L.asInstanceOf[Bytes]))))))))
  }
  it should "pass TestB" in {
    assert(configFromString("a: 1")[TestB] == TestB(1, None))
  }
  it should "pass MyTest" in {
    val myTest = MyTest("adta", 5, 3072L.asInstanceOf[Bytes],
      Some(List(
        1L.asInstanceOf[DurationMilliSeconds],
        1000L.asInstanceOf[DurationMilliSeconds],
        3600000L.asInstanceOf[DurationMilliSeconds])),
      100,
      Map("u" -> "foo", "v" -> "bar"), None, 2, List(15, 23))

    assert(configFromString("""{
             port = 8080
             map {
                foo = "bar"
                zoo = "dar"}
             maplistbytes {
                 foo = ["1k", "2k"]
                 bar = ["3k", "4k"]}
             sparkConf {
                 "spark.master" = "local[*]"
                 "spark.app.name" = "mySpark"}
             mySparkConf = ${sparkConf}
             adtlist = [${adtmap.adt1}]
             adtmap {
                  adt1 {
                    type = "adta"
                    b = 5
                    bytes = "3k"
                    durations = [ "1ms", "1s", "1h" ]
                    map = {u:foo, v:bar}
                    count = 2
                    counts = [15, 23]}
                 adt2 = ${adtmap.adt1}}}
              """)[Test] == Test(
      Some(Port(8080)),
      Map("zoo" -> "dar", "foo" -> "bar"),
      Map(
        "foo" -> List(1024L.asInstanceOf[Bytes], 2048L.asInstanceOf[Bytes]),
        "bar" -> List(3072L.asInstanceOf[Bytes], 4096L.asInstanceOf[Bytes])),
      List(myTest),
      Map("adt2" -> myTest, "adt1" -> myTest),
      Map("spark.master" -> "local[*]", "spark.app.name" -> "mySpark")))
  }
  it should "pass ConfMyPoint" in {
    assert(configFromString("{a = {x:12,y:14} , b = {x:3,y:5}}")[ConfMyPoint] ==
      ConfMyPoint(new MyPoint(12.0, 14.0), new MyPoint(3.0, 5.0)))
  }
  it should "pass MyClass" in {
    assert(configFromString(
      """{boolean = true
         port = 8080
         adt { b = 1 }
         list = [1, 0.2]
         map {key = [${adt}]}}""")[MyClass] ==
      MyClass(true, Port(8080), Adt(1), List(1.0, 0.2), Map("key" -> List(Adt(1))), Some("foo")))
  }
  it should "pass MyInt" in {
    assert(configFromString("{ value: 1 }")[MyInt] ==
      new MyInt(1))
  }
  it should "pass YClass x" in {
    assert(configFromString("{x: hello}")[YClass] == YClass(5, 5))
  }
  it should "pass XClass xclass" in {
    assert(configFromString("xclass: {x: hello}").xclass[XClass] ==
      XClass(6, 1))
  }
  it should "pass XClass x" in {
    assert(configFromString("{x: hello}")[XClass] == XClass(6, 1))
  }
  it should "pass ZClass" in {
    assert(configFromString("{port: { value: 8080}}")[ZClass] ==
      new ZClass(Port(8080)))
  }
  it should "pass ZClass value" in {
    assert(configFromString("{value: 8080}")[ZClass] ==
      new ZClass(Port(8080)))
  }
  it should "pass WClass" in {
    assert(configFromString("{ports: [8080, 5050]}")[WClass] ==
      new WClass(List(Port(8080), Port(5050))))
  }
  it should "pass MyPoint" in {
    assert(configFromString("{ x: 1, y:2}")[MyPoint] ==
      new MyPoint(1.0, 2.0))
  }
  it should "pass MyPort" in {
    assert(configFromString("{port:134}")[MyPort] == MyPort(Port(134)))
  }
  it should "pass map XClass, YClass" in {
    assert(configFromString("{foo: toto, hello: world}")[Map[XClass, YClass]] ==
      Map(XClass(6, 1) -> YClass(5, 5), XClass(4, 1) -> YClass(4, 5)))
  }
  it should "pass map Bird" in {
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
  }
  it should "pass spark 1" in {
    val set1 = configFromString(
      """{"spark.master": "local[*]", "spark.app.name": "test sparkconf"}""")[MySparkConf]
      .getAll
      .toSet
    val set2 = Set(("spark.master", "local[*]"), ("spark.app.name", "test sparkconf"))
    assert(set1 == set2)
  }
  it should "pass spark 2" in {
    val set1 = configFromString(
      """sparkConf: {"spark.master": "local[*]", "spark.app.name": "test sparkconf"}""")[MySparkConf]
      .getAll.toSet
    val set2 = Set(("spark.master", "local[*]"), ("spark.app.name", "test sparkconf"))
    assert(set1 == set2)
  }
  it should "pass spark 3" in {
    assert(new MyConfigReader(ConfigFactory.parseString("{spark.master: local, spark.app.name: test}"))[SparkConf].getAll.toList ==
      List(("spark.master", "local"), ("spark.app.name", "test")))
  }
  it should "pass UClass" in {
    assert(configFromString("{a:1,b:2}")[UClass[Map[String, Int]]] ==
      new UClass(Map("a" -> 1, "b" -> 2)))
  }
  it should "pass Bird" in {
    assert(configFromString("""{"Parrot": "Broccoli","Squirrel": "banana"}""".stripMargin)[UClass[Map[Animal, Food]]] ==
      new UClass(Map(
        new Bird("Parrot") -> Food("Broccoli"),
        new Monkey("Squirrel") -> Food("banana"))))
  }
  it should "pass map DClass" in {
    assert(configFromString("{z:20, y:30}")[DClass] ==
      new DClass(Map("y" -> 30, "z" -> 20)))
  }
  def configFromString(s: String) = {
    new ConfigReader(ConfigFactory.parseString(s).resolve())
  }
}
