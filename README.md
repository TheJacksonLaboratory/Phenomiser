# Phenomiser

This is new implementation of the Phenomiser algorithm published in https://www.sciencedirect.com/science/article/pii/S0002929709003991

You can make a query programmatically using the Phenomiser class. Alternatively, you can build a jar file with all dependencies and run from the command line.


**Quick start**

1. Test the app with the following variables. 
Note in the debug mode, which saves computatation time, you can only query with three HPO terms for their matching with 100 diseases. 

```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM
-q HP:0003074,HP:0010645,HP:0001943
-debug
```
You should be able to see a list of diseases and associated P values printed out in your console. 

2. Remove "-debug" to find similarities of query terms to all diseases. 
Note we used "-f" to force the app not using cached data that was computed for debug mode in the last step, and that we used "-sampling 3-3" to save computing time (see below for details).
Also note that this step will take 2~4 hours on a personal computer.

```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM
-q HP:0003074,HP:0010645,HP:0001943
-sampling 3-3
-f
```

3. Make more queries with new sets of HPO terms (Must be three HPO terms).
Note that we do not use "-f" because we can (and want to) used cached data from last step.

```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM
-q ${HP term1},${HP term2},${HP term3}
```

4. Make a formal query with the following code. Note we used "-f" to force the app to recompute data because the cached 
data from last step only works if we query with three HPO terms. This will take a LONG time on a personal computer.

```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM
-q ${HP term1},${HP term2},${HP term3}
-f
```

5. Make following calls with cached data. This should take only minutes or less as we can used cached data.
```
-hpo ${path to}hp.obo
-da ${path to}phenotype.hpoa
-db OMIM
-q {1-10 HPO terms separated by ","}
```


**Usuage in details**

Run the app with "-h" to print out a list of all arguments:

```
usage: Phenomiser
 -cachePath,--cachePath <arg>      specify the path to cache folder
 -cpu,--cpu <arg>                  specify the number of threads to use
                                   (default: 4)
 -da,--disease-annotations <arg>   [required]specify full path to disease
                                   annotations
 -db,--disease-database <arg>      specify comma-separated database
                                   [OMIM,ORPHA]
 -debug,--debug_mode               save computation time if this option is
                                   used
 -exit,--exit                      exit afterward
 -f,--force_recache                force recompute to get a new cache of
                                   resources
 -h,--help                         print usage
 -hpo <arg>                        [required]specify full path to hp.obo
 -o,--out <arg>                    specify output path
 -q,--query-terms <arg>            [required]specify comma-separated query
                                   terms (e.g. HP:0003074)
 -sampling,--sampling <arg>        specify sampling range in format
                                   "min-max" (default 1-10)
```

Important notes:

- The first run will take a LONG time on a personal computer as the app needs to sample HPO terms to compute similarity 
score distributions for all diseases. Therefore, we suggest using -debug mode if you just want to try it out. However, 
you can only query with three HPO terms under debug mode. Also, the app only analyze the similarities to a randomly 
selected subset of 100 diseases. As a result, the top ranked diseases (smalled p values) are probably not the ones that 
you would hope to see.  

- We strongly suggest running the app on a cluster with multiple CPUs, and then download the cached data to use on 
personal computers. 

- use "-cpu" to inform the app the number of available CPUs. 

- Default caching data is under HOME/Phenomiser.data. Use "-cachePath" if you want to overwrite the default setting.

- Since the app tries to use cached data when they are present, pay attention whether cached data is correct for a query.
Use "-f" to force the app to recompute and overwrite old caching data.