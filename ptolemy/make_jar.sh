# Make out dir and compile java files
mkdir -p out_jar
javac -sourcepath ../. -d ./out_jar/ ./gui/*.java ./plot/*.java ./util/*.java
# Copy dependencies
cd out_jar
mkdir -p ptolemy/plot/img
mkdir -p ptolemy/plot/plotml
cp -R ../plot/img/*.gif ptolemy/plot/img
cp -R ../plot/plotml/*.jar ptolemy/plot/plotml
# Create jar files
jar --create --file ../out_jar/gui.jar  ./ptolemy/gui/
jar --create --file ../out_jar/plot.jar  ./ptolemy/plot/
jar --create --file ../out_jar/util.jar  ./ptolemy/util/