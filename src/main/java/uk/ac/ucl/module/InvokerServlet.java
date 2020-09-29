package uk.ac.ucl.module;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.catalina.request.Request;
import uk.ac.ucl.catalina.response.Response;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.core.ReflectUtil;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * InvokerServlet provides service to servlet mapping
 */
@Component
@Scope("singleton") // The default scope is singleton, here is just to indicate it
public class InvokerServlet extends HttpServlet {

    public void service(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse) {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;
        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        try {
            Class servletClass = context.getWebappClassLoader().loadClass(servletClassName);
            // No need to check if servletObject is null, this is checked in ReflectUtil
            Object servletObject = context.getServlet(servletClass);

            // The types of arguments of service() is ServletRequest and ServletResponse
            // They have to be casted to these two types to match corresponding invoke method
            ReflectUtil.invoke(servletObject,
                    "service", (ServletRequest) request, (ServletResponse) response);
            if (response.getRedirectPath() == null) {
                response.setStatus(Constant.code_200);
            }
            else{
                response.setStatus(Constant.code_302);
            }
        } catch (ClassNotFoundException | ServletException e) {
            e.printStackTrace();
        }
    }
}
