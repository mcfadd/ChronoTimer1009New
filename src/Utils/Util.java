package Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import entitiesDynamic.Racer;
import environment.Run.Race;

/**
 * @author Andy
 * The Util class, apart of the Utils package, is the Utility class for the ChronoTimer,
 * is resposible for processing input files, Exporting Runs among other things.
 */
public class Util {

	/**
	 * @author Andy
	 * The ProcessFile class, an inner class of Util, is responsible for generating a stack of commands.
	 */
	private static class ProcessFile{

		private static Stack<String> commands = new Stack<>();
		private static Stack<String> times = new Stack<>();

		/**
		 * Processes commands from a file and adds them to a queue which is used by makeCommands()
		 * @see ProcessFile#makeCommands().
		 */
		private static void commandProcessing(){

			String[] splitted = fileContent.toString().split("\\s");
			

			for(int i = splitted.length - 1; i >= 0; i--){

				if(!splitted[i].equals("") && !splitted[i].equals(" ") && (!splitted[i].contains(":"))){


					if(splitted[i].contains("TIME")){


						commands.push(splitted[i + 1].replace("\\n", "").replace("\\r", "").replace("\\b", "").replace("\\d", "").trim());
						commands.push(splitted[i].replace("\\n", "").replace("\\r", "").replace("\\b", "").replace("\\d", "").trim());

					}else{

						commands.push(splitted[i].replace("\\n", "").replace("\\r", "").replace("\\b", "").replace("\\d", "").trim());

					}
				}else{
					
					times.push(splitted[i].trim());
					
				}
			}
			

			makeCommands();
			
		}
		
		
		
		private static void makeCommands(){

			Deque<String> temp = new LinkedList<>();
			StringBuilder sb = new StringBuilder();
			
			
			while(!commands.isEmpty()){     
				
				sb.append(commands.pop().trim());

				if(sb.toString().equals("TIME") || sb.toString().equals("TRIG") || sb.toString().equals("EVENT") || sb.toString().equals("NUM") || sb.toString().equals("TOG")
						|| sb.toString().equals("PRINT") || sb.toString().equals("EXPORT")){

					sb.append(" " + commands.pop());

				}else if(sb.toString().equals("CONN")){

					sb.append(" " + commands.pop() + " " + commands.pop());

				}

				temp.add(sb.toString());

				sb.setLength(0);
				sb = new StringBuilder();

			}

			while(!temp.isEmpty()){

				commands.push(temp.removeLast());

			}
		}
	}


	private static StringBuilder fileContent;

	/**
	 * @param filename
	 * Opens and reads a file, and returns the contents as one String.
	 */
	public static void readFileAsString(String filename) {

		if(fileContent != null){

			fileContent.setLength(0);

		}

		fileContent = new StringBuilder();

		try{

			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;

			while ((line = reader.readLine()) != null) {

				fileContent.append(line + "\n");

			}

			reader.close();

		
		}catch(IOException ex){	}
		
		
	}
	
	public static String getContent() {
		return fileContent.toString();
	}
	
	public static void buildCommands(String path) {
		readFileAsString(path);
		ProcessFile.commandProcessing();
		
	}
	public static String getTimeCommand(){
		return ProcessFile.times.isEmpty() ? null : ProcessFile.times.pop();
	}

	/**
	 * @return the next Command as a String
	 */
	public static String getNextCommand(){
		return ProcessFile.commands.isEmpty() ? null : ProcessFile.commands.pop();
	}

	/**
	 * @return true if there are more commands
	 */
	public static boolean areCommandIssued(){
		return !ProcessFile.commands.isEmpty();
	}

	/**
	 * @param fileName
	 * @param race
	 * @return
	 * @throws IOException
	 * Creates a File named fileName and saves the Race paramter 'race' to it in Json format.
	 * (See EXPORT command documentation) 
	 */
	public static boolean save(String fileName, boolean ioState, Race...race)throws IOException{

		if(race == null || race.length == 0){

			return false;
		}

		fileName = System.getProperty("user.dir") + fileName;

		for(int i = 0; i < race.length; i++){

			if(race[i] == null){

				continue;
			}


				try{

					AccessController.checkPermission(new FilePermission(System.getProperty("user.dir"), "read,write"));

				}catch(SecurityException e){
					
					if(!ioState){
					
					System.out.print("Please run as admin or write the full path: ");
					Scanner tempScan = new Scanner(System.in);
					fileName = tempScan.nextLine();
					tempScan.close();
					}
					
				}

			File tmp = new File(fileName);
			tmp.setWritable(true);
			
			Printer.printToConsole("Saved in " + fileName + "\n");


			if(!tmp.exists()){
				tmp.createNewFile();
			}
			

			BufferedWriter bw = new BufferedWriter(new FileWriter(tmp.getAbsolutePath(), true));

			Gson g = new Gson();

			Iterator<Racer> it = race[i].getRecord();

			while(it.hasNext()){
				bw.write(g.toJson(it.next()));
			}
			
			bw.flush();
			bw.close();

		}

		return true;
	}

	/**
	 * @throws IOException
	 */
	public static void clear() throws IOException {
		File tmp = new File(System.getProperty("user.dir"), "json.txt");
		tmp.delete();
		tmp.createNewFile();
	}

}