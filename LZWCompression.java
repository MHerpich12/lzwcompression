import java.io.*;
import java.util.*;

/**
 * LZWCompression class houses main() function call of program used to complete LZW Compression Algorithm on either
 * ASCII or binary file.  Can be run from command line (eg, java LZWCompression c input.txt output.txt) or directly
 * from Java editor.  Takes input file, either ASCII or binary, and performs 12-bit LZW Compression algorithm.  Writes
 * compressed file to output in 8-bit (single byte) chunks.  Ability to utilize either a HashMap or TreeMap in compression.
 * For decompression, takes input file, either ASCII or binary, and performs 12-bit LZW Decompression, reading in 8-bit
 * (single byte) chunks.  LZW Algorithm performs best with significant redundancies within data.  For more information,
 * see https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Welch.
 *
 * Java class includes three input files: words.html, CrimeLatLonXY1990.csv, and Overview.mp4.  Results are as follows:
 *
 * words.html: original data, 2,436KB; compressed data, 1,045KB; 57% compression
 * CrimeLatLonXY1990.csv: original data, 270KB; compressed data, 133KB; 51% compression
 * Overview.mp4: original data, 24,423KB; compressed data, 32,982; (35%) compression
 *
 * Comparison of TreeMap versus HashMap for input files:
 * (C) words.html: TreeMap, 1.912 sec; HashMap, 1.549 sec;
 * (C) CrimeLatLonXY1990.xsv: TreeMap, 0.503 sec; HashMap, 0.415 sec;
 * (C) Overview.mp4: TreeMap, 28.183 sec; HashMap, 20.916 sec;
 *
 * (D) words.html: TreeMap, 1.746 sec; HashMap, 1.697 sec;
 * (D) CrimeLatLonXY1990.xsv: TreeMap, 0.415 sec; HashMap, 0.446 sec;
 * (D) Overview.mp4: TreeMap, 32.827 sec; HashMap, 32.454 sec;
 */
public class LZWCompression {

	/**
	 * LZWCompression class instance variables
	 * HashMap<String, Integer> = HashMap object for compression
	 * TreeMap<String, Intger> = TreeMap object for compression (only one of Hash or Tree necessary)
	 * String[] decomp = String array used for decompression
	 */
	//private HashMap<String, Integer> table = new HashMap<String, Integer>();
	private TreeMap<String, Integer> table = new TreeMap<String, Integer>();
    private String[] decomp;

	/**
	 * LZWCompression() constructor.
	 * Initializes either HashMap or TreeMap table with 0 - 255 bit characters
	 * Initializes decomp String (for decompression) with 0 - 255 bit characters
	 */
    public LZWCompression(){
		//Enter all symbols into table, hash map
    	for(int i = 0; i < 256; i++){
    		table.put(Character.toString((char) i), i); 
    	}
    	
    	//enter all symbols into table, array
    	decomp = new String[4096];
    	for(int l = 0; l < 256; l++){
    		decomp[l] = Character.toString((char) l);
    	}
    			
    }

	/**
	 * createTable() method creates a new table (either HashMap or TreeMap) to prevent 12-bit overflow during compression.
	 */
    //public HashMap<String, Integer> createTable(){
    public TreeMap<String, Integer> createTable(){
    	//HashMap<String, Integer> newtable = new HashMap<String, Integer>();
    	TreeMap<String, Integer> newtable = new TreeMap<String, Integer>();
    	//Enter all symbols into table
    	for(int i = 0; i < 256; i++){
    		newtable.put(Character.toString((char) i), i); 
    	}
    	return newtable;
    }

	/**
	 * createArray() method creates a new String[] array(to prevent 12-bit overflow during decompression.
	 */
    public String[] createArray(){
    	String[] newstring = new String[4096];
    	for(int l = 0; l < 256; l++){
    		newstring[l] = Character.toString((char) l);
    	}
    	return newstring;
    }

	/**
	 * LZWCompress() main method to execute LZW Compression Algorithm on input file.
	 * String input: input file, either ASCII or binary
	 * String output: output file, either ASCII or binary
	 * Post-Condition: outputs LZW compressed file to working directory.
	 */
    public void LZWCompress(String input, String output){
    	
    	byte inbyte1 = 0;
    	byte inbyte2 = 0;
    	DataInputStream read = null;
		DataOutputStream out = null;
		int index;
		boolean firstval = true;
		byte[] buffer = new byte[3];
		String s = "";
    	
  	   	index = 256;
    	
    	try{
    		read = new DataInputStream(new BufferedInputStream(
    				new FileInputStream(input)));
    		out = new DataOutputStream(new BufferedOutputStream(
    				new FileOutputStream(output)));
    		
    		//read(first character from w into string s)
    		inbyte1 = read.readByte();
    		
    		char sc = (char) (inbyte1 & 0xFF);
    		s = "" + sc;
    		
    		while(true){
    			//read character c
    			inbyte2 = read.readByte();
    			
    			char c = (char) (inbyte2 & 0xFF);
    			
    			//if s+c in table
    			if(table.containsKey(s + c)){
    				//s = s + c
    				s = s + c;
    			} else {
    				//output codeword
    				//get 12-bit binary string
    				String outstring = this.expByte(table.get(s));
    				
    				//store either in first/second or second/third spot in 3-byte buffer
    				if(firstval){
    					buffer[0] = (byte) Integer.parseInt(outstring.substring(0,8),2);
    					buffer[1] = (byte) Integer.parseInt(outstring.substring(8,12) + "0000",2); 
    				} else {
    					buffer[1] += (byte) Integer.parseInt(outstring.substring(0,4),2); 
    					buffer[2] = (byte) Integer.parseInt(outstring.substring(4,12),2);
    					
    					//if buffer full, write to file
    					for(int j = 0; j < 3; j++){
    						out.writeByte(buffer[j]);
    						buffer[j] = 0;
    					}
    				}
    				
    				firstval = !firstval;
    				
    				//enter s + c into table
    				//if overflow, create new table
    				if(index < 4096){
    					table.put(s + c, index);
    					index += 1;
    				} else {
    					this.table = createTable();
    					index = 256;
    					table.put(s + c,  index);
    					index += 1;
    				}
    				//s = c
    				s = "" + c;
    			}
    		}
			
    	} catch(EOFException e){
    		
    		//if end of file, output codeword
    		String outstring = this.expByte(table.get(s));
			
    		if(firstval){
				buffer[0] = (byte) Integer.parseInt(outstring.substring(0,8),2);
				buffer[1] = (byte) Integer.parseInt(outstring.substring(8,12) + "0000",2);
				
				for (int k = 0; k < 2; k++){
					try{
						out.writeByte(buffer[k]);
					} catch (FileNotFoundException f){
						f.printStackTrace();
					} catch (IOException f){
						f.printStackTrace();
					}
				}
				
			} else {
				buffer[1] += (byte) Integer.parseInt(outstring.substring(0,4),2); 
				buffer[2] = (byte) Integer.parseInt(outstring.substring(4,12),2);
				
				for (int k = 0; k < 3; k++){
					try{
						out.writeByte(buffer[k]);
						buffer[k] = 0;
					} catch (FileNotFoundException f){
						f.printStackTrace();
					} catch (IOException f){
						f.printStackTrace();
					}
				}
			}
    		System.out.println("End of file.");
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
	 * LZWDecompress() main method to execute LZW Decompression Algorithm on input file.
	 * String input: input file, either ASCII or binary
	 * String output: output file, either ASCII or binary
	 * Post-Condition: outputs LZW decompressed file to working directory.
	 */
    public void LZWDecompress(String input, String output){
		byte[] buffer = new byte[3];
    	DataInputStream read = null;
		DataOutputStream out = null;
		int index;
		boolean firstval = true;
		int priorkey;
		int currkey;
		
		index = 256;
		
		try{
			read = new DataInputStream(new BufferedInputStream(
    				new FileInputStream(input)));
    		out = new DataOutputStream(new BufferedOutputStream(
    				new FileOutputStream(output)));
    		
    		buffer[0] = read.readByte();
    		buffer[1] = read.readByte();
    		
    		//read prior codeword and output corresponding value
    		priorkey = unexpByte(buffer[0],buffer[1],firstval);
    		
    		out.writeBytes(decomp[priorkey]);
    		firstval = !firstval;
    		
    		while(true){
    			//read codeword
    			if(firstval){
    				buffer[0] = read.readByte();
    				buffer[1] = read.readByte();
    				currkey = unexpByte(buffer[0],buffer[1],firstval);
    			} else {
    				buffer[2] = read.readByte();
    				currkey = unexpByte(buffer[1],buffer[2],firstval);
    			}
    			firstval = !firstval;
    			
    			if(currkey >= index){
    				//enter string (priorcodeword) + firstchar(string(priorcodeword)) into table
    				String inval = decomp[priorkey] + (char) (decomp[priorkey].charAt(0) & 0xFF);
    				
    				//reset to prevent overflow
    				if(index == 4096){
    					this.decomp = createArray();
    					index = 256;
    				}
    				    				
    				decomp[index] = inval;
    				
    				//output string(priorcodeword) + firstchar(string(priorcodeword))
    				out.writeBytes(decomp[index]);
    				index += 1;
    			} else {
    				
    				//enter string (priorcodeword) + firstchar(string(codeword)) into table
    				String inval = decomp[priorkey] + (char) (decomp[currkey].charAt(0) & 0xFF);
    				
    				//reset to prevent overflow
    				if(index == 4096){
    					this.decomp = createArray();
    					index = 256;
    				}
    				
   					decomp[index] = inval;
    				
    				//output string associated with codeword
    				out.writeBytes(decomp[currkey]);
    				index += 1;
    			}
    			//priorcodeword = codeword
    			priorkey = currkey;
    		}
    		
		} catch(EOFException e){
    		System.out.println("End of file.");
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
	 * expByte() method converts integer to 12-bit binary string representation.
	 * Utilized for LZW compression algorithm.
	 */
    public String expByte(int i){
		String output = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
		
		while(output.length() < 12){
			output = "0" + output;
		}
		
		return output;
	}

	/**
	 * unexpByte() method returns 12-bit binary string representation based on two 8-bit (single byte) sequences.
	 * Byte firstbyte: first byte represented in 12-bit binary string (8-bit).
	 * Byte secondbyte: second byte represented in 12-bit binary string (8-bit).
	 * Boolean first: utilized to determine which 12 bits to use from 16-bit (2-byte) sequence.
	 * Utilized for LZW decompression algorithm.
	 */
    public int unexpByte(byte firstbyte, byte secondbyte, boolean first){
		
    	String num1 = String.format("%8s", Integer.toBinaryString(firstbyte)).replace(' ', '0');
		String num2 = String.format("%8s", Integer.toBinaryString(secondbyte)).replace(' ', '0');

		//use only number bits if sign extended
		if(num1.length() > 8){
			num1 = num1.substring(num1.length() - 8,num1.length());
		}
		
		if(num2.length() > 8){
			num2 = num2.substring(num2.length() - 8, num2.length());
		}
		
		//for testing
		//System.out.println(num1);
		//System.out.println(num2);
		
		int outval;
		
		if(first){
			outval = Integer.parseInt(num1.substring(0,8) + num2.substring(0,4),2);
		} else { 
			outval = Integer.parseInt(num1.substring(4,8) + num2.substring(0,8),2);
		}
    	return outval;
    	}

	/**
	 * main() method of program.
	 * Can be executed from command line utilizing syntax:
	 * java LZWCompression c input.txt output.txt OR
	 * java LZWComrpession d input.txt output.txt
	 * Can also be run from standard Java IDE
	 * Outputs time taken so as to compare between data structures (HashMap or TreeMap).
	 */
	public static void main(String[] args) {
		
		if(args.length == 0){
			Scanner scanner = new Scanner(System.in);
			System.out.println("What do you want to do (d/c)?");
			String selection = scanner.nextLine();
		
			if(selection.equals("c")){
				System.out.println("Enter file to compress:");
				String input = scanner.nextLine();
				System.out.println("Enter output file name:");
				String output = scanner.nextLine();
				System.out.println("Compressing " + input + " to " + output);
				long startTime = System.nanoTime();
				LZWCompression newCompress = new LZWCompression();
				newCompress.LZWCompress(input, output);
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Runtime in seconds for algorithm: " + totalTime/1000000000.0);
			} else if(selection.equals("d")){
				System.out.println("Enter file to decompress:");
				String input = scanner.nextLine();
				System.out.println("Enter output file name:");
				String output = scanner.nextLine();
				System.out.println("Decompressing " + input + " to " + output);
				long startTime = System.nanoTime();
				LZWCompression newCompress = new LZWCompression();
				newCompress.LZWDecompress(input, output);
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Runtime in seconds for algorithm: " + totalTime/1000000000.0);
			} else {
				System.out.println("Invalid command.  Please enter either c for compress or d for decompress.");
			}
		} else {
			if(args[0].equals("c")){
				LZWCompression newCompress = new LZWCompression();
				System.out.println("Compressing " + args[1] + " to " + args[2]);
				long startTime = System.nanoTime();
				newCompress.LZWCompress(args[1], args[2]);
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Runtime in seconds for algorithm: " + totalTime/1000000000.0);
			} else if(args[0].equals("d")){
				LZWCompression newCompress = new LZWCompression();
				System.out.println("Decompressing " + args[1] + " to " + args[2]);
				long startTime = System.nanoTime();
				newCompress.LZWDecompress(args[1], args[2]);
				long endTime   = System.nanoTime();
				long totalTime = endTime - startTime;
				System.out.println("Runtime in seconds for algorithm: " + totalTime/1000000000.0);
			} else {
				System.out.println("Invalid command.  Please enter either c for compress or d for decompress.");
			}
		}
	}

	
}
