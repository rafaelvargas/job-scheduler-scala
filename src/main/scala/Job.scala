import scala.concurrent.duration.FiniteDuration
import cats.effect.IO

final case class Job(
    id: String,
    duration: FiniteDuration
) {
  def run: IO[Unit] =
    // 20% chance to fail
    IO.randomUUID.map(_.hashCode % 100 < 20).flatMap {
      case true =>
        IO.sleep(duration / 2) *>
          IO.raiseError(new RuntimeException("Simulated job failure"))
      case false =>
        IO.sleep(duration)
    } *>
      IO.println(s"Job $id completed successfully")
}
