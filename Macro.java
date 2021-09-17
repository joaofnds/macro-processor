import java.io.File;  
import java.io.FileNotFoundException;  
import java.util.Scanner;
import java.util.ArrayList;

public class Macro { 
  public static void main(String[] args) {

    ArrayList<String> asm = new ArrayList<String> ();
    ArrayList<String> macro = new ArrayList<String> ();

    boolean storeMacro = false;

    //Leitura do arquivo asm
    try {
      File myObj = new File("teste_macro_z808.asm");
      Scanner myReader = new Scanner(myObj);

      while (myReader.hasNextLine()) {
        String line = myReader.nextLine();
        asm.add(line);

        if(line.contains("MACRO")) {
          storeMacro = true;
        }

        if(line.contains("ENDM")) {
          storeMacro = false;
        }

        if(storeMacro) {
          macro.add(line);
        }
      }

      System.out.println(macro);
      myReader.close();

    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}
