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

package com.github.dnvriend.component.client.echoservice

import com.github.dnvriend.component.ComponentTestSpec
import com.github.dnvriend.component.client.wsclient.DefaultWsClientProxy
import com.github.dnvriend.model.Person
import play.api.test.WsTestClient

class EchoServiceClientTest extends ComponentTestSpec {

  def withEchoService(f: EchoServiceClient => Unit): Unit =
    WsTestClient.withClient(client => f(new EchoServiceClientProvider(new DefaultWsClientProxy(client)).get()))

  it should "non TLS HTTP 200 for '/get'" in withEchoService { service =>
    service.get().futureValue shouldBe 200
  }

  it should "TLS HTTP 200 for a get on '/get'" in withEchoService { service =>
    service.getTls().futureValue shouldBe 200
  }

  it should "support basic auth for non-tls" in withEchoService { service =>
    service.basicAuth("foo", "bar").futureValue shouldBe 200
  }

  it should "support basic auth for tls" in withEchoService { service =>
    service.basicAuthTls("foo", "bar").futureValue shouldBe 200
  }

  it should "support post" in withEchoService { service =>
    service.post(Person("foo", 25)).futureValue shouldBe 200
  }

  it should "support put" in withEchoService { service =>
    service.put(Person("foo", 25)).futureValue shouldBe 200
  }

  it should "support delete" in withEchoService { service =>
    service.delete().futureValue shouldBe 200
  }

  it should "support patch" in withEchoService { service =>
    service.patch(Person("foo", 25)).futureValue shouldBe 200
  }

  it should "echo marshal and unmarshal JSON" in withEchoService { service =>
    service.echo(Person("foo", 25)).futureValue shouldBe Person("foo", 25)
  }
}
