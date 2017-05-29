/**
  * Created by sajith on 5/29/17.
  */

import java.util.Date

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import spray.json.DefaultJsonProtocol

import scala.io.StdIn

final case class User(name: String, id: String)

object UserFactory {
  def randomString(length: Int) = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }

  def createUser(): User = {
    val r = new scala.util.Random(new Date().getTime)
    User(randomString(3), randomString(4))
  }
}

trait UserProtocol extends DefaultJsonProtocol {

  import spray.json._

  implicit val userFormat = jsonFormat2(User)

  val json =
    MediaType.applicationWithFixedCharset("json", HttpCharsets.`UTF-8`)

  implicit def userMarshaller: ToEntityMarshaller[User] = Marshaller.oneOf(
    Marshaller.withFixedContentType(json) { organisation =>
      HttpEntity(json, organisation.toJson.compactPrint)
    })
}

object ApiServer extends App with UserProtocol {
  implicit val system = ActorSystem("api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  def loadUsers: Stream[User] = Stream.cons(UserFactory.createUser(), {
    Thread.sleep(10)
    loadUsers
  })

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
    .withParallelMarshalling(parallelism = 10, unordered = false)

  // (fake) async database query api
  def dummyUser(id: String) = User(s"User $id", id.toString)

  def fetchUsers(): Source[User, NotUsed] = Source(loadUsers)

  val route =
    pathPrefix("users") {
      get {
        complete(fetchUsers())
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
}
