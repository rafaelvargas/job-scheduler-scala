import scala.concurrent.duration._
import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      executor <- JobExecutor(maxConcurrentJobs = 2)
      job1 = Job("job-1", 10.seconds)
      job2 = Job("job-2", 5.seconds)
      job3 = Job("job-3", 7.seconds)
      job4 = Job("job-4", 3.seconds)

      _ <- executor.submitJob(job1)
      _ <- executor.submitJob(job2)
      _ <- executor.submitJob(job3)
      _ <- executor.submitJob(job4)

      status3 <- executor.checkJobStatus("job-3")
      _ <- IO.println(s"Job-3 status: $status3")

      _ <- executor.cancelJob("job-3")

      finalStatus1 <- executor.awaitJob("job-1")
      _ <- IO.println(s"Job-1 final status: $finalStatus1")

      finalStatus2 <- executor.awaitJob("job-2")
      _ <- IO.println(s"Job-2 final status: $finalStatus2")

      finalStatus3 <- executor.awaitJob("job-3")
      _ <- IO.println(s"Job-3 final status: $finalStatus3")

      finalStatus4 <- executor.awaitJob("job-4")
      _ <- IO.println(s"Job-4 final status: $finalStatus4")
    } yield ()
}
