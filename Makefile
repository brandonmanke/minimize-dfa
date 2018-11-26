JAVAC = javac
JAVA = java

build: *.java
	$(JAVAC) $^

clean: *.class
	rm *.class
