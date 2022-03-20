package ankbot.json

import ankbot.reader.json.JsonReader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JsonReaderSpec
  extends AnyFlatSpec
  with Matchers {

  val jsonReader1 = new JsonReader("{a:1,b:[2,3],c:foo}")
  val jsonReader2 = new JsonReader("{a:1,b:[2,3],c:foo,d:3.0,e:{a:1,b:2,c:3},f:[{a:1,b:2},{a:3,b:4}]}")
  val jsonReader3 = new JsonReader("{a:[{a:1.0,b:2}]}")

  behavior of "JsonReader"

  it should "pass list int" in {
    assert(new JsonReader("[1,2,3]")[List[Int]] ==
      List(1, 2, 3))
  }
  it should "pass map string,int" in {
    assert(new JsonReader("{a:1,b:2,c:3}")[Map[String, Int]] ==
      Map("a" -> 1, "b" -> 2, "c" -> 3))
  }
  it should "pass map string,string" in {
    assert(new JsonReader("{a:1,b:2,c:3}")[Map[String, String]] ==
      Map("a" -> "1", "b" -> "2", "c" -> "3"))
  }
  it should "pass Foo reader1" in {
    assert(jsonReader1[Foo] ==
      Foo(a = 1, b = List(2, 3), c = "foo"))
  }
  it should "pass Foo reader2" in {
    assert(jsonReader2[Foo] ==
      Foo(a = 1, b = List(2, 3), c = "foo", d = 3.0))
  }
  it should "pass Bar reader2" in {
    assert(jsonReader2[Bar] ==
      Bar(a = 1, b = List(2, 3), c = "foo", d = 3.0, e = Map("a" -> 1, "b" -> 2, "c" -> 3)))
  }
  it should "pass Dar reader2" in {
    assert(jsonReader2[Dar] ==
      Dar(a = 1, b = List(2, 3), c = "foo", d = 3.0, e = Map("a" -> 1, "b" -> 2, "c" -> 3),
        f = List(Map("a" -> 1, "b" -> 2), Map("a" -> 3, "b" -> 4))))
  }
  it should "pass Goo reader3" in {
    assert(jsonReader3[Goo] == Goo(a = List(Map("a" -> 1.0, "b" -> 2.0))))
  }
}

case class Foo(a: Int, b: List[Int], c: String, d: Double = 0.0)
case class Bar(a: Int, b: List[Int], c: String, d: Double, e: Map[String, Int])
case class Dar(a: Int, b: List[Int], c: String, d: Double = 0.0,
  e: Map[String, Int],
  f: List[Map[String, Int]])
case class Goo(a: List[Map[String, Double]])