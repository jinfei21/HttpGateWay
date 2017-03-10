package com.yjfei.pgateway.filters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.yjfei.pgateway.common.Constants;
import com.yjfei.pgateway.common.FilterInfo;
import com.yjfei.pgateway.common.IGateFilterDao;

public class GateFilterPoller {

	private static final Logger LOGGER = LoggerFactory.getLogger(GateFilterPoller.class);

	private Map<String, FilterInfo> runningFilters = Maps.newHashMap();
	private IGateFilterDao gateFilterDao;

	private DynamicBooleanProperty pollerEnabled = DynamicPropertyFactory.getInstance()
			.getBooleanProperty(Constants.GateFilterPollerEnabled, true);

	private DynamicLongProperty pollerInterval = DynamicPropertyFactory.getInstance()
			.getLongProperty(Constants.GateFilterPollerInterval, 30000);

	private DynamicBooleanProperty active = DynamicPropertyFactory.getInstance()
			.getBooleanProperty(Constants.GateUseActiveFilters, true);
	private DynamicBooleanProperty canary = DynamicPropertyFactory.getInstance()
			.getBooleanProperty(Constants.GateUseCanaryFilters, false);

	private DynamicStringProperty preFiltersPath = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.GateFilterPrePath, null);
	private DynamicStringProperty routeFiltersPath = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.GateFilterRoutePath, null);
	private DynamicStringProperty postFiltersPath = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.GateFilterPostPath, null);
	private DynamicStringProperty errorFiltersPath = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.GateFilterErrorPath, null);
	private DynamicStringProperty customFiltersPath = DynamicPropertyFactory.getInstance()
			.getStringProperty(Constants.GateFilterCustomPath, null);

	private static GateFilterPoller instance = null;

	private static volatile boolean running = true;

	private Thread checherThread = new Thread("GateFilterPoller") {

		public void run() {
			while (running) {
				try {
					if (!pollerEnabled.get())
						continue;

					if (canary.get()) {
						Map<String, FilterInfo> filterSet = Maps.newHashMap();

						List<FilterInfo> activeScripts = gateFilterDao.getAllActiveFilters();

						if (!activeScripts.isEmpty()) {
							for (FilterInfo filterInfo : activeScripts) {
								filterSet.put(filterInfo.getFilterId(), filterInfo);
							}
						}

						List<FilterInfo> canaryScripts = gateFilterDao.getAllCanaryFilters();
						if (!canaryScripts.isEmpty()) {
							for (FilterInfo filterInfo : canaryScripts) {
								filterSet.put(filterInfo.getFilterId(), filterInfo);
							}
						}

						for (FilterInfo filterInfo : filterSet.values()) {
							doFilterCheck(filterInfo);
						}

					} else if (active.get()) {
						List<FilterInfo> newFilters = gateFilterDao.getAllActiveFilters();
						if (newFilters.isEmpty())
							continue;
						for (FilterInfo newFilter : newFilters) {
							doFilterCheck(newFilter);
						}
					}
				} catch (Throwable t) {
					LOGGER.error("GateFilterPoller run error!", t);
				} finally {
					try {
						sleep(pollerInterval.get());
					} catch (InterruptedException e) {
						LOGGER.error("GateFilterPoller sleep error!", e);
						running = false;
					}
				}
			}
		}
	};
	
	private GateFilterPoller(IGateFilterDao gateFilterDao){
		this.gateFilterDao = gateFilterDao;
		this.checherThread.start();
	}
	
	
	public static void start(IGateFilterDao gateFilterDao){
		if(instance == null){
			synchronized(GateFilterPoller.class){
				if(instance == null){
					instance = new GateFilterPoller(gateFilterDao) ;
				}
			}
		}
	}
	
	public static GateFilterPoller getInstance(){
		return instance;
	}

	public void stop(){
		this.running = false;
	}
	private void doFilterCheck(FilterInfo newFilter) throws IOException {
		FilterInfo existFilter = runningFilters.get(newFilter.getFilterId());
		if (existFilter == null || !existFilter.equals(newFilter)) {
			LOGGER.info("adding filter to disk" + newFilter.toString());
			writeFilterToDisk(newFilter);
			runningFilters.put(newFilter.getFilterId(), newFilter);
		}
	}

	private void writeFilterToDisk(FilterInfo newFilter) throws IOException {
		String filterType = newFilter.getFilterType();

		String path = preFiltersPath.get();
		if (filterType.equals("post")) {
			path = postFiltersPath.get();
		} else if (filterType.equals("route")) {
			path = routeFiltersPath.get();
		} else if (filterType.equals("error")) {
			path = errorFiltersPath.get();
		} else if (!filterType.equals("pre") && customFiltersPath.get() != null) {
			path = customFiltersPath.get();
		}

		File f = new File(path, newFilter.getFilterName() + ".groovy");
		FileWriter file = new FileWriter(f);
		BufferedWriter out = new BufferedWriter(file);
		out.write(newFilter.getFilterCode());
		out.close();
		file.close();
		LOGGER.info("filter written " + f.getPath());
	}
}
