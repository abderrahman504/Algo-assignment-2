package com.example;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3 && args.length != 2)
        {
            System.out.println("Invalid number of commandline arguments!");
            System.out.println("Usage:");
            System.out.println("Usage: java -jar huffman.jar c <input file> <unit size>");
            System.out.println("Usage: java -jar huffman.jar d <input file>");
            return;
        }
        char mode = args[0].charAt(0);
        if (args.length == 3)
        {
            String targetPath = args[1];
            int unitSize = Integer.parseInt(args[2]);
            if (mode == 'c')
            {
                new Compressor().compress(targetPath, unitSize);
                return;
            }

        }
        else if (args.length == 4)
        {
            String targetPath = args[1];
            if (mode == 'd')
            {
                new Decompressor().decompress(targetPath);
                return;
            }
        }
    }
}