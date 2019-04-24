JCC = javac

default: Regork.java
	$(JCC) Regork.java

jar: Regork.class
	cp Regork.class config/Regork.class
	jar cfmv config/Regork.jar config/Manifest.txt Regork.class
	mv config/Regork.jar .

Regork.class: Regork.java
	$(JCC) Regork.java

clean:
	rm *.class
	rm config/Regork.class
	rm config/Regork.jar
