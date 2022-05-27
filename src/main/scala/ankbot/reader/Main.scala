package ankbot.reader

import com.typesafe.config.ConfigFactory

object Main extends App {
  def configFromString(s: String) = {
    new ConfigReader(ConfigFactory.parseString(s).resolve())
  }

  /*val m = new JsonReader("{a:1,b:2,c:3}")[Map[String, Int]]
  println(m)
  val l = new JsonReader("[3,4,5]")[List[Int]]
  println(l)

  println(configFromString("""{x: "234333333.111111545464546546546546546546546545411111111111111111111111123"}""").x[scala.math.BigDecimal])

  val conf: ConfigReader = configFromString("""{uuid: "123e4567-e89b-12d3-a456-556642440000"}""")
  println(conf.uuid[java.util.UUID])
*/

  new java.math.BigInteger("123456789")
  println(configFromString("""{x: "123456789"}""").x[scala.math.BigInt])
  println(configFromString("""{x: "123456789"}""").x[java.math.BigInteger])
  println(configFromString("""{x: "1234.56789"}""").x[java.math.BigDecimal])
}
