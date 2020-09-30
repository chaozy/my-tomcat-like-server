package uk.ac.ucl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ucl.context.Context;

@Component
@Aspect
public class AOP {
    private Logger serviceLogger = LogManager.getLogger(Service.class);
    private Logger contextLogger = LogManager.getLogger(Context.class);

    @Before("execution(void uk.ac.ucl.catalina.conf.Connector.init(..))")
    public void initConnector(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        int port = (int)args[0];
        serviceLogger.info("Initializing ProtocolHandler [http-bio-{}]", port);
    }
    @After("execution(void uk.ac.ucl.catalina.conf.Connector.start(..))")
    public void startConnector(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        int port = (int)args[0];
        serviceLogger.info("Start ProtocolHandler [http-bio-{}]", port);
    }

    @After("execution(void uk.ac.ucl.context.Context.init(..))")
    public void finishDeployingContext(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String docBase = (String)args[0];
        long interval = (long)args[1];

        contextLogger.info("Deployment of web application directory {}" +
                " has finished at {} ms", docBase, interval);
    }

}
