package com.yjfei.pgateway.core;

import java.util.concurrent.Callable;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.yjfei.pgateway.common.GateException;
import com.yjfei.pgateway.context.RequestContext;

public class GateCallable implements Callable {

	private static Logger LOGGER = LoggerFactory.getLogger(GateCallable.class);

	private AsyncContext ctx;
	private GateRunner gateRunner;
	private Transaction parent;
	public GateCallable(AsyncContext asyncContext, GateRunner gateRunner,Transaction parent) {
		this.ctx = asyncContext;
		this.gateRunner = gateRunner;
		this.parent = parent;
	}

	@Override
	public Object call() throws Exception {
		Transaction tran = ((DefaultMessageProducer)Cat.getProducer()).newTransaction(parent,"GateCallable", "call");
		RequestContext.getCurrentContext().unset();
		RequestContext gateContext = RequestContext.getCurrentContext();
		try {
			service(ctx.getRequest(), ctx.getResponse(), tran);
		} catch (Throwable t) {
			LOGGER.error("GateCallable execute error.", t);
			Cat.logError(t);
		} finally {
            try {
                ctx.complete();
            } catch (Throwable t) {
                Cat.logError("AsyncContext complete error.", t);
            }
			gateContext.unset();
		
			tran.complete();
		}
		return null;
	}

	private void service(ServletRequest req, ServletResponse res,Transaction tran) {
		try {

			init((HttpServletRequest) req, (HttpServletResponse) res);

			// marks this request as having passed through the "Gate engine", as
			// opposed to servlets
			// explicitly bound in web.xml, for which requests will not have the
			// same data attached
			RequestContext.getCurrentContext().setGateEngineRan();

			try {
				preRoute(tran);
			} catch (GateException e) {
				error(e,tran);
				postRoute(tran);
				return;
			}
			try {
				route(tran);
			} catch (GateException e) {
				error(e,tran);
				postRoute(tran);
				return;
			}
			try {
				postRoute(tran);
			} catch (GateException e) {
				error(e,tran);
				return;
			}

		} catch (Throwable e) {
			error(new GateException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()),tran);
		}
	}

	/**
	 * executes "post" GateFilters
	 *
	 * @throws GateException
	 */
	private void postRoute(Transaction t) throws GateException {
		Transaction tran = ((DefaultMessageProducer)Cat.getProducer()).newTransaction(t,"GateCallable", "postRoute");
		try {
			gateRunner.postRoute();
		} finally {
			tran.complete();
		}
	}

	/**
	 * executes "route" filters
	 *
	 * @throws GateException
	 */
	private void route(Transaction t) throws GateException {
		Transaction tran = ((DefaultMessageProducer)Cat.getProducer()).newTransaction(t,"GateCallable", "route");
		try {
			gateRunner.route();
		} finally {
			tran.complete();
		}
	}

	/**
	 * executes "pre" filters
	 *
	 * @throws GateException
	 */
	private void preRoute(Transaction t) throws GateException {
		Transaction tran = ((DefaultMessageProducer)Cat.getProducer()).newTransaction(t,"GateCallable", "preRoute");
		try {
			gateRunner.preRoute();
		} finally {
			tran.complete();
		}
	}

	/**
	 * initializes request
	 *
	 * @param servletRequest
	 * @param servletResponse
	 */
	private void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		gateRunner.init(servletRequest, servletResponse);
	}

	/**
	 * sets error context info and executes "error" filters
	 *
	 * @param e
	 */
	private void error(GateException e,Transaction t) {
		Transaction tran = ((DefaultMessageProducer)Cat.getProducer()).newTransaction(t,"GateCallable", "postRoute");
		try {
			RequestContext.getCurrentContext().setThrowable(e);
			gateRunner.error();
		} finally {
			tran.complete();
		}
		Cat.logError(e);
	}

	private void statReporter(RequestContext gateContext, long start) {

		long remoteServiceCost = 0l;
		Object remoteCallCost = gateContext.get("remoteCallCost");
		if (remoteCallCost != null) {
			try {
				remoteServiceCost = Long.parseLong(remoteCallCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientCost = 0l;
		Object sendResponseCost = gateContext.get("sendResponseCost");
		if (sendResponseCost != null) {
			try {
				replyClientCost = Long.parseLong(sendResponseCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientReadCost = 0L;
		Object sendResponseReadCost = gateContext.get("sendResponseCost:read");
		if (sendResponseReadCost != null) {
			try {
				replyClientReadCost = Long.parseLong(sendResponseReadCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientWriteCost = 0L;
		Object sendResponseWriteCost = gateContext.get("sendResponseCost:write");
		if (sendResponseWriteCost != null) {
			try {
				replyClientWriteCost = Long.parseLong(sendResponseWriteCost.toString());
			} catch (Exception ignore) {
			}
		}

		String routeName = gateContext.getRouteName();
		if (routeName == null || routeName.equals("")) {
			routeName = "unknown";
			System.out.println(gateContext.getRequest().getRequestURL());
			LOGGER.warn("Unknown Route: [ {} ]", gateContext.getRequest().getRequestURL());
		}

		// StatReporter.statCost(System.currentTimeMillis() - start,
		// remoteServiceCost, replyClientCost,
		// replyClientReadCost, replyClientWriteCost, routeName);
	}
}
