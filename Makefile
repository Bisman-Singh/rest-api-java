JAVAC = javac
JAVA = java
SRC = src
OUT = out

all: compile

compile:
	mkdir -p $(OUT)
	$(JAVAC) -d $(OUT) $(SRC)/*.java

run: compile
	$(JAVA) -cp $(OUT) HttpServer

clean:
	rm -rf $(OUT)

.PHONY: all compile run clean
