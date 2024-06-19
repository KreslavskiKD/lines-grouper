package ru.kirill.kreslavski.lines_grouper;

import ru.kirill.kreslavski.lines_grouper.grouper.Grouper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Required exactly one argument: file name");
            return;
        }

        String fileName = args[0];
        Grouper grouper = new Grouper();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(grouper::consume);
        } catch (IOException e) {
            System.out.println("Problem reading the file: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt", true))){

            BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));


            grouper.writeStats(log);


        } catch (IOException e) {
            System.out.println("Problem writing in file: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}
