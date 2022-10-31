package com.ntt.tc.util;

/**
 * An FilterInputStream which decodes base64 encoded stream.
 *
 * @version		1.0, 12 Mar 1997
 * @author		Yusuke Sasaki
 * @see			java.io.InputStream
 */
import java.io.*;

public class Base64DecodeStream extends FilterInputStream {
	private static final byte[] DECODE_TABLE = {
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,62,-1,-1,-1,63,
		52,53,54,55,56,57,58,59,60,61,-1,-1,-1,-1,-1,-1, // '=' 61st
		-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,
		15,16,17,18,19,20,21,22,23,24,25,-1,-1,-1,-1,-1,
		-1,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
		41,42,43,44,45,46,47,48,49,50,51,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
		-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
	};
	
	private byte[] buffer;
	private int[] data;
	private int counter;
	private boolean end;
	
	/**
	 * constructs a new InputStream which decodes base64.
	 *
	 * @param in Input stream which should be encoded with base64.
	 */
	public Base64DecodeStream(InputStream in) {
		super(in);
		buffer = new byte[4];
		data = new int[3];
		counter = 0;
		end = false;
	}

	/**
	 * Reads the next decoded byte from this input stream.
	 * This behavior is the same as InputStream.
	 *
	 * @return	the next byte of data, or -1 if the end of the
	 *				stream is reached.
	 * @exception	IOException if an I/O error occurs.
	 */
	public int read() throws IOException {
		int result;
		int d;
		
		if (end) return -1;
		
		if (counter == 0) { // data に何もない
			d = 0;
			int hasIgnored = 0;
			while (d < 4) {
				int c = in.read();	// 新しく読み込む
				if (c == -1) break; // もうデータはなかった。
				else {
					byte data = DECODE_TABLE[c];
					if (data >= 0) {
						buffer[d++] = data;
						hasIgnored = 0;
					}
					else {
						if (hasIgnored == 2) break;
						// 三度無視が続くと終わりとみなす。
						hasIgnored++;
					}
				}
			}
			switch (d) {
				case 0:	// はじめから読めなかった。
					data[0] = -1;
					data[1] = -1;
					data[2] = -1;
					break;
				
				case 1:	// 1文字だけ読めた。(ありえない）
					data[0] = (buffer[0]<<2) & 255;
					data[1] = -1;
					data[2] = -1;
					System.out.println("Base64 Format Error.");
					break;
					
				case 2: // ２文字読めた。(pad 2 つ)
					data[0] = ((buffer[0]<<2) | (buffer[1]>>4)) & 255;
					data[1] = -1;
					data[2] = -1;
					break;
				case 3: // ３文字読めた。(pad 1 つ)
					data[0] = ((buffer[0]<<2) | (buffer[1]>>4)) & 255;
					data[1] = ((buffer[1]<<4) | (buffer[2]>>2)) & 255;
					data[2] = -1;
					break;
					
				case 4: // break せず、正常にデータをとった。
					data[0] = ((buffer[0]<<2) | (buffer[1]>>4)) & 255;
					data[1] = ((buffer[1]<<4) | (buffer[2]>>2)) & 255;
					data[2] = ((buffer[2]<<6) | buffer[3]) & 255;
			}
		}
		result = data[counter++];
		if (result == -1) end = true;
		if (counter == 3) counter = 0;
		
		return result;
	}
	
   /**
    * Reads up to len bytes of data from this input stream 
    * into an array of bytes.
    *
    * @param      b     the buffer into which the data is read.
    * @param      off   the start offset of the data.
    * @param      len   the maximum number of bytes read.
    * @return     the total number of bytes read into the buffer, or
    *             <code>-1</code> if there is no more data because the end of
    *             the stream has been reached.
    * @exception  IOException  if an I/O error occurs.
    * @see        ntt.security.Base64DecodeStream#read()
	*
	*/
	public int read(byte[] b, int off, int len) throws IOException {
		int c = 0;
		int i;
		
		for (i = 0; i < len; i++) {
			c = read();
			if (c < 0) break;
			b[i] = (byte)c;
		}
		if (i == 0 && c < 0) return -1;
		else return i;
	}
	
}
