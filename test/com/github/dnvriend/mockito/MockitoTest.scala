/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.mockito

import com.github.dnvriend.TestSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

case class Data(retrievalDate: java.util.Date)

trait DataService {
  def findData: Data
}

trait MyService {
  def isDailyData: Boolean
}

class MyServiceImpl(dataService: DataService) extends MyService {
  override def isDailyData: Boolean = {
    println(dataService.findData)
    true
  }
}

class MockitoTest extends TestSpec with MockitoSugar {

  it should "return true if the data is from today" in {

    /**
     * You can use mocks to isolate unit tests against external dependencies.
     * For example, if your class depends on an external DataService class,
     * you can feed appropriate data to your class without instantiating a DataService object.
     *
     * Mocking is especially useful for testing the public methods of classes.
     * Mocking objects and private methods is possible, but considerably harder.
     */
    val mockDataService: DataService = mock[DataService]
    when(mockDataService.findData) thenReturn Data(new java.util.Date())

    val myService: MyService = new MyServiceImpl(mockDataService)
    myService.isDailyData shouldBe true
  }
}
