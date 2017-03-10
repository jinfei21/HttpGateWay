package com.yjfei.pgateway.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.PropertiesInstanceConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.yjfei.pgateway.common.Constants;
import com.yjfei.pgateway.core.FilterFileManager;
import com.yjfei.pgateway.core.FilterLoader;
import com.yjfei.pgateway.core.LogConfigurator;
import com.yjfei.pgateway.filters.GateFilterDaoFactory;
import com.yjfei.pgateway.filters.GateFilterPoller;
import com.yjfei.pgateway.groovy.GroovyCompiler;
import com.yjfei.pgateway.groovy.GroovyFileFilter;
import com.yjfei.pgateway.monitor.MetricReporter;
import com.yjfei.pgateway.util.IPUtils;

public class InitializeServletListener implements ServletContextListener {

	private Logger LOGGER = LoggerFactory.getLogger(InitializeServletListener.class);
	private String appName = null;

	private LogConfigurator logConfigurator;

	public InitializeServletListener() {
		String applicationID = System.getProperty(Constants.DeploymentApplicationID);
		if (applicationID == null || applicationID.isEmpty()) {
			System.setProperty(Constants.DeploymentApplicationID, Constants.ApplicationName);
			System.setProperty(Constants.DeployEnvironment, "test");
		}
        loadConfiguration();
        configLog();
        //registerEureka();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
        try {

            initMonitor();
            initGateWay();
            startGateFilterPoller();

            ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        } catch (Exception e) {
        	LOGGER.error("Error while initializing pgateway.", e);
            //throw Throwables.propagate(e);
        }
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		FilterFileManager.shutdown();
	}

    private void initMonitor() {
    	MetricReporter.getInstance().start();
    }
	
    private void initGateWay() throws Exception {

        LOGGER.info("Starting Groovy Filter file manager");
        final AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        final String preFiltersPath = config.getString(Constants.GateFilterPrePath);
        final String postFiltersPath = config.getString(Constants.GateFilterPostPath);
        final String routeFiltersPath = config.getString(Constants.GateFilterRoutePath);
        final String errorFiltersPath = config.getString(Constants.GateFilterErrorPath);
        final String customPath = config.getString(Constants.GateFilterCustomPath);

        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());
        if (customPath == null) {
            FilterFileManager.init(5, preFiltersPath, postFiltersPath, routeFiltersPath, errorFiltersPath);
        } else {
            FilterFileManager.init(5, preFiltersPath, postFiltersPath, routeFiltersPath, errorFiltersPath, customPath);
        }
        LOGGER.info("Groovy Filter file manager started");
    }

    private void startGateFilterPoller() {
        GateFilterPoller.start(GateFilterDaoFactory.getGateFilterDao());
        LOGGER.info("GateFilterPoller Started.");
    }
    
	private void loadConfiguration() {
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");

		appName = ConfigurationManager.getDeploymentContext().getApplicationId();

		// Loading properties via archaius.
		if (null != appName) {
			try {
				LOGGER.info(String.format("Loading application properties with app id: %s and environment: %s", appName,
						ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
				ConfigurationManager.loadCascadedPropertiesFromResources(appName);
			} catch (IOException e) {
				LOGGER.error(String.format(
						"Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
						appName, ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
			}
		} else {
			LOGGER.warn(
					"Application identifier not defined, skipping application level properties loading. You must set a property 'archaius.deployment.applicationId' to be able to load application level properties.");
		}

	}

	private void configLog() {
		logConfigurator = new LogConfigurator(appName,ConfigurationManager.getDeploymentContext().getDeploymentEnvironment());
		logConfigurator.config();
	}

	private void registerEureka() {
		DynamicBooleanProperty eurekaEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("eureka.enabled",
				true);
		if (!eurekaEnabled.get())
			return;

		EurekaInstanceConfig eurekaInstanceConfig = new PropertiesInstanceConfig() {
		};

		DiscoveryManager.getInstance().initComponent(eurekaInstanceConfig, new DefaultEurekaClientConfig());

		final DynamicStringProperty serverStatus = DynamicPropertyFactory.getInstance()
				.getStringProperty("server." + IPUtils.getLocalIP() + ".status", "up");
		DiscoveryManager.getInstance().getDiscoveryClient().registerHealthCheckCallback(new HealthCheckCallback() {
			@Override
			public boolean isHealthy() {
				return serverStatus.get().toLowerCase().equals("up");
			}
		});

		String version = String.valueOf(System.currentTimeMillis());
		String group = ConfigurationManager.getConfigInstance().getString("server.group", "default");
		String dataCenter = ConfigurationManager.getConfigInstance().getString("server.data-center", "default");

		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("version", version);
		metadata.put("group", group);
		metadata.put("dataCenter", dataCenter);

		String turbineInstance = getTurbineInstance();
		if (turbineInstance != null) {
			metadata.put("turbine.instance", turbineInstance);
		}

		ApplicationInfoManager.getInstance().registerAppMetadata(metadata);
	}

	public String getTurbineInstance() {
		String instance = null;
		String ip = IPUtils.getLocalIP();
		if (ip != null) {
			instance = ip + ":" + ConfigurationManager.getConfigInstance().getString("server.internals.port", "8077");
		} else {
			LOGGER.warn("Can't build turbine instance as can't fetch the ip.");
		}
		return instance;
	}
}
