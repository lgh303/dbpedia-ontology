package ontology;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class Test {
     public static void main (String[] args) {
          int x = 4;
          System.out.println("Count: " + x);
          try (Scanner scanner = new Scanner(new File("sparql/test.rq"))) {
               String content = scanner.useDelimiter("\\Z").next();
               System.out.println(content);
          } catch (IOException e) {
               e.printStackTrace();
          }
     }
}
