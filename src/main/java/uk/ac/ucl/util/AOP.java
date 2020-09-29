package uk.ac.ucl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AOP {
    @Before("execution(void uk.ac.ucl.catalina.conf.Connector.init(..))")
    public void initConnector(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        int port = (int)args[0];
        Logger logger = LogManager.getLogger("ServerXMLParsing");
        logger.info("Initializing ProtocolHandler [http-bio-{}]", port);
    }
}
