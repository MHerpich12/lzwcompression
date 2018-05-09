import java.io.*;
import java.util.zip.*;
import java.util.Scanner;

/**
 * Compressor class is used for converting an input ASCII or binary file from 8-bit (single byte) chunks to 12-bit (two
 * byte) chunks.  Receives input file and performs byte-by-byte operations to write to 12-bit chunks concatenated in
 * three byte (24-bit) array sequences.  Used as introduction to LZWCompression algorithm.
 */
public class Compressor {

	/**
	 * main() method of program.
	 * Receives from command line input file name and output file name.
	 * Utilizes byte-by-byte manipulation to convert 8-bit (single byte) chunks into 12-bit chunks.  Writes two 12-bit
	 * sequences to every three bytes (24 bits) until fully converted.
	 */
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter file to compress:");
		String input = scanner.nextLine();
		System.out.println("Enter output file name:");
		String output = scanner.nextLine();

		byte inbyte1 = 0;
		byte inbyte2 = 0;
		boolean readlast = false;
		
		DataInputStream read = null;
		DataOutputStream out = null;
		
		
		try{
			read = new DataInputStream(new BufferedInputStream(
				new FileInputStream(input)));
			out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(output)));
			
			inbyte1 = read.readByte();
			readlast = true;
			inbyte2 = read.readByte();
			
			//continuous loop to read input bytes
			while(true){
			readlast = false;
			byte[] buffer = new byte[3];

			//reads two 8-bit (single byte) values from input
			int in1 = new Byte(inbyte1).intValue();
			if(in1 < 0){
				in1 += 256;
			}
			int in2 = new Byte(inbyte2).intValue();

			if(in2 < 0){
				in2 += 256;
			}
			
			//calls expByte method to convert two 8-bit (single byte) values to two 12-bit (3 byte) array
			String bytestring1 = Compressor.expByte(in1);
			String bytestring2 = Compressor.expByte(in2);

			//concatenate two 12-bit sequences to 24-bit sequence
			String combstring = bytestring1 + bytestring2;

			//convert integer value to byte and write 24-bit sequence to three 8-bit values
			buffer[0] = (byte) Integer.parseInt(combstring.substring(0,8),2);
			buffer[1] = (byte) Integer.parseInt(combstring.substring(8,16),2);
			buffer[2] = (byte) Integer.parseInt(combstring.substring(16,24),2);

			//writes 3 byte sequence consisting of two 12-bit values and padding
			for(int i = 0; i < buffer.length; i++){
				out.writeByte(buffer[i]);
			}
			
			inbyte1 = read.readByte();
			readlast = true;
			inbyte2 = read.readByte();
			}
			
		}  catch(EOFException e){
			//if end of file occurs after only having read one single byte (8-bit) value
			//must convert 8-bit value to 12-bit and write to file
			if(readlast == true){
				
				int in1 = new Byte(inbyte1).intValue();
				if(in1 < 0){
					in1 += 256;
				}
			
				String combstring = Compressor.expByte(in1);
				combstring = combstring + "0000";
				byte[] buffer = new byte[2];
				buffer[0] = (byte) Integer.parseInt(combstring.substring(0,8),2);
				buffer[1] = (byte) Integer.parseInt(combstring.substring(8,16),2);
			
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
	 * expByte() method receives integer value, converts to 8-bit binary string, and returns equivalent 12-bit array
	 * utilizing sufficient "0" as padding.
	 * Pre-Condition: integer received must be from 8-bit (single byte) sequence.
	 * Post-Condition: returns 12-bit binary sequence as String.
	 */
	public static String expByte(int i){
		String output = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
		
		while(output.length() < 12){
			output = "0" + output;
		}
		
		return output;
	}

}
