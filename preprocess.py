#!/usr/bin/env python

import os.path as osp
from datetime import datetime

from lib_preprocess import prepr_concepts
from lib_preprocess import prepr_interlang_shuffle
from lib_preprocess import prepr_instances
from lib_preprocess import prepr_mappingbased

RES_DIR = "resources"
SAVE_DIR = "cache"

def save(lines, filename):
    with open(osp.join(SAVE_DIR, filename), "w") as ouf:
        ouf.writelines(lines)
        print "[%s] Saved %s" % (str(datetime.now()), filename)

with open(osp.join(RES_DIR, "dbpedia_2016-10.nt")) as inf:
    lines = inf.readlines()
    classes, properties, dbpedia_lines = prepr_concepts(lines)
    save(dbpedia_lines, "dbpedia_2016-10.nt")

with open(osp.join(RES_DIR, "interlanguage_links_en.ttl")) as inf:
    lines = inf.readlines()
    interlang_pairs, interlang_lines = prepr_interlang_shuffle(
        lines, "http://dbpedia.org/", "http://fr.dbpedia.org/", 150000)
    save(interlang_lines, "interlanguage_links_en.ttl")

def prepare_instances(lang, cand_instances, classes):
    with open(osp.join(RES_DIR, "instance_types_%s.ttl" % lang)) as inf:
        lines = inf.readlines()
        instances, types_lines = prepr_instances(
            lines, cand_instances, classes)
        save(types_lines, "instance_types_%s.ttl" % lang)
    return instances

instances_en = prepare_instances(
    "en", set([en for en, fr in interlang_pairs]), classes)
instances_fr = prepare_instances(
    "fr", set([fr for en, fr in interlang_pairs]), classes)

def prepare_mappingbased(atype, lang, instance, properties):
    with open(osp.join(RES_DIR, "mappingbased_%s_%s.ttl" % (atype, lang))) as inf:
        lines = inf.readlines()
        mappingbased_lines = prepr_mappingbased(lines, properties, instance)
        save(mappingbased_lines, "mappingbased_%s_%s.ttl" % (atype, lang))

prepare_mappingbased("literals", "en", instances_en, properties)
prepare_mappingbased("objects", "en", instances_en, properties)
prepare_mappingbased("literals", "fr", instances_fr, properties)
prepare_mappingbased("objects", "fr", instances_fr, properties)
