JAVAC = javac	
all_javas := ./all.javas

.PHONY: compile
compile: $(all_javas)
	$(JAVAC) -d ../target/. @$<

.INTERMEDIATE: $(all_javas)
$(all_javas):
	find . -name '*.java' > $@

clean:
	rm -r ../target/*
