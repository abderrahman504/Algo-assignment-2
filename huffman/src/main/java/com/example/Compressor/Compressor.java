package com.example.Compressor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.MyBufferedWriter;

public class Compressor {
    

    public void compress(String filePath, int unitSize) throws IOException
    {
        List<List<Byte>> leftoverUnitCont = new ArrayList<List<Byte>>(1);
        HashMap<List<Byte>, Integer> frequencies = new Huffman().getFrequencies(filePath, unitSize, leftoverUnitCont);
        HashMap<List<Byte>, String> codes = new Huffman().buildCodes(frequencies);
        
        long fileSize = 0;
        int longestCodeLen = 0;
        for(var entry : frequencies.entrySet())
            fileSize += entry.getValue();
        for(var entry : codes.entrySet())
            longestCodeLen = Math.max(longestCodeLen, codes.get(entry.getKey()).length());
        fileSize *= unitSize;
        fileSize += leftoverUnitCont.get(0).size();
        String[] pathSplit = filePath.split("[\\/]");
        String fName = pathSplit[pathSplit.length-1];
        StringBuilder outPath = new StringBuilder();
        outPath.append(filePath.substring(0, filePath.length()-fName.length()));
        outPath.append("19015906.").append(unitSize).append(".").append(fName).append(".hc");

        writeCompressedFile(codes, unitSize, leftoverUnitCont.get(0), fileSize, longestCodeLen, filePath, outPath.toString());
    }



    void writeCompressedFile(HashMap<List<Byte>, String> codes, int unitLen, List<Byte> leftoverUnit, long fileSize, int longestCodeLen, String inputPath, String outputPath) throws IOException
    {
        System.out.println("Original data size = "+ fileSize);
        // FileInputStream reader = new FileInputStream(inputPath);
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(inputPath));
        MyBufferedWriter myWriter = new MyBufferedWriter(new FileOutputStream(outputPath));
        writeMetadata(myWriter, unitLen, codes, leftoverUnit, fileSize, longestCodeLen);
        StringBuilder leftoverCode = new StringBuilder("");
        long bytesWritten = 0;
        Outer:
        while(true)
        {
            while (leftoverCode.length() < 8)
            {
                //Read unit and fill leftover with its code
                byte[] bytes = new byte[unitLen];
                int readRes = reader.read(bytes);
                if (readRes < unitLen)//End of file reached with left over code
                {
                    //pad leftover with zeroes and write to file
                    while(leftoverCode.length() < 8) leftoverCode.append('0');
                    byte[] byteToWrite = {(byte)(Integer.parseInt(leftoverCode.substring(0, 8), 2))};
                    myWriter.write(byteToWrite);
                    bytesWritten++;
                    break Outer;
                }
                List<Byte> unit = new ArrayList<Byte>(unitLen);
                for (var b : bytes)
                    unit.add(b);    
                leftoverCode.append(codes.get(unit));
            }
            try
            {
                byte[] byteToWrite = {(byte)(Integer.parseInt(leftoverCode.substring(0, 8), 2))};
                leftoverCode = new StringBuilder(leftoverCode.substring(8, leftoverCode.length()));
                myWriter.write(byteToWrite);
            } catch (NumberFormatException e)
            {
                System.out.println("Error in code: " + leftoverCode);
            }
            
            bytesWritten++;
        }
        System.out.println("Encoded data size = " + bytesWritten);
        myWriter.forceWrite();
        reader.close();
        myWriter.close();
    }
    
    /*
    Metadata Structure (Remember no newlines):
    <unit length (4b)><no. of units(4b)><leftover length(4b)><leftover bytes><file size(8b)><code length storage size(1b)>
    <unit><code bit length(1-4b)><code>
    <unit><code bit length(1-4b)><code>
    <unit><code bit length(1-4b)><code>
    ...
    */
    
    void writeMetadata(MyBufferedWriter writer, int unitLen, HashMap<List<Byte>, String> codes, List<Byte> leftoverUnit, long fileSize, int longestCodeLen) throws IOException
    {
        //Write unit length
        int n = unitLen;
        writer.writeInt(n);
        //Write number of units
        int noOfUnits = codes.size();
        writer.writeInt(noOfUnits);
        //Write leftover length
        int leftoverLen = leftoverUnit.size();
        writer.writeInt(leftoverLen);
        //Write leftover bytes
        byte[] leftover = new byte[leftoverLen];
        for(int i=0; i<leftoverLen; i++) leftover[i] = leftoverUnit.get(i);
        writer.write(leftover);
        //Write file size
        writer.writeLong(fileSize);
        //Determining how many bytes to store code length in
        byte codeLenStorageSize;
        if (longestCodeLen < 256)
            codeLenStorageSize = 1;
        else if(longestCodeLen < Short.MAX_VALUE*2 + 1)
            codeLenStorageSize = 2;
        else
            codeLenStorageSize = 4;
        writer.write(codeLenStorageSize);
        long metadataSize = 21 + leftoverLen;
        //Write codes
        for (var entry : codes.entrySet()) 
        {
            byte[] unit = new byte[unitLen];
            for(int i=0; i<unitLen; i++) unit[i] = entry.getKey().get(i);
            //Write unit bytes
            writer.write(unit);
            
            //Build code array
            byte[] code = stringToBytes(entry.getValue());
            //Write code length
            switch (codeLenStorageSize)
            {
                case 1:
                    writer.write((byte)entry.getValue().length());
                    break;
                case 2:
                    writer.writeShort((short)entry.getValue().length());
                    break;
                case 4:
                    writer.writeInt(entry.getValue().length());
                    break;
            }
            //Write code bytes
            writer.write(code);
            metadataSize += codeLenStorageSize + code.length + unitLen;
        }
        System.out.println("Metadata size = " + metadataSize);
    }
    
    byte[] stringToBytes(String str)
    {
        int codeLen = str.length();
        byte[] code = new byte[(int)Math.ceil(codeLen/8f)];
        for (int s=0, e=Math.min(8, codeLen); s<codeLen;)
        {
            String codeStr = str.substring(s, e);
            if (e-s == 8)
                code[s/8] = (byte)Integer.parseInt(codeStr, 2);
            else
                code[s/8] = (byte)(Integer.parseInt(codeStr, 2)<<(8-e+s));
            s+=8;
            e = Math.min(e+8, codeLen);
        }
        return code;
    }
    
}
