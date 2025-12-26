# Job Scheduler - Scala

This repo has the implementation of a job scheduler in Scala using the [Cats Effect](https://typelevel.org/cats-effect/) library.

## How to Run

To run the project, use the following command:

```bash
sbt run
```

## Requirements

- Scala 3.3.7
- sbt 1.11.7

## Key Design Decisions

- **Semaphore usage for concurrency control**: Using `Semaphore[IO]` provides a clean, functional way to limit concurrent jobs without manual locking or complex coordination logic.

- **Polling to obtain final job results**: The `awaitJob` method polls job status every 100ms. While simple, this approach avoids complex callback mechanisms and works well for moderate workloads.

- **Result Handle Pattern**: `submitJob` returns `IO[IO[JobStatus]]` - an immediate handle that can be awaited later. This allows callers to:
   - Submit jobs without blocking
   - Await results when needed


## What could be improved?

- **Event-Driven Completion**: Instead of polling to check job status, use a different approach to signal job completion immediately.

- **Persistence Logic**: Add job state persistence (e.g., database) to 

- **Job Priority & Queuing**: Implement priority queues so high-priority jobs can jump ahead of pending jobs.

- **Job Dependencies Logic**: Allow jobs to depend on other jobs, creating DAG-based workflows.
