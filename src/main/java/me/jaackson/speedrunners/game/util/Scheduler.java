package me.jaackson.speedrunners.game.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Automatically queues tasks into the server executor from a {@link ScheduledExecutorService}.</p>
 *
 * @author Ocelot
 */
public class Scheduler implements ScheduledExecutorService
{
	private final AtomicInteger schedulerCount;
	private final Executor serverExecutor;
	private final ScheduledExecutorService service;

	public Scheduler(Executor gameExecutor)
	{
		this.schedulerCount = new AtomicInteger();
		this.serverExecutor = gameExecutor;
		this.service = Executors.newScheduledThreadPool(2, task -> new Thread(task, "Speedrunners Scheduler " + this.schedulerCount.getAndIncrement()));
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
	{
		return this.service.schedule(() -> this.serverExecutor.execute(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
	{
		return this.service.schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
	{
		return this.service.scheduleAtFixedRate(() -> this.serverExecutor.execute(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
	{
		return this.service.scheduleWithFixedDelay(() -> this.serverExecutor.execute(command), initialDelay, delay, unit);
	}

	@Override
	public void shutdown()
	{
		this.service.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		return this.service.shutdownNow();
	}

	@Override
	public boolean isShutdown()
	{
		return this.service.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return this.service.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return this.service.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task)
	{
		return this.service.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result)
	{
		return this.service.submit(() -> this.serverExecutor.execute(task), result);
	}

	@Override
	public Future<?> submit(Runnable task)
	{
		return this.service.submit(() -> this.serverExecutor.execute(task));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
	{
		return this.service.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
	{
		return this.service.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
	{
		return this.service.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		return this.service.invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command)
	{
		this.service.execute(() -> this.execute(command));
	}
}