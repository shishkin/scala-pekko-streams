import java.nio.file.Paths
import org.apache.pekko
import pekko.actor.ActorSystem
import pekko.{ Done, NotUsed }
import pekko.stream.connectors.file.scaladsl.Archive
import pekko.stream.connectors.file.ArchiveMetadata
import pekko.stream._
import pekko.stream.scaladsl._
import pekko.util.ByteString
import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("Pekko")

  val source = Source.unfold(0) {
    case a if a > 3 => None
    case a => Some(a + 1, a)
  }.log(name = "original")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFinish = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Info))

  val runnableGraph = source.toMat(BroadcastHub.sink(bufferSize = 1))(Keep.right)

  val materializedSource1 = runnableGraph.run()
    .log(name = "materialized-1")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFinish = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Info))

  val materializedSource2 = runnableGraph.run()
    .log(name = "materialized-2")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFinish = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Info))

  val s1 = materializedSource1
    .map(i => ByteString((i*2).toString))
    .log(name = "s1")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFinish = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Info))

  val s2 = materializedSource2
    .map(i => ByteString((i*1000).toString))
    .log(name = "s2")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFinish = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Info))


  val filesStream = Source(List(
    (ArchiveMetadata("s1.txt"), s1),
    (ArchiveMetadata("s2.txt"), s2)
  ))


  val done = filesStream
    .via(Archive.zip())
    .runWith(FileIO.toPath(Paths.get("result.zip")))

  implicit val ec: ExecutionContext = system.dispatcher
  done.onComplete(_ => system.terminate())
}
