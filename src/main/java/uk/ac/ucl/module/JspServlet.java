package uk.ac.ucl.module;

import org.apache.juli.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.util.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ucl.catalina.request.Request;
import uk.ac.ucl.catalina.response.Response;
import uk.ac.ucl.classLoader.JspClassLoader;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.core.StrUtil;
import uk.ac.ucl.util.io.JspUtil;
import uk.ac.ucl.util.io.WebXMLParsing;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * JspServlet is responsible for handling the request from client for JSP.
 * Its mechanism is similiar to InvokerServlet
 */
@Component
@Scope("singleton")
public class JspServlet extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        Request request = (Request)req;
        Response response = (Response)resp;

        Context context = request.getContext();
        String uri = request.getUri();
            // If the folder directory is the root directory
            if (uri.equals("/")) {
                uri = WebXMLParsing.getWelcomeFileName(request.getContext());
            }
            String fileName = StrUtil.subAfter(uri, "/", true);
            File file = new File(context.getDocBase(), fileName);

            if (file.exists()) {

                String path = context.getPath();
                String subFolder;
                if (path.equals("/")) {
                    subFolder = "_";
                }
                else{
                    subFolder = StrUtil.subAfter(path, "/");
                }
                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (!jspServletClassFile.exists()){
                    JspUtil.compileJsp(context, file);
                }
                // If the JSP has been modified, re-compile it.
                else if (file.lastModified() > jspServletClassFile.lastModified()) {
                    JspUtil.compileJsp(context, file);
                    JspClassLoader.invaliJspClassLoader(uri, context);
                }

                String extension = FileUtils.getFileExtension(file);
                String mimeType = WebXMLParsing.getMimeType(extension);
                response.setContentType(mimeType);

                // Load the class of the servlet, and then find it from the context
                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);
                try {
                    Class jspServletClass = jspClassLoader.loadClass(jspServletClassName);
                    HttpServlet servlet = context.getServlet(jspServletClass);
                    servlet.service(request, response);
                } catch (ClassNotFoundException | ServletException | IOException e) {
                    e.printStackTrace();
                }

                if (response.getRedirectPath() == null) {
                    response.setStatus(Constant.code_200);
                }
                else{
                    response.setStatus(Constant.code_302);
                }
            } else {
                response.setStatus(Constant.code_404);
            }
        }

}
