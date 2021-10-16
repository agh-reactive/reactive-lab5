package reactive5

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.Await
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.util.ByteString
import scala.util.Success
import scala.util.Failure
import scala.concurrent.duration.Duration
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.typed.scaladsl.ActorContext
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.Materializer

object HttpClient {

  def apply(): Behavior[Any] = Behaviors.setup { context =>
    val http = Http(context.system)
    val result = http
      .singleRequest(HttpRequest(uri = "http://localhost:8080/hello"))

    context.pipeToSelf(result) {
      case Success(value) => value
      case Failure(e)     => throw e
    }

    def shutdown(context: ActorContext[_]) = {
      Await.result(http.shutdownAllConnectionPools(), Duration.Inf)
      context.system.terminate()
    }
    // for Actor Materializer
    implicit val system = context.system

    Behaviors.receiveMessage {
      case resp @ HttpResponse(StatusCodes.OK, headers, entity, _) =>
        context.log.info("Got response, ContentType: " + entity.contentType)
        context.pipeToSelf(
          entity.dataBytes
            .runFold(ByteString(""))(_ ++ _)
            .map { body =>
              "Got response, body: " + body.utf8String
            }
        )(identity)
        Behaviors.same
      case resp @ HttpResponse(code, _, _, _) =>
        context.log.info("Request failed, response code: " + code)
        resp.discardEntityBytes()
        Behaviors.same
      case Success(body: String) =>
        context.log.info(body)
        shutdown(context)
        Behaviors.same
    }
  }

}

object HttpClientMain extends App {
  val actorSystem =
    ActorSystem(HttpClient(), "http-client", ConfigFactory.empty())

  Await.ready(actorSystem.whenTerminated, Duration.Inf)
}
