package com.neatnodes.neatnodes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class DataSet {
	private final int inputs; //the number of inputs for the function represented by this dataset
	private final int outputs; //the number of outputs for the function represented by this dataset
	private List<Double[]> entries = new ArrayList<Double[]>(); //stores the dataset itself
	
	//takes a path to a CSV file and loads the data in it. Each row is validated to make sure it contains values for all specified inputs and outputs.
	public DataSet(String pathToCSV) throws DataFormatException{
	    int inputNumber = 0;
	    int outputNumber = 0;
		try {
			final CSVParser parser =
					new CSVParserBuilder()
					.withSeparator(',')
					.withIgnoreQuotations(true)
					.build();
			final CSVReader reader =
					new CSVReaderBuilder(new FileReader(pathToCSV))
					.withCSVParser(parser)
					.build();
			
		     String [] nextLine = reader.readNext();
		     if (nextLine == null || nextLine.length < 1) {
		    	 throw new DataFormatException("No CSV header row found");
		     }
		     
		     //parse the header row
		     for(int i = 0; i < nextLine.length; i ++) {
		    	 if(inputNumber == 0 && !nextLine[i].equals("input")) {
		    		 throw new DataFormatException("CSV headers are named incorrectly.");
		    	 }
		    	 
		    	 if(outputNumber > 0 && !nextLine[i].equals("output")) {
		    		 throw new DataFormatException("CSV headers are named incorrectly.");
		    	 }
		    	 
		    	 if(nextLine[i].equals("input")) {
		    		 inputNumber ++;
		    	 }
		    	 else if(nextLine[i].equals("output")) {
		    		 outputNumber ++;
		    	 }
		    	 else {
		    		 throw new DataFormatException("CSV headers are named incorrectly.");
		    	 }
		     }

		     if(inputNumber == 0 || outputNumber == 0) {
	    		 throw new DataFormatException("CSV headers are named incorrectly. Both inputs and outputs must be provided.");
		     }
		     
		     //parse the data rows
		     while ((nextLine = reader.readNext()) != null) {
		        this.entries.add(new Double[ inputNumber + outputNumber ]);
		        for(int i = 0; i < nextLine.length; i ++) {
		        	try {
		        		this.entries.get(entries.size() - 1)[i] = Double.parseDouble(nextLine[i]);
		        	}
		        	catch(NumberFormatException e) {
		        		System.out.println(nextLine[i]);
		        		throw new DataFormatException("CSV file is not formatted correctly. All values must be numbers and cannot be blank.");
		        	}
		        }
		     }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			this.inputs = inputNumber;
			this.outputs = outputNumber;
		}
	}
	
	public int getInputNumber() {
		return this.inputs;
	}

	public int getOutputNumber() {
		return this.outputs;
	}
	
	public int getNumberOfEntries() {
		return entries.size();
	}

	public Double[] getInputsForRow(int rowNumber) {
		Double[] row = this.entries.get(rowNumber);
		Double[] returnValue = new Double[this.inputs];
		for(int i = 0; i < this.inputs; i ++) {
			returnValue[i] = row[i];
		}
		return returnValue;
	}
	
	public Double[] getOutputsForRow(int rowNumber) {
		Double[] row = this.entries.get(rowNumber);
		Double[] returnValue = new Double[this.outputs];
		for(int i = 0; i < this.outputs; i ++) {
			returnValue[i] = row[i + this.inputs];
		}
		return returnValue;
	}

	public static void main(String args[]) {
		
		try {
			DataSet ds = new DataSet("test.csv");
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
