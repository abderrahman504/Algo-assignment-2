package com.example.Decompressor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.MyBufferedWriter;

public class Decompressor {
    

    public void decompress(String filePath) throws IOException
    {
        if (!filePath.substring(filePath.length()-3).equals(".hc"))
        {
            System.out.println("Invalid file extension. Should end with .hc\nAborting...");
            return;
        }
        String[] pathSplit = filePath.split("[\\/]");
        String fName = pathSplit[pathSplit.length-1];
        StringBuilder outPath = new StringBuilder();
        outPath.append(filePath.substring(0, filePath.length()-fName.length()));
        outPath.append("extracted.").append(fName.substring(0, fName.length()-3));
        start(filePath, outPath.toString());
    }


    void start(String inputPath, String outputPath) throws IOException
    {
        int[] unitLen = {0};
        HashMap<String, List<Byte>> codes = new HashMap<String, List<Byte>>();
        // FileInputStream reader = new FileInputStream(inputPath);
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(inputPath));
        List<List<Byte>> leftoverBytesCont = new ArrayList<List<Byte>>(1);
        long[] fileSize = new long[1];
        extractMetadata(reader, codes, unitLen, leftoverBytesCont, fileSize);
        //Start decompressing data
        MyBufferedWriter writer = new MyBufferedWriter(new FileOutputStream(outputPath));
        writeDecompressedFile(reader, writer, codes, unitLen[0], leftoverBytesCont.get(0), fileSize[0]);
    }


    /*
    Metadata Structure (Remember no newlines):
    <unit length (4b)><no. of units(4b)><leftover length(4b)><leftover bytes><file size(8b)><code length storage size(1b)>
    <unit><code bit length(1-4b)><code>
    <unit><code bit length(1-4b)><code>
    <unit><code bit length(1-4b)><code>
    ...
    */
    void extractMetadata(BufferedInputStream reader, HashMap<String, List<Byte>> codes, int[] unitLen, List<List<Byte>> leftoverBytesCont, long[] fileSize) throws IOException
    {
        //Read unit length
        byte[] bytes = reader.readNBytes(4);
        unitLen[0] = 0;
        for (byte b : bytes)
            unitLen[0] = (unitLen[0] << 8) | (b & 0xFF);
        //Read number of units
        bytes = reader.readNBytes(4);
        int noOfUnits = 0;
        for (byte b : bytes)
            noOfUnits = (noOfUnits << 8) | (b & 0xFF);
        //Read leftover length
        bytes = reader.readNBytes(4);
        int leftoverLen = 0;
        for (byte b : bytes)
            leftoverLen = (leftoverLen << 8) | (b & 0xFF);
        //Read leftover bytes
        bytes = reader.readNBytes(leftoverLen);
        List<Byte> leftoverBytes = new ArrayList<Byte>(leftoverLen);
        for (var b : bytes)
            leftoverBytes.add(b);
        leftoverBytesCont.add(leftoverBytes);
        //Read file size
        bytes = reader.readNBytes(8);
        for (byte b : bytes)
            fileSize[0] = (fileSize[0] << 8) | (b & 0xFF);
        //read code length storage size
        int codeLenStore = (byte)reader.read();
        //Read codes
        for (int i = 0; i < noOfUnits; i++)
        {
            //Read unit bytes
            bytes = reader.readNBytes(unitLen[0]);
            List<Byte> unit = new ArrayList<Byte>(unitLen[0]);
            for (var b : bytes)
            unit.add(b);
            //Read code length
            bytes = reader.readNBytes(codeLenStore);
            int codeLen = 0;
            for (byte b : bytes) codeLen = (codeLen << 8) | (b & 0xFF);
            //Read code bytes
            bytes = reader.readNBytes(Math.ceilDiv(codeLen, 8));
            StringBuilder codeBuilder = new StringBuilder();
            for(byte b : bytes)
            {
                codeBuilder.append(byteToString(b));
            }
            //trim the excess bits from the end
            if(codeLen % 8 != 0) codeBuilder.delete(codeBuilder.length()-8+codeLen%8, codeBuilder.length());
            
            codes.put(codeBuilder.toString(), unit);
        }
        return;
    }

    void writeDecompressedFile(BufferedInputStream reader, MyBufferedWriter writer, HashMap<String, List<Byte>> codes, int unitLen, List<Byte> leftoverBytes, long fileSize) throws IOException
    {
        int minCodeLen = -1;
        for(var entry : codes.entrySet())
        {
            if(minCodeLen == -1) minCodeLen = entry.getKey().length();
            else minCodeLen = Math.min(minCodeLen, entry.getKey().length());
        }
        
        StringBuilder leftoverCode = new StringBuilder("");
        long bytesWritten = 0;
        while(true)
        {
            //if leftoverCode isn't empty then check if any substring from the start is a code
            if (leftoverCode.length() >= minCodeLen)//Check if leftover contains valid code
            {
                String validCode = extractValidCode(leftoverCode, minCodeLen, codes);
                while(validCode.length() != 0 && bytesWritten != fileSize-leftoverBytes.size())
                {
                    List<Byte> unit = codes.get(validCode);
                    if(unit == null)
                    {
                        System.out.println("Code not found in codes. This shouldn't happen.");
                        return;
                    }
                    //Write unit to file
                    for (var b : unit)
                        writer.write(b);
                    bytesWritten += unitLen;
                    validCode = extractValidCode(leftoverCode, minCodeLen, codes);
                }
            }
            
            //Read some bytes and put them in leftoverCode
            byte[] bytes = reader.readNBytes(500);
            if (bytes.length == 0)//If end of file was reached
            {
                if (leftoverCode.length() != 0 && Integer.parseInt(leftoverCode.toString(), 2) != 0)
                    System.out.println("Finished decompressed file but have some leftover code. This shouldn't happen.");
                break;
            }
            //Append read bytes to leftoverCode
            for (var b : bytes)
            {
                String codeByte = byteToString(b);
                for(int i=0; i<8-codeByte.length(); i++) leftoverCode.append('0');
                leftoverCode.append(codeByte);
            }
        }

        //Write leftover bytes
        for (var b : leftoverBytes)
            writer.write(b);
        writer.forceWrite();
        writer.close();
        reader.close();
    }

    String extractValidCode(StringBuilder string, int minCodeLen, HashMap<String, List<Byte>> codes)
    {
        for (int i=minCodeLen; i<=string.length(); i++)
        {
            String code = string.substring(0, i);
            if (codes.containsKey(code))
            {
                string.delete(0, i);
                return code;
            }
        }
        return "";
    }



    String byteToString(byte b)
    {
        StringBuilder str = new StringBuilder("");
        if (b >= 0)
        {
            str.append('0');
            b = (byte)(b | 0b10000000);
            str.append(Integer.toBinaryString(b & 0xFF).substring(1));
        }
        else{
            str.append(Integer.toBinaryString(b & 0xFF));
        }
        return str.toString();
    }
}
