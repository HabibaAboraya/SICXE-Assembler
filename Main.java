import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {
   private static List<String> labels = new ArrayList<>();

    private static List<String> instructions = new ArrayList<>();

    private static List<String> references = new ArrayList<>();

    private static List<String> locationCounter = new ArrayList<>();

    private static List<String> objectCode = new ArrayList<>();

    private static List<String[]> symbolTableList = new ArrayList<>();

    static int base = 0000 ;

    static String b="";

    public static void main(String[] args) {

        Converter.initialize();

        readFile("/Users/habibaaboraya/Downloads/inSICXE.txt");

        printFile();

        pass1();

        printSymbolTable();


       calculateObjectCode();

        printFullTable();

        generateHTERecords();

        createMR ();




    }

    // A function to read file
    public static void readFile(String fileName) {

        try {

            File file = new File(fileName);

            Scanner scanner = new Scanner(file);

            // Read the file line by line
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();

                String[] arr = line.split("\\s+"); // Split by spaces

                if (arr.length == 3) {

                    labels.add(arr[0]);

                    instructions.add(arr[1]);

                    references.add(arr[2]);

                }

                else if (arr.length == 2) {

                    labels.add("$$$$$"); // Placeholder for no label

                    instructions.add(arr[0]);

                    references.add(arr[1]);

                }

            }

            scanner.close();

        } catch (FileNotFoundException e) {

            System.out.println("File not found: " + e.getMessage());

        }

    }

    //A function to Print file
    public static void printFile() {

        System.out.println("File Content:");

        System.out.printf("%-10s %-15s %-15s %n", "Labels", "Instructions", "References");

        for (int j = 0; j < instructions.size(); j++) {

            System.out.printf("%-10s %-15s %-15s %n", labels.get(j), instructions.get(j), references.get(j));

        }

    }


    //A function to calculate location counter and print symbol table
    public static void pass1() {

        if (references.isEmpty()) {

            System.out.println("No references found to process.");

            return;

        }

        locationCounter.add(String.format("%04X", Integer.parseInt(references.get(0), 16)));//location counter [0]

        locationCounter.add(String.format("%04X", Integer.parseInt(references.get(0), 16)));//location counter [1]



        for (int i = 0; i < instructions.size(); i++) {

            int prevLoc = Integer.parseInt(locationCounter.get(i), 16);

            String instr = instructions.get(i);

            String ref = references.get(i);

            String format = Converter.getFormat(instr);


            if (format.equals("1")) {

                locationCounter.add(String.format("%04X", prevLoc + 1));

            } else if (format.equals("2")) {

                locationCounter.add(String.format("%04X", prevLoc + 2));

            } else if (format.equals("3")) {

                locationCounter.add(String.format("%04X", prevLoc + 3));

            }

            else if (instr.equals("BYTE")) {

                int length;

                if (ref.startsWith("X")) {

                    length = (ref.length() - 3) / 2;

                }

                else {

                    length = ref.length() - 3;

                }

                locationCounter.add(String.format("%04X", prevLoc + length));

            }

            else if (instr.equals("RESB")) {

                locationCounter.add(String.format("%04X", prevLoc + Integer.parseInt(ref)));

            }

            else if (instr.equals("RESW")) {

                locationCounter.add(String.format("%04X", prevLoc + Integer.parseInt(ref) * 3));

            }
            else if (instr.equals("WORD")) {

                locationCounter.add(String.format("%04X", prevLoc + 3));

            }
            else if (instr.startsWith("+")) {

                locationCounter.add(String.format("%04X", prevLoc + 4));

            }
            else if(instr.equals("BASE")){

                locationCounter.add(String.format("%04X", prevLoc));
                b = ref;

            }

        }

        // Print the results for pass1
        System.out.println("---------------------------------------------");

        System.out.println("pass1:");

        System.out.printf("%-10s %-15s %-15s %-15s %n", "LocCtr", "Labels", "Instructions", "References");

        for (int i = 0; i < instructions.size(); i++) {

            System.out.printf("%-10s %-15s %-15s %-15s %n", locationCounter.get(i), labels.get(i), instructions.get(i), references.get(i));

        }

    }

    //A function to print symbol table and length
        public static void printSymbolTable() {

            if (locationCounter.size() == 0) {

                System.out.println("No location counters available.");

                return;

            }
            int firstLoc = Integer.parseInt(locationCounter.get(0), 16); // First location counter

            int lastLoc = Integer.parseInt(locationCounter.get(locationCounter.size() - 1), 16); // Last location counter

            int programLength = lastLoc - firstLoc;

            // Convert the program length to hexadecimal
            String programLengthHex = Integer.toHexString(programLength).toUpperCase(); // Convert to hex and make uppercase


            System.out.println("---------------------------------------------");

            System.out.println("Symbol Table");


            System.out.printf("%-10s %-15s %n", "Labels", "Location Counter");

            for (int i = 0; i < labels.size(); i++) {

                if (!labels.get(i).equals("$$$$$")) {

                    String[] labelLocPair = {labels.get(i), locationCounter.get(i)};

                    symbolTableList.add(labelLocPair);

                    System.out.printf("%-10s %-15s %n", labels.get(i), locationCounter.get(i));

                }
                if(labels.get(i).equals(b)){
                    base = Integer.parseInt(locationCounter.get(i));
                }

            }

            System.out.println("Program Length: " +programLengthHex);
        }

    //Pass 2


    //A function to calculate opcode + n + i

    public static String calculateOpcodeNI(String opcode, String ref) {

         String binary = Integer.toBinaryString(Integer.parseInt(opcode, 16));

         binary = String.format("%8s", binary).replace(" ", "0");

        if (ref.startsWith("#")) {

            binary = binary.substring(0, binary.length() - 2) + "01";

        }

        else if (ref.startsWith("@")) {

            binary = binary.substring(0, binary.length() - 2) + "10";

        }

        else{
            binary = binary.substring(0, binary.length() - 2) + "11";
        }

        int decimal = Integer.parseInt(binary, 2);

        String hexValue = String.format("%02X", decimal);

        return hexValue;

    }

    //A function to calculate xbpe and displacement or address
    public static String calculateXBPEDisp(String ref, String nextLocCtr) {

        String xbpe = "0";

        String add = "";

        String dispString="";

        if (ref.startsWith("#")) {
            ref = ref.substring(1);  // Remove the '#' symbol
            }



        for (int i = 0; i < symbolTableList.size(); i++) {

            String[] symbol = symbolTableList.get(i);

            // Check if the label matches
            if (symbol[0].equals(ref)) {

                // Parse the location counters (locctr) as integers (in base 16)
                int TA = Integer.parseInt(symbol[1], 16);  // Convert locctr of label to integer


                int pc = Integer.parseInt(nextLocCtr, 16);  // Convert nextLocCtr to integer

                // Calculate the displacement
                int dis = TA - pc ;

                if(!(dis >= -2048 && dis <= 2047 )){

                    dis = TA - base;

                }

                 dispString = String.format("%03X", dis).toUpperCase();


                if(ref.startsWith("+") && ref.endsWith(",x")) {

                        xbpe = "90"; // 1001

                        ref = ref.replace(",x", ""); // Remove ",x"

                        add = xbpe + Integer.toHexString(TA).toUpperCase();

                        return add;

                }
                else if (ref.startsWith("+")){

                        xbpe = "10";//0001

                        ref = ref.replace(",x", ""); // Remove ",x"

                        add = xbpe + Integer.toHexString(TA).toUpperCase();

                        return add;
                    }

                else if ( ref.endsWith(",x")){

                    xbpe = "A"; //1010

                    return xbpe + dispString;

                }
                else if (ref.endsWith(",x") && !(dis >= -2048 && dis <= 2047 )){

                    xbpe = "C";//1100

                    return xbpe + dispString;

                }

                else if(!(dis >= -2048 && dis <= 2047 )){

                    xbpe = "4";//0010

                    return xbpe + dispString;

                }

                else{

                    xbpe = "2";//0010

                    return xbpe + dispString;
                }

                }



    }
        return "Label not found";
    }


    public static void calculateObjectCode() {

        for (int i = 0; i < instructions.size(); i++) {

            String nextLocCtr = (i + 1 < locationCounter.size()) ? locationCounter.get(i + 1) : locationCounter.get(i);

            String ref = references.get(i);

            String instr = instructions.get(i);

            String opcode = Converter.getOpcode(instr);

            String format = Converter.getFormat(instr);

            if ("START".equals(instr) || "BASE".equals(instr) || "RESB".equals(instr) || "RESW".equals(instr) || "END".equals(instr)) {
                objectCode.add("------");

            }

            else if (format.equals("1")) {
                objectCode.add(opcode);
            }
            else if (format.equals("2")) {
                String[] registerPair = ref.split(","); // Split the input into registers
                String reg1, reg2;
                if (registerPair.length == 1) {
                    reg1 = Registers.getRegisterHex(registerPair[0]);
                    reg2 = "00"; // Default value for second register
                }
                // Case where there are two registers (e.g., "ADD A, B")
                else if (registerPair.length == 2) {
                    reg1 = Registers.getRegisterHex(registerPair[0]);
                    reg2 = Registers.getRegisterHex(registerPair[1]);
                }
                // If the format is wrong (e.g., no registers provided), handle the error
                else {
                    objectCode.add("Error");
                    continue;
                }

                objectCode.add(opcode + reg1 + reg2);
            }
            else if (format.equals("3")) {
                String part1= calculateOpcodeNI (opcode,ref);
                String part2= calculateXBPEDisp(ref,nextLocCtr);
                objectCode.add(part1+part2);
            }

            else if(instr.equals("WORD")){
                try {
                    int decimalValue = Integer.parseInt(ref); // Parse the reference as a decimal integer
                    String hexValue = String.format("%06X", decimalValue); // Convert to a 6-character hexadecimal string
                    objectCode.add(hexValue); // Add the formatted hexadecimal value as object code
                } catch (NumberFormatException e) {
                    objectCode.add("Error"); // Add an error message as object code
                    System.err.println("Invalid decimal value for WORD: " + ref);
                }


            }
            else if (instr.equals("BYTE")) {
                try {
                    if (ref.startsWith("X'") && ref.endsWith("'")) {
                        String hexValue = ref.substring(2, ref.length() - 1).toUpperCase();
                        objectCode.add(hexValue);
                    } else if (ref.startsWith("C'") && ref.endsWith("'")) {
                        String charSequence = ref.substring(2, ref.length() - 1);
                        StringBuilder asciiHex = new StringBuilder();
                        for (char c : charSequence.toCharArray()) {
                            asciiHex.append(String.format("%02X", (int) c));
                        }
                        objectCode.add(asciiHex.toString());
                    } else {
                        objectCode.add("Error");
                        System.err.println("Invalid BYTE directive: " + ref);
                    }
                } catch (Exception e) {
                    objectCode.add("Error");
                    System.err.println("Error processing BYTE directive: " + ref);
                }}
        }


    }

    // A function to print the whole table with object code
    public static void printFullTable() {
        System.out.println("---------------------------------------------");
        System.out.println("Full Table with Object Code:");

        System.out.printf("%-10s %-15s %-15s %-15s %-15s %n", "LocCtr", "Labels", "Instructions", "References", "Object Code");

        // Iterate through the instructions, location counters, and object codes
        for (int i = 0; i < instructions.size(); i++) {
            // Get the object code for the current instruction
            String objectCodeValue = (i < objectCode.size()) ? objectCode.get(i) : "------"; // Handle cases where no object code is present

            // Print the row with Location Counter, Labels, Instructions, References, and Object Code
            System.out.printf("%-10s %-15s %-15s %-15s %-15s %n", locationCounter.get(i), labels.get(i), instructions.get(i), references.get(i), objectCodeValue);
        }

        System.out.println("---------------------------------------------");
    }


    public static void generateHTERecords() {
        // Header Record
        String headerRecord = "";
        List<String> textRecords = new ArrayList<>();
        String endRecord = "";

        // Generate Header Record
        String programName = labels.get(0).equals("$$$$$") ? "DEFAULT" : labels.get(0); // Use the first label or "DEFAULT"
        String startAddress = String.format("%06X", Integer.parseInt(locationCounter.get(0), 16)); // Ensure 6-digit hex format
        String programLength = String.format("%06X", Integer.parseInt(locationCounter.get(locationCounter.size() - 1), 16)
                - Integer.parseInt(startAddress, 16));

        headerRecord = "H^" + String.format("%-6s", programName)+"^" + startAddress +"^"+ programLength;

        // Generate Text Records
        String currentTextRecord = "";
        String currentStartAddress = "";
        int currentByteCount = 0;

        for (int i = 0; i < objectCode.size(); i++) {
            String objCode = objectCode.get(i);

            if ("------".equals(objCode)) {
                continue; // Skip lines with no object code
            }

            if (currentTextRecord.isEmpty()) {
                // Start a new text record
                currentStartAddress = locationCounter.get(i);
            }

            if (currentByteCount + objCode.length() / 2 > 30) {
                // Finish the current text record if it exceeds 30 bytes
                textRecords.add("T" + currentStartAddress + String.format("%02X", currentByteCount) + currentTextRecord);
                currentTextRecord = ""; // Reset for a new record
                currentByteCount = 0;
                currentStartAddress = locationCounter.get(i); // Update start address for the new record
            }

            // Add the current object code
            currentTextRecord += objCode;
            currentByteCount += objCode.length() / 2; // Update byte count
        }

        // Add the last text record if any
        if (!currentTextRecord.isEmpty()) {
            textRecords.add("T^" + currentStartAddress + String.format("%02X", currentByteCount) + currentTextRecord);
        }

        // Generate End Record
        endRecord = "E" +"^"+ startAddress;

        // Print the HTE Records
        System.out.println("---------------------------------------------");
        System.out.println("HTE Records:");
        System.out.println(headerRecord);
        for (String textRecord : textRecords) {
            System.out.println(textRecord);
        }
        System.out.println(endRecord);
        System.out.println("---------------------------------------------");
    }

    public static void createMR (){

        for (int i = 0; i < instructions.size(); i++) {

            String instr = instructions.get(i);

            String ref = references.get(i);

            int locctr = Integer.parseInt(locationCounter.get(i)+1,16);

            String lc = String.format("%06X", locctr);

            if(instr.startsWith("+") && !(ref.startsWith("#"))){

                System.out.println("M^"+lc+"^"+"05");

            }


    }



}}























