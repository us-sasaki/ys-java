javac -cp .;diffutils-1.2.1.jar DiffTest.java
java -cp .;diffutils-1.2.1.jar DiffTest a.txt b.txt

echo "-----------------------"

java -cp .;diffutils-1.2.1.jar DiffTest FileLister.java FileLister2.java

pause
