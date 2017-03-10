package com.yjfei.pgateway.filters;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.yjfei.pgateway.common.Constants;
import com.yjfei.pgateway.common.IGateFilterDao;

public class GateFilterDaoFactory {
    private static final DynamicStringProperty daoType = DynamicPropertyFactory.getInstance().getStringProperty(Constants.GateFilterDaoType, "jdbc");

    private GateFilterDaoFactory(){
    	
    }
    
    public static IGateFilterDao getGateFilterDao(){
    	if("jdbc".equalsIgnoreCase(daoType.get())){
    		return new JDBCGateFilterDaoBuilder().build(); 
    	}
    	return new JDBCGateFilterDaoBuilder().build();
    }
    
}
