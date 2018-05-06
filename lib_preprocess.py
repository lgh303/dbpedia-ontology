#!/usr/bin/env python

import random

RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
RDFS = "http://www.w3.org/2000/01/rdf-schema#"
OWL = "http://www.w3.org/2002/07/owl#"

def wrap(string):
    return "<" + string + ">"

type_rel = wrap(RDF + "type")
domain_rel = wrap(RDFS + "domain")
range_rel = wrap(RDFS + "range")
sameas_rel = wrap(OWL + "sameAs")
owl_class = wrap(OWL + "Class")

def triple(line):
    tup = line.strip(" .\n").split(" ")
    return tup[0], tup[1], " ".join(tup[2:])

def prepr_concepts(lines):
    classes = set()
    properties = set()
    dbpedia_lines = []
    p_counter = {}
    for line in lines:
        subj, rel, obj  = triple(line)
        if rel in [domain_rel, range_rel]:
            p_counter[subj] = p_counter.get(subj, 0) + 1
            if p_counter[subj] == 2:
                properties.add(subj)
        elif rel == type_rel and obj == owl_class:
            classes.add(subj)
    for line in lines:
        subj, rel, obj  = triple(line)
        if (rel in [domain_rel, range_rel] and subj in properties) or \
           (rel == type_rel and obj == owl_class):
            dbpedia_lines.append(line)
    return classes, properties, dbpedia_lines

def prepr_interlang_shuffle(lines, subj_prefix, obj_prefix, num_remain):
    interlang_lines = []
    for line in lines:
        subj, rel, obj = triple(line)
        if rel == sameas_rel and \
           subj_prefix in subj and obj_prefix in obj:
            interlang_lines.append(line)
    random.shuffle(interlang_lines)
    interlang_lines = interlang_lines[:num_remain]
    interlang_pairs = set()
    for line in interlang_lines:
        subj, rel, obj = triple(line)
        interlang_pairs.add((subj, obj))
    return interlang_pairs, interlang_lines

def prepr_instances(lines, cand_instances, classes):
    instances = set()
    types_lines = []
    for line in lines:
        subj, rel, obj = triple(line)
        if rel == type_rel and subj in cand_instances and obj in classes:
            instances.add(subj)
            types_lines.append(line)
    return instances, types_lines

def prepr_mappingbased(lines, properties, instances):
    mappingbased_lines = []
    for line in lines:
        subj, rel, obj = triple(line)
        if rel in properties and subj in instances:
            mappingbased_lines.append(line)
    return mappingbased_lines
