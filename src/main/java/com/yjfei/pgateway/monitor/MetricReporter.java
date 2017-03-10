package com.yjfei.pgateway.monitor;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.yjfei.pgateway.util.MetricUtils;

public class MetricReporter {
	private static Logger LOGGER = LoggerFactory.getLogger(MetricReporter.class);

	private DynamicLongProperty reportInterval = DynamicPropertyFactory.getInstance()
			.getLongProperty("gate.asyncservlet.reporter.interval", 60000);
	private DynamicStringProperty appName = DynamicPropertyFactory.getInstance()
			.getStringProperty("archaius.deployment.applicationId", "pgateway");
	private volatile boolean running = true;

	private static ConcurrentHashMap<String, AtomicReference<AvgCost>> costMap = new ConcurrentHashMap<String, AtomicReference<AvgCost>>();
	private volatile AtomicReference<ThreadPoolExecutor> poolExecutorRef;
	private volatile AtomicLong rejectedRequests ;

	private static final MetricReporter instance = new MetricReporter();
	private Thread worker = new Thread("MetricReporter") {
		public void run() {

			try {
				while (running) {
					try {
						Date date = new Date();
						reportCountStats(date);
						reportCostStats(date);
					} catch (Throwable e) {
						LOGGER.error("Encounter an error while reporting.", e);
					} finally {
						sleep(reportInterval.get());
					}
				}
			} catch (InterruptedException e) {
				LOGGER.error("Async Servlet Reporter stopped because some error.", e);
			}

		}
	};

	private long preCompletedTasks = 0;
	private long preTotalTasks = 0;
	private long preRejectTasks = 0;
	
	private MetricReporter() {

	}

	public void start() {
		if (null != worker) {
			running = true;
			worker.start();
		}
	}

	public void shutdown() {
		running = false;
	}
	
	public static MetricReporter getInstance(){
		return instance;
	}
	
	public  void setThreadPoolExecutor(AtomicReference<ThreadPoolExecutor> poolExecutorRef,AtomicLong rejectedRequests){
		this.poolExecutorRef = poolExecutorRef;
		this.rejectedRequests = rejectedRequests;
	}

	private void reportCountStats(Date date) {
		ThreadPoolExecutor p = poolExecutorRef.get();
		if (p == null)
			return;

		int activeTasks = p.getActiveCount();
		long completedTasks = p.getCompletedTaskCount();
		long totalTasks = p.getTaskCount();
		int waitingTasks = p.getQueue().size();
		int threads = p.getPoolSize();
		long rejectTasks = rejectedRequests.get();

		long completedTasksThisRound = completedTasks - preCompletedTasks;
		long totalTasksThisRound = totalTasks - preTotalTasks;
		long rejectTasksThisRound = rejectTasks - preRejectTasks;

		preCompletedTasks = completedTasks;
		preTotalTasks = totalTasks;

		String prefix = appName.get();

		MetricUtils.log(prefix + ".request.processing", activeTasks, null, date);
		MetricUtils.log(prefix + ".request.waiting", waitingTasks, null, date);
		MetricUtils.log(prefix + ".request.completed", completedTasksThisRound, null, date);
		MetricUtils.log(prefix + ".request.rejected", rejectTasksThisRound, null, date);
		MetricUtils.log(prefix + ".request.request", totalTasksThisRound, null, date);
		MetricUtils.log(prefix + ".thread-pool.size", threads, null, date);
	}

	private void reportCostStats(Date date) {

		String prefix = appName.get() + ".request.";
		String key, routeName, metricName;
		int separateIndex;

		AvgCost stats;
		for (Map.Entry<String, AtomicReference<AvgCost>> entry : costMap.entrySet()) {

			key = entry.getKey();
			stats = entry.getValue().getAndSet(new AvgCost());

			separateIndex = key.lastIndexOf(".");

			routeName = key.substring(0, separateIndex);
			metricName = prefix + key.substring(separateIndex + 1);

			Map<String, String> tags = new TreeMap<String, String>();
			tags.put("routeName", routeName);

			if (key.endsWith(".count") && !routeName.equals("gate")) {
				MetricUtils.log(metricName, stats.getCount(), tags, date);
			} else {
				MetricUtils.log(metricName, stats.getAvg(), tags, date);
				MetricUtils.log(metricName + ".min", stats.getMinCost(), tags, date);
				MetricUtils.log(metricName + ".max", stats.getMaxCost(), tags, date);
			}
		}
	}

	public static void statRouteErrorStatus(String route, String cause) {
		Cat.logEvent(route, cause);
	}

	public static void statCost(long cost, long remoteServiceCost, long replyClientCost, long replyClientReadCost,
			long replyClientWriteCost, String routeName) {
		AtomicReference<AvgCost> ref = getAvgCost(routeName + ".cost");
		ref.get().addCost(cost);

		ref = getAvgCost(routeName + ".service-cost");
		ref.get().addCost(remoteServiceCost);

		ref = getAvgCost(routeName + ".reply-client-cost");
		ref.get().addCost(replyClientCost);

		ref = getAvgCost(routeName + ".reply-client-cost-read");
		ref.get().addCost(replyClientReadCost);

		ref = getAvgCost(routeName + ".reply-client-cost-write");
		ref.get().addCost(replyClientWriteCost);

		ref = getAvgCost(routeName + ".gate-cost");
		ref.get().addCost(cost - remoteServiceCost);

		ref = getAvgCost(routeName + ".count");
		ref.get().addCost(0);
	}

	private static AtomicReference<AvgCost>  getAvgCost(String routeName) {
		AtomicReference<AvgCost> ref = costMap.get(routeName);
		if (ref == null) {
			ref = new AtomicReference<AvgCost>(new AvgCost());
			AtomicReference<AvgCost> found = costMap.putIfAbsent(routeName, ref);
			if (found != null) {
				ref = found;
			}
		}
		return ref;
	}
	static class AvgCost {
		private AtomicInteger count = new AtomicInteger(0);
		private AtomicLong totalCost = new AtomicLong(0);
		private AtomicLong minCost = new AtomicLong(0);
		private AtomicLong maxCost = new AtomicLong(0);

		public void addCost(long c) {
			count.getAndIncrement();
			totalCost.getAndAdd(c);

			while (true) {
				long min = minCost.get();
				if (min == 0 || min > c) {
					if (minCost.compareAndSet(min, c)) {
						break;
					}
				} else {
					break;
				}
			}

			while (true) {
				long max = maxCost.get();
				if (max == 0 || max < c) {
					if (maxCost.compareAndSet(max, c)) {
						break;
					}
				} else {
					break;
				}
			}
		}

		public int getCount() {
			return count.get();
		}

		public long getMinCost() {
			return minCost.get();
		}

		public long getMaxCost() {
			return maxCost.get();
		}

		public long getAvg() {
			int c = count.get();
			long t = totalCost.get();
			return c == 0 ? 0 : t / c;
		}
	}
}
