package com.ntt.tc.util;

/**
 * An FilterOutputStream which encodes octet stream.
 *
 * @version		1.0, 12 Mar 1997
 * @author		Yusuke Sasaki
 * @see			java.io.OutputStream
 */
import java.io.*;

public class Base64EncodeStream extends FilterOutputStream {
	private static final String ENCODE_TABLE =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final int PLAIN_BLOCK_LENGTH = 3;
	private static final String LS = System.getProperty("line.separator");
	
	private int blocksInALine = 20; // 80 letters in a line.
	
	private byte[] buffer;
	private int counter;
	private int blockCount;

	/**
	 * Constructs a new OutputStream which encodes a stream with base64.
	 *
	 * @param out Output stream which is to be encoded with base64.
	 */
	public Base64EncodeStream(OutputStream out) {
		super(out);
		buffer = new byte[PLAIN_BLOCK_LENGTH];
		counter = 0;
		blockCount = 0;
	}
	
	/**
	 * Constructs a Base64EncodeStream with appropriate blocks in a line.
	 * 
	 * @param	out	Output stream which is to be encoded with base64.
	 * @param	blocks	block's number in a line.
	 */
	public Base64EncodeStream(OutputStream out, int blocks) {
		this(out);
		blocksInALine = blocks;
	}

	/**
	 * encodes and writes a byte.
	 *
	 * @exception IOException if an I/O error occurs.
	 */
	public void write(int b) throws IOException {
		
		buffer[counter++] = (byte)b;
		if (counter == PLAIN_BLOCK_LENGTH) {
			counter = 0;
			out.write((byte)(ENCODE_TABLE.charAt( (buffer[0] >> 2) & 0x3f )));
			out.write((byte)(ENCODE_TABLE.charAt( ((buffer[0] << 4) | ((buffer[1] >> 4) & 0xf) ) & 0x3f )));
			out.write((byte)(ENCODE_TABLE.charAt( ((buffer[1] << 2) | ((buffer[2] >> 6) & 0x3) ) & 0x3f )));
			out.write((byte)(ENCODE_TABLE.charAt( buffer[2] & 0x3F )));
			blockCount++;
			if (blockCount == blocksInALine) {
				blockCount = 0;
				out.write(LS.getBytes());
			}
		}
	}
	public void write(byte[] b, int off, int len) throws IOException {
	
		for (int i = 0; i < len; i++)
			write((int)b[i + off]);
	}
	
	/**
	 * Flushes this stream. Some padding '=' may be put if remainder exists.
	 *
	 */
	public void flush() throws IOException {
	
		switch (counter) {
			case 1:
				out.write((byte)(ENCODE_TABLE.charAt( (buffer[0] >> 2) & 0x3f )));
				out.write((byte)(ENCODE_TABLE.charAt( (buffer[0] << 4 ) & 0x3f )));
				out.write((byte)'=');	// pad
				out.write((byte)'=');	// pad
				break;
				
			case 2:
				out.write((byte)(ENCODE_TABLE.charAt( (buffer[0] >> 2) & 0x3f)));
				out.write((byte)(ENCODE_TABLE.charAt( ((buffer[0]<<4) |((buffer[1]>>4)&0xf))&0x3f)));
				out.write((byte)(ENCODE_TABLE.charAt((buffer[1] << 2) & 0x3f )));
				out.write((byte)'=');	// pad
				break;
		}
		out.flush();
	}

}
