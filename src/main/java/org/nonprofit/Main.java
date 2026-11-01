package org.nonprofit;
import java.util.Scanner;
import org.nonprofit.bootstrap.ApplicationBootstrap;
/** Entry point: constructs dependencies then starts the replaceable console view. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        try(Scanner input=new Scanner(System.in)) {
            new ApplicationBootstrap().build(input).start();
        }
    }
}
