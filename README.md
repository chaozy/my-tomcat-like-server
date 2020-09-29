# DIYTomcat

DIYTomcat is a self-learning, educative java project that establishes a simple http server. It follows the basic design of the Apache Tomcat and implements parts of its components, such as the servlet container Catalina, JSP translator and compiler Jasper. It was definitely a personal project that I used to learn the structure of a web server, the principles of http protocol and the techniques in java programming. 

**UPDATE**

I have reconstructed the project with Spring in order to builder stronger code. Both `IOC` and `AOP`  are applied into the work. The structure still remains a little bit of unclear, since it is my first complicated project. I will keep developing it in the future.

## Learning Material

Most of the knowledge comes from [the official website of Apache Tomcat](http://tomcat.apache.org/). It clearly explains the structure of the Tomcat, which provides the undelying principle and other useful informations. 

The chinese book [Tomcat架构解析](https://book.douban.com/subject/27034717/) provided me much more details about the implementation of Tomcat, such as the classloading of the web application, handling servlet and jsp by DefaultServlet and JspServlet respectively.  Actually, this project starts with the overall framwork given in this book:

![FRMEWORK](https://github.com/chaozy/projects/blob/master/java/TomcatDIY/Framework.jpeg)

[Core Java, Volume II--Advanced Features](https://www.pearson.com/us/higher-education/program/Horstmann-Core-Java-Volume-II-Advanced-Features-11th-Edition/PGM2019648.html#:~:text=Core%20Java%2C%20Vol.,applications%20with%20thoroughly%20tested%20examples.https://www.pearson.com/us/higher-education/program/Horstmann-Core-Java-Volume-II-Advanced-Features-11th-Edition/PGM2019648.html#:~:text=Core Java%2C Vol.,applications with thoroughly tested examples.). has helped me a lot in gainning the basic knowledge in XML handling, web programming in java and reflection.

Besides these three materials, there are a lot of other online resources that help me finishing this project, espcially stackoverflow😊

## USAGE

The project follows the launching structure of Apache Tomcat. Executing the `startup.sh` in the root folder launches the server on three ports `18080, 18081, 18082`(configured in `/lib/server.xml`)

 `startup.sh` contains the shell script to either pack and compile two initial java classes together, which will load the rest classes in. 

## STRUCTURE

- `/lib` contains the project-required jars, since the java libraries in this project is handled by maven dependency, read `pom.xml` for more details.
- `/logs` contains the log files named by the dates
- `/conf` contains three basic configuration files: `context.xml`, `server.xml`, `web.xml`
- `notes` concludes the knowledge I gained from     this project
- `TODO` describes some features or components that I haven't finished yet
- `/src/main` contains the source code
- `/work` contains the translated JSP code and their class files

## TOOLS

- `junit` used for testing
- `log4j2` used for logging, configuration file log4j2.xml is in /src/main/resources 
- `maven` used for project management
- `jsoup` used for parsing .xml files
- `jspc` from Apache Tomcat used to handle JSP

## FLAWS😢

There are some advanecd techniques in Apache Tomcat that I either failed to complete them or jsut simplified the function. Here are a few examples:

1. Apache Tomcat uses `digester`component to handle `.xml` files, while I use `jsoup`to simply parse `.xml` into a document get what I want. 
2. Apache Tomcat utilizes the design pattern chain of responsibilty in each layer of container(Engine, Host, Context, Wrapper). It implements `Valve` and `Pipeline` in these four layers, which means user can develop request handling in each layer. It is very flexible. While I skipped this part and handle the request directly.
3. Apache Tomcat provides a lot of config files to config the server, for example the `${catalina.base}/conf/logging.properties` is responsible for the global logging, `${catalina.base}/conf/catalina.policy`for the sercurity policy.
4. Apache Tomcat provides both `bio` and `nio` connectors to be chosen depends on the case, while only `bio` connector is used in this project 

