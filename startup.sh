rm -f bootstrap.jar

# Only Bootstrap.class and CommonClassLoader.class are needed to start the server, this two classes will be loaded by ApplicationClassLoader, the others will be loaded by CommonClassLoader.
jar cvf0 bootstrap.jar -C target/classes uk/ac/ucl/Bootstrap.class -C target/classes uk/ac/ucl/classLoader/CommonClassLoader.class

rm -f lib/TomcatDIY.jar

cd target/classes

jar cvf0 ../../lib/TomcatDIY.jar *

cd ..
cd ..


java -cp bootstrap.jar uk.ac.ucl.Bootstrap

