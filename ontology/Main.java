package ontology;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.query.*;

import java.util.*;
import java.io.*;

public class Main {

     public static OntModel load(String datasetPath) {
          OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
          model.read(datasetPath);
          System.out.println("Loaded: " + datasetPath);
          return model;
     }

     private static int count(Iterator iter) {
          int cnt = 0;
          while (iter.hasNext()) {
               OntResource resource = (OntResource)iter.next();
               // if (cnt < 5) {
               //      System.out.println(resource.getURI());
               // }
               cnt++;
          }
          return cnt;
     }

     private static void addOne(HashMap<String, Integer> counter, String key) {
          if (counter.containsKey(key)) {
               counter.put(key, counter.get(key) + 1);
          } else {
               counter.put(key, 1);
          }
     }

     private static void sortCounter(HashMap<String, Integer> counter, int max) {
          List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(counter.entrySet());
          Collections.sort(list, new Comparator<Map.Entry<String,Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1,
                                       Map.Entry<String, Integer> o2) {
                         return o2.getValue() - o1.getValue();
                    }
               });
          int count = 0;
          int total = 0;
          for (Map.Entry<String, Integer> entry : list) {
               count = count + 1;
               total = total + entry.getValue();
               if (count < max) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
               }
          }
          System.out.println("Unique=" + count + ", Total=" + total);
          System.out.println("------");
     }

     public static void statConcepts(OntModel model, String tag) {
          System.out.println("\n----- " + tag + " classes & properties -----");
          Iterator<OntClass> allClassIter = model.listClasses();
          System.out.println("Count classes: " + count(allClassIter));
          Iterator<OntProperty> allPropertyIter = model.listAllOntProperties();
          System.out.println("Count property: " + count(allPropertyIter));
          Iterator<DatatypeProperty> allDatatypeIter = model.listDatatypeProperties();
          System.out.println("Count Datatype Property: " + count(allDatatypeIter));
          Iterator<ObjectProperty> allObjectPropIter = model.listObjectProperties();
          System.out.println("Count Object Property: " + count(allObjectPropIter));
     }

     public static void statIndividuals(OntModel model, String tag) {
          System.out.println("\n----- " + tag + " individuals -----");
          Iterator<Individual> allIndividuals = model.listIndividuals();
          System.out.println("Individuals: " + count(allIndividuals));
     }

     public static void statOccurrences(OntModel model, String tag) {
          System.out.println("\n----- " + tag + " occurrences -----");
          StmtIterator iter = model.listStatements();
          HashMap<String, Integer> subjCounter = new HashMap<String, Integer>();
          HashMap<String, Integer> predCounter = new HashMap<String, Integer>();
          HashMap<String, Integer> objCounter = new HashMap<String, Integer>();
          while (iter.hasNext()) {
               Statement stmt = iter.next();
               String subject = stmt.getSubject().getURI();
               String predicate = stmt.getPredicate().getURI();
               addOne(subjCounter, subject);
               addOne(predCounter, predicate);
               RDFNode object = stmt.getObject();
               if (object.isResource()) {
                    addOne(objCounter, object.asResource().getURI());
               }
          }
          System.out.println(tag + ": Subject Counter");
          sortCounter(subjCounter, 8);
          System.out.println(tag + ": Predicate Counter");
          sortCounter(predCounter, 8);
          System.out.println(tag + ": Object Counter");
          sortCounter(objCounter, 8);

          OntClass cls = model.getOntClass("http://www.w3.org/2002/07/owl#Thing");
          Iterator<OntClass> iterSubClass = cls.listSubClasses();
          HashMap<String, Integer> subClsCounter = new HashMap<String, Integer>();
          while (iterSubClass.hasNext()) {
               String objectURI = iterSubClass.next().getURI();
               Integer value = objCounter.get(objectURI);
               if (value == null) {
                    value = 0;
               }
               subClsCounter.put(objectURI, value);
          }
          System.out.println(tag + ": Subclass Counter of #Thing");
          sortCounter(subClsCounter, 8);
     }

     public static void runSparql(OntModel model, String sparqlFilePath) {
          String content = null;
          try (Scanner scanner = new Scanner(new File(sparqlFilePath))) {
               content = scanner.useDelimiter("\\Z").next();
          } catch (IOException e) {
               e.printStackTrace();
               return;
          }
          System.out.println("\n----- " + sparqlFilePath + " -----");
          System.out.println(content);
          long startTime = System.currentTimeMillis();
          Query query = QueryFactory.create(content);
          QueryExecution qexec = QueryExecutionFactory.create(query, model);
          try {
               ResultSet results = qexec.execSelect();
               ResultSetFormatter.out(results, model);
          } finally {
               qexec.close();
          }
          long stopTime = System.currentTimeMillis();
          long elapsedTime = stopTime - startTime;
          System.out.println("Elapsed Time: " + elapsedTime + "ms");
     }

     public static void writeOwl(OntModel model, String savePath) {
          BufferedWriter out = null;
          try {
               out = new BufferedWriter(new FileWriter(savePath));
               model.write(out, "RDF/XML");
               System.out.println("Saved " + savePath);
               out.close();
          } catch (IOException e) {
               e.printStackTrace();
          }
     }

     public static void run(OntModel model, OntModel[] subModels,
                            String type, String extra) {
          for (OntModel subModel: subModels) {
               model.addSubModel(subModel);
          }
          if (type == "concepts") {
               statConcepts(model, extra);
          } else if (type == "individuals") {
               statIndividuals(model, extra);
          } else if (type == "occurrences") {
               statOccurrences(model, extra);
          } else if (type == "sparql") {
               runSparql(model, extra);
          } else {
               System.out.println("Warning: unknown type " + type);
          }
          for (OntModel subModel: subModels) {
               model.removeSubModel(subModel);
          }
     }

     public static void main(String[] args) {

          String resourceDir = "cache/";

          OntModel base = load(resourceDir + "dbpedia_2016-10.nt");
          OntModel insTypesEn = load(resourceDir + "instance_types_en.ttl");
          OntModel insTypesFr = load(resourceDir + "instance_types_fr.ttl");
          OntModel mapLiteralEn = load(resourceDir + "mappingbased_literals_en.ttl");
          OntModel mapObjEn = load(resourceDir + "mappingbased_objects_en.ttl");

          OntModel mapLiteralFr = load(resourceDir + "mappingbased_literals_fr.ttl");
          OntModel mapObjFr = load(resourceDir + "mappingbased_objects_fr.ttl");
          OntModel interLangEn= load(resourceDir + "interlanguage_links_en.ttl");

          OntModel[] allEnv = {insTypesEn, insTypesFr};
          run(base, allEnv, "concepts", "all");
          run(base, allEnv, "individuals", "all");

          OntModel[] enEnv = {insTypesEn};
          run(base, enEnv, "individuals", "en");

          OntModel[] frEnv = {insTypesFr};
          run(base, frEnv, "individuals", "fr");

          OntModel[] enOccurEnv = {insTypesEn, mapLiteralEn, mapObjEn};
          run(base, enOccurEnv, "occurrences", "en");
          OntModel[] frOccurEnv = {insTypesFr, mapLiteralFr, mapObjFr};
          run(base, frOccurEnv, "occurrences", "fr");
          OntModel[] allOccurEnv = {insTypesEn, mapLiteralEn, mapObjEn,
                                    insTypesFr, mapLiteralFr, mapObjFr};
          run(base, allOccurEnv, "occurrences", "all");

          // SPARQL

          OntModel[] baseEnv = {};
          run(base, baseEnv, "sparql", "sparql/domainrange.rq");
          run(base, baseEnv, "sparql", "sparql/subclass.rq");
          run(base, baseEnv, "sparql", "sparql/count_relation.rq");

          OntModel[] resourceEnv = {insTypesEn, mapLiteralEn};
          run(base, resourceEnv, "sparql", "sparql/resource.rq");

          OntModel[] sparqlSameasEnv = {insTypesEn, insTypesFr, interLangEn};
          run(base, sparqlSameasEnv, "sparql", "sparql/sameas.rq");

          // Save Model (only the base?)
          // writeOwl(base, "output/ontology.owl");
     }
}
