package com.danielsanrocha.xatu

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.should.Matchers

abstract class UnitSpec extends AsyncFunSpec with Matchers with OneInstancePerTest
