# Build ontology with Jena using DBpedia data.

## 0. Requirements

* python2.7 for preprocessing data
* JDK
* Jena3

```
    export JAVA_HOME=/path/to/jdk
    export JENA_HOME=/path/to/jena
    export CLASSPATH=.:$JAVA_HOME/lib/tools.jar:$JENA_HOME/lib/*
```

## 1. Data download

Download the dataset from http://wiki.dbpedia.org/downloads-2016-10

The following datasets are used:

* dbpedia_2016-10.nt
* instance_types_en.ttl
* instance_types_fr.ttl
* interlanguage_links_en.ttl
* mappingbased_literals_en.ttl
* mappingbased_literals_fr.ttl
* mappingbased_objects_en.ttl
* mappingbased_objects_fr.ttl


It is suggested to symbol link these datasets under `resources/`.

## 2. Process the data

```
    python preprocess.py
```

It will sample all classes, most properties and 10w+ individuals to `cache/`.

## 3. Build ontology and do statistics

```
    javac ontology/Main.java
    java ontology/Main
```

