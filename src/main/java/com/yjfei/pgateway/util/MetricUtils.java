package com.yjfei.pgateway.util;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetricUtils.class);
	
	public static void log(String metricName, long value, Map<String, String> tags, Date date) {
//		try {
//			Metrics metrics = MetricsFactory.buildMetrics(metricName);
//			metrics.add(value, tags);
//		} catch (MetricsException e) {
//			LOGGER.error("log metric error!", e);
//		}
	}
	
	public static void log(String metricName, float value, Map<String, String> tags, Date date) {
//		try {
//			Metrics metrics = MetricsFactory.buildMetrics(metricName);
//			metrics.add(value, tags);
//		} catch (MetricsException e) {
//			LOGGER.error("log metric error!", e);
//		}
	}

}
