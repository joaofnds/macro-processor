import java.io.File;  
import java.io.FileNotFoundException;  
import java.util.Scanner;
import java.util.ArrayList;

public class Macro { 
  public static void main(String[] args) {
    ArrayList<String> asm = new ArrayList<String> ();

    try {
      File myObj = new File("teste_macro_z808.asm");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        asm.add(myReader.nextLine());
      }
      System.out.println(asm);
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}
