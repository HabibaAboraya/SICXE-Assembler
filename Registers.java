public class Registers {
    public static String getRegisterHex(String register) {
        switch (register.toUpperCase()) {
            case "A":
                return "00"; // Register A -> 0
            case "X":
                return "01"; // Register X -> 1
            case "L":
                return "02"; // Register L -> 2
            case "B":
                return "03"; // Register B -> 3
            case "S":
                return "04"; // Register S -> 4
            case "T":
                return "05"; // Register T -> 5
            case "F":
                return "06"; // Register F -> 6
            default:
                return "00"; // Default case if register is unknown
        }
    }
}
