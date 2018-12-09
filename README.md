# Phenomiser

This is new implementation of the Phenomiser algorithm published in https://www.sciencedirect.com/science/article/pii/S0002929709003991

**Usuage**

You can make a query programmatically using the Phenomiser class. Alternatively, you can build a jar file with all dependencies and run from the command line.

Test the app with the following variables. Note in the debug mode, which saves computatation time, you can only query with three HPO terms for their matching 100 diseases. For more instructions on command line variables, run with "-h" to print out help information.

```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM,ORPHA
-q HP:0003074,HP:0010645,HP:0001943
-debug
```

It will take a long time to run the app without "-debug" on a four-core laptop (~ hours). Use "-cpu 10" to request more resources (if available). 

