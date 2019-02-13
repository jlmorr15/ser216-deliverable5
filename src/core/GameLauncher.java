package core;

import java.util.Scanner;

public class GameLauncher {

    /**
     * Starts the application and asks which user interface is preferred, then launches the game with it.
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Would you like to play using the [G]raphical User Interface or the [T]ext User Interface?");
        Scanner s = new Scanner(System.in);
        String opt = "";
        do {
            System.out.print("Please enter your selection [G/T]: ");
            opt = s.next();
        } while(!opt.equalsIgnoreCase("G") && !opt.equalsIgnoreCase("T"));

        if(opt.equalsIgnoreCase("G")) {
            ui.Connect4GUI.main(args);
        } else {
            ui.Connect4TextConsole.main(args);
        }
    }
}
