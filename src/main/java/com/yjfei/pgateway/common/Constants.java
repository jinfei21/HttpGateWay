package com.yjfei.pgateway.common;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;

public class Constants {

	public static final String ApplicationName = "pgateway";
	public static final String DeploymentApplicationID = "archaius.deployment.applicationId";
	public static final String DeployConfigUrl = "archaius.configurationSource.additionalUrls";
	public static final String DeployEnvironment = "archaius.deployment.environment";

	public static final String DataSourceClassName = "gate.data-source.class-name";
	public static final String DataSourceUrl = "gate.data-source.url";
	public static final String DataSourceUser = "gate.data-source.user";
	public static final String DataSourcePasswd = "gate.data-source.password";
	public static final String DataSourceMinPoolSize = "gate.data-source.min-pool-size";
	public static final String DataSourceMaxPoolSize = "gate.data-source.max-pool-size";
	public static final String DataSourceConnectTimeOut = "gate.data-source.connection-timeout";
	public static final String DataSourceIdleTimeOut = "gate.data-source.idle-timeout";
	public static final String DataSourceMaxLifeTime = "gate.data-source.max-lifetime";

	public static final String FilterTableName = "gate.filter.table.name";
	public static final String GateFilterDaoType = "gate.filter.dao.type";

	public static final String GateFilterAdminEnabled = "gate.filter.admin.enabled";

	public static final String GateFilterPollerEnabled = "gate.filter.poller.enabled";
	public static final String GateFilterPollerInterval = "gate.filter.poller.interval";

	public static final String GateUseActiveFilters = "gate.use.active.filters";
	public static final String GateUseCanaryFilters = "gate.use.canary.filters";

	public static final String GateFilterPrePath = "gate.filter.pre.path";
	public static final String GateFilterRoutePath = "gate.filter.route.path";
	public static final String GateFilterPostPath = "gate.filter.post.path";
	public static final String GateFilterErrorPath = "gate.filter.error.path";
	public static final String GateFilterCustomPath = "gate.filter.custom.path";
	
	
	public static final String GateServletAsyncTimeOut = "gate.servlet.async.timeout";
	public static final String GateThreadPoolCodeSize = "gate.thread-pool.core-size";
	public static final String GateThreadPoolMaxSize = "gate.thread-pool.maximum-size";
	public static final String GateThreadPoolAliveTime = "gate.thread-pool.alive-time";
	
	public static final String GateInitialStreamBufferSize = "gate.initial-stream-buffer-size";
	
	public static final String GateClientMaxConnections = "gate.client.max.connections";
	public static final String GateClientRouteMaxConnections = "gate.client.route.max.connections";
	
    private static final String DefaultGroup = "default-group";
    private static final String DefaultName = "default-name";
	

}
