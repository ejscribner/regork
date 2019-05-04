JCC = javac

default: Regork.java
	$(JCC) Regork.java

jar: Regork.class
	cp Regork.class config/Regork.class
	cp Manager.class config/Manager.class
	cp DataIntegrity.class config/DataIntegrity.class
	cp Ordering.class config/Ordering.class
	cp CommandLineTable.class config/CommandLineTable.class
	jar cfmv config/Regork.jar config/Manifest.txt Regork.class Manager.class DataIntegrity.class Ordering.class CommandLineTable.class
	mv config/Regork.jar .

Regork.class: Regork.java
	$(JCC) Regork.java

clean:
	rm *.class
	rm config/*.class
	rm config/Regork.jar
