# reactive-lab5 - Handling failures, Akka Actor Discovery with Akka Cluster and Akka HTTP

## Handling actor errors

* to gracefully handle actor failures Akka uses _Supervision and Monitoring_ [patterns](https://doc.akka.io//docs/akka/current/general/supervision.html) 
* [Fault Tolerance in Akka](https://doc.akka.io//docs/akka/current/typed/fault-tolerance.html)
* **Note** there were significant differences introduced in Akka Typed version regarding error handling. You can read more [here](https://akka.io/blog/article/2019/02/05/typed-supervision)

## Sending messages to actors on a different machine

* [Akka Location Transparency](https://doc.akka.io//docs/akka/current/general/remoting.html#remoting) 
* [Akka Actor Discovery](https://doc.akka.io/docs/akka/current/typed/actor-discovery.html) can be implemented with [receptionist pattern](https://doc.akka.io/docs/akka/current/typed/actor-discovery.html#receptionist). Check [Bank.scala](src/main/scala/reactive5/Bank.scala) to see the example.
* **Note** In Akka, there is no way to contact actors on a different machine with actors' paths (support was dropped as it is usually a bad idea to contact actors directly via the path). You can communicate between machines transparently using Akka Cluster capabilities.
* Cluster settings can be found in [application.conf](src/main/resources/application.conf)

## Supporting the HTTP communication with Akka HTTP

* For HTTP communication (both server and client) the [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html?language=scala) library can be used.
* Take a look and try to run [HttpServer](src/main/scala/reactive5/HttpServer.scala) and then [HttpClient](src/main/scala/reactive5/HttpClient.scala)
* Server uses high level DSL to compose HTTP route, [see](https://doc.akka.io/docs/akka-http/current/routing-dsl/index.html?language=scala#minimal-example)
* Client is an actor which makes a request and then pipes the HTTP request result to itself


## Homework

The template for Lab 5: https://github.com/agh-reactive/reactive-scala-labs-templates/tree/lab-5 
* **be sure that your local lab-5 branch is up to date with remote one**
* **remember about merging solution from lab-4 into this branch**