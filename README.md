# indexer_searcher

this repository contains my solution to an assignment for the __Object-Oriented Programming__ course on the __University of Warsaw, Faculty of Mathematics, Informatics, and Mechanics__

_the actual contents of the task together with the guideline are an intelectual property of the university employees,
 so I cannot have included them in this repository, nevertheless the concepts of the task are briefly introduced below_

we were asked to build a pair of complementary tools, using Apache Tika and Apache Lucene libraries, precisely following a detailed guideline. We also had to use Maven for dependency resolution and project building.
  
## indexer

is resposible for creating an index of document contents, provides both on-demand indexing, by command-line options and continuous, by monitoring and reacting to changes in selected directories (recursively)

## searcher

provides a convenient, REPL-like interface for performing various types of queries on the index
and displaying search results with context and proper phrase highlighting

## state

although the current build already matches the guideline, there are a few future plans

- [ ] bonus feature SearchCompleter
- [ ] search cancellation on UserInterruptException

## build

`maven` is required for building the project, a jar-with-dependencies target is predifined and can be executed with:
```
mvn clean compile assembly:single
```

## usage

 ```
 > %details on  
 > %color on  
 > release   
 File count: 1  
 /Users/kuba/Documents/TEST/refman-8.0-en.pdf
 It documents MySQL 8.0 through 8.0.18, as well as NDB Cluster releases  
 based on version 8.0 of NDB through 8.0.18-ndb-8.0.18, respectively. It may include documentation of features of  
 MySQL versions that have not yet been released. For information about which versions have been released, see the  
 MySQL 8.0 Release Notes.  
 ... For notes detailing the changes in each release, see the MySQL 8.0 Release Notes.  
    
 >   
 ```
 
 TODO ...
