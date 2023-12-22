package com.example;

import java.io.FileOutputStream;
import java.io.IOException;

public class MyBufferedWriter {
    private FileOutputStream writer;
    private byte[] buffer = new byte[10000];
    private int bufferSize = 0;

    public MyBufferedWriter(FileOutputStream writer)
    {
        this.writer = writer;
    }

    public void write(byte b) throws IOException
    {
        if (bufferSize + 1 >= buffer.length)
        {
            byte[] toWrite = new byte[bufferSize];
            System.arraycopy(buffer, 0, toWrite, 0, bufferSize);
            writer.write(toWrite);
            bufferSize = 0;
            buffer = new byte[10000];
        }
        buffer[bufferSize++] = b;
    }

    public void write(byte[] b) throws IOException
    {
        if (bufferSize + b.length >= buffer.length)
        {
            byte[] toWrite = new byte[bufferSize];
            System.arraycopy(buffer, 0, toWrite, 0, bufferSize);
            writer.write(toWrite);
            bufferSize = 0;
            buffer = new byte[10000];
        }
        System.arraycopy(b, 0, buffer, bufferSize, b.length);
        bufferSize += b.length;
    }

    public void writeShort(short x) throws IOException
    {
        byte[] shortBytes = new byte[2];
        int j=0;
        for (int i=1; i>=0; i--)
        {
            shortBytes[j++] = (byte)(x>>(8*i) & 0xFF);
        }
        write(shortBytes);
    }
    
    public void writeInt(int x) throws IOException
    {
        byte[] intBytes = new byte[4];
        int j=0;
        for (int i=3; i>=0; i--)
        {
            intBytes[j++] = (byte)(x>>(8*i) & 0xFF);
        }
        write(intBytes);
    }

    public void writeLong(long x) throws IOException
    {
        byte[] longBytes = new byte[8];
        int j=0;
        for (int i=7; i>=0; i--)
        {
            longBytes[j++] = (byte)(x>>(8*i) & 0xFF);
        }
        write(longBytes);
    }

    public void forceWrite() throws IOException
    {
        byte[] toWrite = new byte[bufferSize];
        System.arraycopy(buffer, 0, toWrite, 0, bufferSize);
        writer.write(toWrite);
        bufferSize = 0;
        buffer = new byte[10000];
    }

    public void close() throws IOException
    {
        writer.close();
    }
}


