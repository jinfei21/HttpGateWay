package com.yjfei.pgateway.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yjfei.pgateway.common.GateException;
import com.yjfei.pgateway.context.RequestContext;
import com.yjfei.pgateway.core.GateRunner;

public class SyncGateServlet extends HttpServlet {
	
	private static Logger LOGGER = LoggerFactory.getLogger(SyncGateServlet.class);
	
    private GateRunner gateRunner = new GateRunner();

    @Override
    public void service(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws javax.servlet.ServletException, java.io.IOException {
        try {


            init((HttpServletRequest) req, (HttpServletResponse) res);

            // marks this request as having passed through the "Gate engine", as opposed to servlets
            // explicitly bound in web.xml, for which requests will not have the same data attached
            RequestContext.getCurrentContext().setGateEngineRan();

            try {
                preRoute();
            } catch (GateException e) {
                error(e);
                postRoute();
                return;
            }
            try {
                route();
            } catch (GateException e) {
                error(e);
                postRoute();
                return;
            }
            try {
                postRoute();
            } catch (GateException e) {
                error(e);
                return;
            }

        } catch (Throwable e) {
            error(new GateException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()));
        }
    }

    /**
     * executes "post" GateFilters
     *
     * @throws GateException
     */
    void postRoute() throws GateException {
    	gateRunner.postRoute();
    }

    /**
     * executes "route" filters
     *
     * @throws GateException
     */
    void route() throws GateException {
    	gateRunner.route();
    }

    /**
     * executes "pre" filters
     *
     * @throws GateException
     */
    void preRoute() throws GateException {
    	gateRunner.preRoute();
    }

    /**
     * initializes request
     *
     * @param servletRequest
     * @param servletResponse
     */
    void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    	gateRunner.init(servletRequest, servletResponse);
    }

    /**
     * sets error context info and executes "error" filters
     *
     * @param e
     */
    void error(GateException e) {
        RequestContext.getCurrentContext().setThrowable(e);
        gateRunner.error();
        LOGGER.error(e.getMessage(), e);
    }	

}
