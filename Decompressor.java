import java.io.*;
import java.util.zip.*;
import java.util.Scanner;

/**
 * Decompressor class is used for converting an a 12-bit (two byte) chunk ASCII or binary file back to its original 8-bit (single byte) format.
 * Receives input file and performs byte-by-byte operations to write back to 8-bit (single byte) chunks.
 * Used as introduction to LZWCompression algorithm.
 */
public class Decompressor {

	/**
	 * main() method of program.
	 * Receives from command line input file name and output file name.
	 * Utilizes byte-by-byte manipulation to convert 12-bit chunked data from Compressor class back to original 8-bit format.
	 */
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter file to decompress:");
		String input = scanner.nextLine();
		System.out.println("Enter output file name:");
		String output = scanner.nextLine();
		
		byte inbyte1 = 0;
		byte inbyte2 = 0;
		byte inbyte3 = 0;
		boolean readlast = false;
		
		DataInputStream read = null;
		DataOutputStream out = null;
		
		
		try{
			read = new DataInputStream(new BufferedInputStream(
				new FileInputStream(input)));
			out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(output)));
			
			inbyte1 = read.readByte();
			inbyte2 = read.readByte();
			readlast = true;
			inbyte3 = read.readByte();

			//continuous loop to read input bytes
			while(true){
			readlast = false;
			byte[] buffer = new byte[2];

			//read in three 8-bit (two 12-bit) sequence
			int in1 = new Byte(inbyte1).intValue();
			if(in1 < 0){
				in1 += 256;
			}
			int in2 = new Byte(inbyte2).intValue();

			if(in2 < 0){
				in2 += 256;
			}
			int in3 = new Byte(inbyte3).intValue();

			if(in3 < 0){
				in3 += 256;
			}

				//calls unexpByte method to convert three 8-bit (single byte) values to two 12-bit (3 byte) array
				buffer = unexpByte(in1, in2, in3);
			
			for(int i = 0; i < buffer.length; i++){
				out.writeByte(buffer[i]);
			}
			
			inbyte1 = read.readByte();
			inbyte2 = read.readByte();
			readlast = true;
			inbyte3 = read.readByte();
			}
			
		}  catch(EOFException e){
			//if end of file occurs after only having read two single byte (8-bit) values
			//must convert to 12-bit and write to file
			if(readlast == true){
				byte[] buffer = new byte[1];
				
				int in1 = new Byte(inbyte1).intValue();
				if(in1 < 0){
					in1 += 256;
				}
				int in2 = new Byte(inbyte2).intValue();

				if(in2 < 0){
					in2 += 256;
				}
				
				buffer = unexpByte(in1, in2);
			
				for(int i = 0; i < buffer.length; i++){
					try{
						out.writeByte(buffer[i]);
					} catch (FileNotFoundException f){
						f.printStackTrace();
					} catch (IOException f){
						f.printStackTrace();
					}
				}
			}
			
			System.out.println("End of file.");
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			if (read != null) {
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * unexpByte() method receives three 8-bit integer values from two 12-bit encoded values and converts to two-byte array
	 * Pre-Condition: integers received must be from three 8-bit sequence designed to encode two 12-bit values.
	 * Post-Condition: returns two-byte array.
	 */
	public static byte[] unexpByte(int i, int j, int k){
		byte[] output = new byte[2];
		
		String num1 = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
		String num2 = String.format("%8s", Integer.toBinaryString(j)).replace(' ', '0');
		String num3 = String.format("%8s", Integer.toBinaryString(k)).replace(' ', '0');
		
		String out1 = num1.substring(4,8) + num2.substring(0,4);
		String out2 = num3.substring(0,8);
		
		output[0] = (byte) Integer.parseInt(out1.substring(0,8),2);
		output[1] = (byte) Integer.parseInt(out2.substring(0,8),2);
		
		return output;
	}

	/**
	 * unexpByte() method receives two 8-bit integer values from one 12-bit encoded value and converts to single-byte array
	 * Pre-Condition: integers received must be from two 8-bit sequence designed to encode one 12-bit value.
	 * Post-Condition: returns single-byte array.
	 */
	public static byte[] unexpByte(int i, int j){
		byte[] output = new byte[1];
		
		String num1 = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
		String num2 = String.format("%8s", Integer.toBinaryString(j)).replace(' ', '0');
						
		String out1 = num1.substring(4,8) + num2.substring(0,4);
		
		output[0] = (byte) Integer.parseInt(out1.substring(0,8),2);
		
		return output;
	}
}
