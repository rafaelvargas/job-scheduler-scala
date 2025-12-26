import scala.concurrent.duration._
import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      executor <- JobExecutor(maxConcurrentJobs = 2)
      job1 = Job("job-1", 5.seconds)
      job2 = Job("job-2", 8.seconds)
      job3 = Job("job-3", 10.seconds)
      job4 = Job("job-4", 12.seconds)

      result1 <- executor.submitJob(job1)
      result2 <- executor.submitJob(job2)
      result3 <- executor.submitJob(job3)
      result4 <- executor.submitJob(job4)

      status3 <- executor.checkJobStatus("job-3")
      _ <- IO.println(s"Job-3 status: $status3")

      _ <- executor.cancelJob("job-3")

      _ <- result1.flatMap(status => IO.println(s"Job-1 final status: $status"))
      _ <- result2.flatMap(status => IO.println(s"Job-2 final status: $status"))
      _ <- result3.flatMap(status => IO.println(s"Job-3 final status: $status"))
      _ <- result4.flatMap(status => IO.println(s"Job-4 final status: $status"))

      _ <- IO.println("All jobs processed.")
    } yield ()
}
