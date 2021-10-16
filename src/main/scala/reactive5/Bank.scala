package reactive5

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.StatusReply
import akka.Done
import com.typesafe.config.ConfigFactory
import reactive5.Client.Init
import scala.util.Failure
import scala.util.Success
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.receptionist.Receptionist
import akka.cluster.typed.Cluster
import akka.actor.typed.pubsub.Topic.Subscribe

object BankAccount {
  // potentially you can register multiple actors under one ServiceKey
  val BankAccountServiceKey = ServiceKey[Command]("accountService")
  trait Command {
    def replyTo: ActorRef[Response]
  }
  case class Deposit(amount: BigInt, replyTo: ActorRef[Response])
      extends Command {
    require(amount > 0)
  }
  case class Withdraw(amount: BigInt, replyTo: ActorRef[Response])
      extends Command {
    require(amount > 0)
  }

  trait Response
  case object Done extends Response
  case object Failed extends Response

  def apply(balance: BigInt): Behavior[Command] = Behaviors.setup { context =>
    context.system.receptionist ! Receptionist.register(
      BankAccountServiceKey,
      context.self
    )
    Behaviors.receiveMessage {
      case Deposit(amount, replyTo) =>
        replyTo ! Done
        apply(balance + amount)
      case Withdraw(amount, replyTo) if amount <= balance =>
        replyTo ! Done
        apply(balance - amount)
      case c: Command =>
        c.replyTo ! Failed
        Behaviors.same
    }
  }
}

object Client {
  trait Command
  case class Init(replyTo: ActorRef[StatusReply[Done]]) extends Command
  private case class BankAccountResponse(response: BankAccount.Response)
      extends Command
  private case class ListingResponse(listing: Receptionist.Listing)
      extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info(s"Starting up ${context.self}")
    val listingResponseAdapter =
      context.messageAdapter[Receptionist.Listing](ListingResponse)
    Behaviors.receiveMessage { case Init(replyTo) =>
      // find registered BankAccount actor
      context.system.receptionist ! Receptionist.Find(
        BankAccount.BankAccountServiceKey,
        listingResponseAdapter
      )
      context.log.info("Received init")
      findingBankActor(replyTo)
    }
  }

  def findingBankActor(
      replyTo: ActorRef[StatusReply[Done]]
  ): Behavior[Command] = Behaviors.setup { context =>
    val bankAccountResponseAdapter =
      context.messageAdapter[BankAccount.Response](BankAccountResponse)
    Behaviors.receiveMessage {
      case ListingResponse(
            BankAccount.BankAccountServiceKey.Listing(listings)
          ) =>
        listings.foreach( // should be only one such actor
          _ ! BankAccount.Deposit(200, bankAccountResponseAdapter)
        )
        context.log.info(s"BankActor was found $listings")
        awaitForBankAccountResponse(replyTo)
    }
  }

  def awaitForBankAccountResponse(
      replyTo: ActorRef[StatusReply[Done]]
  ): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case BankAccountResponse(BankAccount.Done) =>
        context.log.info(s"Received the success from BankAccount")
        replyTo ! StatusReply.Ack
        apply()
      case BankAccountResponse(BankAccount.Failed) =>
        context.log.info(s"Received the failure from BankAccount")
        replyTo ! StatusReply.error("Bank operation has failed")
        apply()
    }
  }
}

/** Small demonstration of Akka recepcionist patter based on Akka Cluster
  * see: https://doc.akka.io/docs/akka/current/typed/actor-discovery.html
  */
object BankApp extends App {
  val config = ConfigFactory.load()
  val remoteBankAccount =
    ActorSystem(
      BankAccount(0),
      "Reactive5",
      config.getConfig("serverapp").withFallback(config)
    )

  // can be spawned on separate node
  val remoteClient = ActorSystem(
    Client(),
    "Reactive5",
    config.getConfig("clientapp").withFallback(config)
  )

  import akka.actor.typed.scaladsl.AskPattern._

  Thread.sleep(10000) // wait for the cluster to form itself

  remoteClient
    .askWithStatus[Done](Init)(10.seconds, remoteClient.scheduler)
    .onComplete {
      case Failure(exception) => exception.printStackTrace(); println("Failure")
      case Success(value)     => println("Success")
    }(scala.concurrent.ExecutionContext.global)

  Await.result(remoteBankAccount.whenTerminated, Duration.Inf)
}
