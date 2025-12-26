import cats.effect.{FiberIO, IO, Ref}
import cats.effect.std.{Console, Semaphore}
import cats.syntax.all._
import scala.concurrent.duration._

enum JobStatus {
  case Pending
  case Running
  case Succeeded
  case Failed
  case Cancelled
}

class JobExecutor private (
    semaphore: Semaphore[IO],
    jobFibers: Ref[IO, Map[String, FiberIO[Unit]]],
    jobStatuses: Ref[IO, Map[String, JobStatus]]
) {
  private def updateStatus(jobId: String, status: JobStatus): IO[Unit] =
    jobStatuses.update(_ + (jobId -> status))

  def submitJob(job: Job): IO[IO[JobStatus]] = {
    val jobExecution =
      updateStatus(job.id, JobStatus.Pending) *>
        Console[IO].println(s"Job ${job.id} queued") *>
        semaphore.permit
          .use { _ =>
            updateStatus(job.id, JobStatus.Running) *>
              Console[IO].println(s"Job ${job.id} started") *>
              job.run *>
              updateStatus(job.id, JobStatus.Succeeded)
          }
          .handleErrorWith { error =>
            updateStatus(job.id, JobStatus.Failed) *>
              Console[IO].println(s"Job ${job.id} failed: ${error.getMessage}")
          }
          .guarantee(jobFibers.update(_ - job.id))

    jobExecution.start.flatMap(fiber =>
      jobFibers.update(_ + (job.id -> fiber)).as(awaitJob(job.id))
    )
  }

  def cancelJob(jobId: String): IO[Unit] =
    (checkJobStatus(jobId), jobFibers.get).flatMapN {
      case (Some(status), fibers) if fibers.contains(jobId) =>
        val fiber = fibers(jobId)
        val message = status match {
          case JobStatus.Running => s"Job $jobId was cancelled while running"
          case JobStatus.Pending => s"Job $jobId was cancelled before running"
          case _                 => s"Job $jobId cancellation requested"
        }
        updateStatus(jobId, JobStatus.Cancelled) *>
          jobFibers.update(_ - jobId) *>
          fiber.cancel *>
          Console[IO].println(message)
      case _ =>
        Console[IO].println(s"Job $jobId not found or already completed")
    }

  def checkJobStatus(jobId: String): IO[Option[JobStatus]] =
    jobStatuses.get.map(_.get(jobId))

  private def awaitJob(jobId: String): IO[JobStatus] =
    checkJobStatus(jobId).flatMap {
      case Some(
            status @ (JobStatus.Succeeded | JobStatus.Failed |
            JobStatus.Cancelled)
          ) =>
        IO.pure(status)
      case Some(_) =>
        IO.sleep(100.millis) >> awaitJob(jobId)
      case None =>
        IO.raiseError(new RuntimeException(s"Job $jobId not found"))
    }
}

object JobExecutor {
  def apply(maxConcurrentJobs: Int): IO[JobExecutor] =
    for {
      semaphore <- Semaphore[IO](maxConcurrentJobs.toLong)
      jobFibers <- Ref.of[IO, Map[String, FiberIO[Unit]]](Map.empty)
      jobStatuses <- Ref.of[IO, Map[String, JobStatus]](Map.empty)
    } yield new JobExecutor(semaphore, jobFibers, jobStatuses)
}
