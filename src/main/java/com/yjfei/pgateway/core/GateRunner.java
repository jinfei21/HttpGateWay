package com.yjfei.pgateway.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yjfei.pgateway.common.GateException;
import com.yjfei.pgateway.common.HttpServletRequestWrapper;
import com.yjfei.pgateway.common.HttpServletResponseWrapper;
import com.yjfei.pgateway.context.RequestContext;

/**
 * This class initializes servlet requests and responses into the RequestContext
 * and wraps the FilterProcessor calls to preRoute(), route(), postRoute(), and
 * error() methods
 *
 */
public class GateRunner {

	/**
	 * Creates a new <code>GateRunner</code> instance.
	 */
	public GateRunner() {
	}

	/**
	 * sets HttpServlet request and HttpResponse
	 *
	 * @param servletRequest
	 * @param servletResponse
	 */
	public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		RequestContext.getCurrentContext().setRequest(new HttpServletRequestWrapper(servletRequest));
		RequestContext.getCurrentContext().setResponse(new HttpServletResponseWrapper(servletResponse));
	}

	/**
	 * executes "pre" filterType GateFilters
	 *
	 * @throws GateException
	 */
	public void preRoute() throws GateException {
		FilterProcessor.getInstance().preRoute();
	}

	/**
	 * executes "route" filterType GateFilters
	 *
	 * @throws GateException
	 */
	public void route() throws GateException {
		FilterProcessor.getInstance().route();
	}

	/**
	 * executes "post" filterType GateFilters
	 *
	 * @throws GateException
	 */
	public void postRoute() throws GateException {
		FilterProcessor.getInstance().postRoute();
	}

	/**
	 * executes "error" filterType GateFilters
	 */
	public void error() {
		FilterProcessor.getInstance().error();
	}

}
