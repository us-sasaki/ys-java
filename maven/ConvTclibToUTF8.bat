javac ConvEncoding.java
java ConvEncoding tclib/src false

cd tclib
mvn javadoc:javadoc
mvn package

pause
