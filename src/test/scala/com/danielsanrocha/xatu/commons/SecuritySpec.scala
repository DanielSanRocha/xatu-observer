package com.danielsanrocha.xatu.commons

import com.danielsanrocha.xatu.UnitSpec

import scala.concurrent.Future

class SecuritySpec extends UnitSpec {
  describe("hash method") {
    it("should hash password") {
      Future { Security.hash("1234") should equal("gdyb21LQTcIANtvYMT7QVQ") }
    }
  }
}
