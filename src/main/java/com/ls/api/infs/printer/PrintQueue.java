package com.ls.api.infs.printer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.ls.api.model.PrintJob;

@Component
public class PrintQueue {

  /**
   * Houses pending jobs to be printed in a thread safe collection.
   */
  private final ConcurrentLinkedQueue<PrintJob> queue = new ConcurrentLinkedQueue<>();

  /**
   * Utilized where {@link ConcurrentLinkedQueue} size must be known cheaply and
   * accurately under contention.
   */
  private final AtomicInteger size = new AtomicInteger(0);

  /**
   * The total amount of jobs that have been processed during the lifetime
   * of this application.
   */
  private final AtomicInteger total = new AtomicInteger(0);

  public void enqueue(PrintJob job) {

    if (job == null)
      throw new IllegalArgumentException("Cannot handle null value.");

    queue.add(job);
    size.incrementAndGet();
    total.incrementAndGet();
  }

  public PrintJob dequeue() {

    final PrintJob job = queue.poll();

    if (job != null)
      size.decrementAndGet();

    return job;
  }

  public boolean isEmpty() {
    return size.get() == 0;
  }

  public int size() {
    return size.get();
  }

  public int total() {
    return total.get();
  }
}