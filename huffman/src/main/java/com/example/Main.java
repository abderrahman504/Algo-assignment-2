package com.example;

import java.io.IOException;

import com.example.Compressor.Compressor;
import com.example.Decompressor.Decompressor;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0){
            try{

                new Compressor().compress("gbbct10.seq", 1);
                new Decompressor().decompress("19015906.1.gbbct10.seq.hc");
                // new Compressor().compress("img1.jpg", 1);
                // new Decompressor().decompress("19015906.1.img1.jpg.hc");
            }
            catch (IOException e){
                System.out.println("IO exception!");
            }
            return;
        }
        else if (args.length != 3 && args.length != 2){
            System.out.println("Invalid number of commandline arguments!");
            System.out.println("Usage:");
            System.out.println("java -jar huffman.jar c <input file> <unit size>");
            System.out.println("java -jar huffman.jar d <input file>");
            return;
        }
        char mode = args[0].charAt(0);
        if (args.length == 3)
        {
            String targetPath = args[1];
            int unitSize = Integer.parseInt(args[2]);
            if (mode == 'c')
            {
                try {
                    long start = System.currentTimeMillis();
                    new Compressor().compress(targetPath, unitSize);
                    long time = System.currentTimeMillis() - start;
                    System.out.println("Compression time = "+time+" ms");
                } catch (IOException e) {
                    System.out.println("IO exception during compression");
                }
                return;
            }

        }
        else if (args.length == 2)
        {
            String targetPath = args[1];
            if (mode == 'd')
            {
                try {
                    long start = System.currentTimeMillis();
                    new Decompressor().decompress(targetPath);
                    long time = System.currentTimeMillis() - start;
                    System.out.println("Decompression time = "+time+" ms");
                    
                } catch (Exception e) {
                    System.out.println("IO exception during decompression!");
                }
                return;
            }
        }
    }
}