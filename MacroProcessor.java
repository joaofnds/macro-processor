import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.IOException;
import java.io.FileWriter; 
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;

public class MacroProcessor { 

  public static ArrayList macroReplace(String call, ArrayList macro) {
    ArrayList<String> code = new ArrayList<String>();
    Iterator iterator = macro.iterator();
    return code;
  }

  public static void main(String[] args) {

    ArrayList<String> asm = new ArrayList<String>();
    ArrayList<String> macro = new ArrayList<String>();

    ArrayList<String> macroSub = new ArrayList<String>();

    //Variável indicando se deve guardar o código refente ao macro
    boolean storeMacro = false;
    String macroName = null;

    //Leitura do arquivo asm
    try {
      File myObj = new File("teste_macro_z808.asm");
      Scanner myReader = new Scanner(myObj);

      while (myReader.hasNextLine()) {
        String line = myReader.nextLine();

        //Se tiver palavra MACRO, começa a salvar o código em macro
        if(line.contains("MACRO")) {
          storeMacro = true;
          
          //Remove o comentário
          String[] aux = line.split(";");
          line = aux[0];

          //Salvando o nome da função Macro
          aux = line.split(" ");
          macroName = aux[0];
        }

        //Se chegar ao fim, muda a variável de controle de store do macro
        if(line.contains("ENDM")) {
          storeMacro = false;
        }

        //Enquanto variável for verdadeira, salva o código do Macro
        if(storeMacro) {
          macro.add(line);
        }

        if(macroName != null && line.contains(macroName)) {
          macroSub = macroReplace(line, macro);
        }

        asm.add(line);
      }

      myReader.close();

    } catch (FileNotFoundException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }
  
    //Criação e escrita de arquivo
    try {
      FileWriter myWriter = new FileWriter("filename.txt");

      Iterator iterator = macro.iterator();
      while(iterator.hasNext()) {
        myWriter.write(iterator.next() + "\n");
      }

      myWriter.close();
    } catch (IOException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }
  }
}
