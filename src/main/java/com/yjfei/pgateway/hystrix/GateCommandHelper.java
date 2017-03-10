package com.yjfei.pgateway.hystrix;

import com.netflix.config.DynamicPropertyFactory;

public class GateCommandHelper {
    public static int getSemaphoreMaxConcurrent(String commandGroup, String commandKey) {
        int maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".semaphore.max", 0).get();
        if (maxConcurrent == 0) {
            maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".semaphore.max", 0).get();
        }
        if (maxConcurrent == 0) {
            maxConcurrent = DynamicPropertyFactory.getInstance().getIntProperty("gate.semaphore.max.global", 100).get();
        }
        return maxConcurrent;
    }

    public static String getThreadPoolKey(String commandGroup, String commandKey) {
        String poolKey = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".thread-pool.key", null).get();
        if (poolKey == null) {
            poolKey = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".thread-pool.key", commandGroup).get();
        }
        return poolKey;
    }

    public static int getThreadTimeout(String commandGroup, String commandKey) {
        int timeout = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".thread.timeout", 0).get();
        if (timeout == 0) {
            timeout = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".thread.timeout", 0).get();
        }
        if (timeout == 0) {
            timeout = DynamicPropertyFactory.getInstance().getIntProperty("gate.thread.timeout.global", 1500).get();
        }
        return timeout;
    }

    public static boolean getCircuitBreakerEnabled(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.enabled", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.enabled", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("gate.circuit-breaker.enabled.global", "true").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static boolean getCircuitBreakerForceOpen(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.force-open", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.force-open", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("gate.circuit-breaker.force-open.global", "false").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static boolean getCircuitBreakerForceClosed(String commandGroup, String commandKey) {
        String bool = DynamicPropertyFactory.getInstance().getStringProperty(commandKey + ".circuit-breaker.force-closed", null).get();
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty(commandGroup + ".circuit-breaker.force-closed", null).get();
        }
        if (bool == null) {
            bool = DynamicPropertyFactory.getInstance().getStringProperty("gate.circuit-breaker.force-closed.global", "false").get();
        }
        return bool.toString().equalsIgnoreCase("true");
    }

    public static int getCircuitBreakerRequestThreshold(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.request-threshold", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.request-threshold", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("gate.circuit-breaker.request-threshold.global", 10).get();
        }
        return i;
    }

    public static int getCircuitBreakerErrorThreshold(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.error-percentage", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.error-percentage", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("gate.circuit-breaker.error-percentage.global", 30).get();
        }
        return i;
    }

    public static int getCircuitBreakerSleep(String commandGroup, String commandKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(commandKey + ".circuit-breaker.sleep", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty(commandGroup + ".circuit-breaker.sleep", 0).get();
        }
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("gate.circuit-breaker.sleep.global", 10000).get();
        }
        return i;
    }

    public static int getThreadPoolSize(String threadPoolKey) {
        int i = DynamicPropertyFactory.getInstance().getIntProperty(threadPoolKey + ".hystrix.thread-pool.size", 0).get();
        if (i == 0) {
            i = DynamicPropertyFactory.getInstance().getIntProperty("gate.hystrix.thread-pool.size.global", 10).get();
        }
        return i;
    }
}