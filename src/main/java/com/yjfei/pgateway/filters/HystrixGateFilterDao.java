package com.yjfei.pgateway.filters;

import java.util.List;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.yjfei.pgateway.common.FilterInfo;
import com.yjfei.pgateway.common.IGateFilterDao;

public class HystrixGateFilterDao implements IGateFilterDao{
	
	private IGateFilterDao delegation;

	public HystrixGateFilterDao(IGateFilterDao delegation){
		this.delegation = delegation;
	}
	
	@Override
	public List<String> getAllFilterIds() throws Exception {
        return new GetAllFilterIdsCommand().execute();
	}

	@Override
	public List<FilterInfo> getGateFilters(String filterId) throws Exception {
        return new GetGateFiltersCommand(filterId).execute();
	}

	@Override
	public FilterInfo getFilter(String filterId, int revision) throws Exception {
        return new GetFilterCommand(filterId, revision).execute();
	}

	@Override
	public FilterInfo getLatestFilter(String filterId) throws Exception {
        return new GetLatestFilterCommand(filterId).execute();
	}

	@Override
	public FilterInfo getActiveFilter(String filterId) throws Exception {
        return new GetActiveFilterCommand(filterId).execute();
	}

	@Override
	public List<FilterInfo> getAllCanaryFilters() throws Exception {
        return new GetAllCanaryFiltersCommand().execute();
	}

	@Override
	public List<FilterInfo> getAllActiveFilters() throws Exception {
        return new GetAllActiveFiltersCommand().execute();
	}

	@Override
	public FilterInfo canaryFilter(String filterId, int revision) throws Exception {
        return new CanaryFilterCommand(filterId, revision).execute();
	}

	@Override
	public FilterInfo activateFilter(String filterId, int revision) throws Exception {
        return new ActivateFilterCommand(filterId, revision).execute();
	}

	@Override
	public FilterInfo deactivateFilter(String filterId, int revision) throws Exception {
        return new DeactivateFilterCommand(filterId, revision).execute();
	}

	@Override
	public FilterInfo addFilter(String filterCode, String filterType, String filterName,
			String filterDisablePropertyName, String filterOrder) throws Exception {
        return new AddFilterCommand(filterCode, filterType, filterName, filterDisablePropertyName, filterOrder).execute();
	}

    @Override
    public String getFilterIdsRaw(String index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getFilterIdsIndex(String index) {
        throw new UnsupportedOperationException();
    }

    public abstract class AbstractCommand<R> extends HystrixCommand<R> {
        protected AbstractCommand() {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("pgateway"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                                    .withExecutionIsolationSemaphoreMaxConcurrentRequests(5))
            );
        }
    }
    
    public class GetAllFilterIdsCommand extends AbstractCommand<List<String>> {
        @Override
        protected List<String> run() throws Exception {
            return delegation.getAllFilterIds();
        }
    }
    
    private class GetGateFiltersCommand extends AbstractCommand<List<FilterInfo>> {
        private String filterId;

        public GetGateFiltersCommand(String filterId) {
            this.filterId = filterId;
        }

        @Override
        protected List<FilterInfo> run() throws Exception {
            return delegation.getGateFilters(filterId);
        }
    }
    
    private class GetFilterCommand extends AbstractCommand<FilterInfo> {
        private final String filterId;
        private final int revision;

        public GetFilterCommand(String filterId, int revision) {
            this.filterId = filterId;
            this.revision = revision;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.getFilter(filterId, revision);
        }
    }
    
    private class GetLatestFilterCommand extends AbstractCommand<FilterInfo> {
        private String filterId;

        public GetLatestFilterCommand(String filterId) {
            this.filterId = filterId;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.getLatestFilter(filterId);
        }
    }
    
    private class GetActiveFilterCommand extends AbstractCommand<FilterInfo> {
        private String filterId;

        public GetActiveFilterCommand(String filterId) {
            this.filterId = filterId;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.getActiveFilter(filterId);
        }
    }
    
    private class GetAllCanaryFiltersCommand extends AbstractCommand<List<FilterInfo>>  {
        @Override
        protected List<FilterInfo> run() throws Exception {
            return delegation.getAllCanaryFilters();
        }
    }
    
    private class GetAllActiveFiltersCommand extends AbstractCommand<List<FilterInfo>> {
        @Override
        protected List<FilterInfo> run() throws Exception {
            return delegation.getAllActiveFilters();
        }
    }
    
    private class CanaryFilterCommand extends AbstractCommand<FilterInfo>{
        private final String filterId;
        private final int revision;

        public CanaryFilterCommand(String filterId, int revision) {
            this.filterId = filterId;
            this.revision = revision;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.canaryFilter(filterId, revision);
        }
    }
    
    private class ActivateFilterCommand extends AbstractCommand<FilterInfo>{
        private final String filterId;
        private final int revision;

        public ActivateFilterCommand(String filterId, int revision) {
            this.filterId = filterId;
            this.revision = revision;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.activateFilter(filterId, revision);
        }
    }

    private class DeactivateFilterCommand extends AbstractCommand<FilterInfo> {
        private final String filterId;
        private final int revision;

        public DeactivateFilterCommand(String filterId, int revision) {
            this.filterId = filterId;
            this.revision = revision;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.deactivateFilter(filterId, revision);
        }
    }
    
    private class AddFilterCommand extends AbstractCommand<FilterInfo> {
        private final String filterCode;
        private final String filterType;
        private final String filterName;
        private final String filterDisablePropertyName;
        private final String filterOrder;

        public AddFilterCommand(String filterCode, String filterType, String filterName, String filterDisablePropertyName, String filterOrder) {
            this.filterCode = filterCode;
            this.filterType = filterType;
            this.filterName = filterName;
            this.filterDisablePropertyName = filterDisablePropertyName;
            this.filterOrder = filterOrder;
        }

        @Override
        protected FilterInfo run() throws Exception {
            return delegation.addFilter(filterCode, filterType, filterName, filterDisablePropertyName, filterOrder);
        }
    }
    
}
