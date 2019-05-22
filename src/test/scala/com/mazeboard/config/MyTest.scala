package com.mazeboard.config

import org.scalatest.{ FlatSpec, Matchers }

class MyTest extends FlatSpec with Matchers {
  "test" should "run my test" in {
    val test = new MyTest2()
    test.execute()
  }
  class MyTest2 extends FlatSpec with Matchers {
    "test" should "run my test" in {
      "MyTest2 ok" shouldBe "MyTest2"
    }
  }
}

