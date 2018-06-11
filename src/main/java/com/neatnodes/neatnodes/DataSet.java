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

class DataSet {
	private final int inputs; //the number of inputs for the function represented by this dataset
	private final int outputs; //the number of outputs for the function represented by this dataset
	private List<Double[]> entries = new ArrayList<Double[]>(); //stores the dataset itself
	private boolean hasWeights;
	
	//takes a path to a CSV file and loads the data in it. Each row is validated to make sure it contains values for all specified inputs and outputs.
	//the expected format for a CSV file is as follows:
	//there must be at least 1 input column and at least 1 output column
	//all input columns must come before all output columns
	//you can add an optional weight column as the last column
	//all rows must have numeric values for each header
	//all rows must align with the header row
	protected DataSet(String pathToCSV) throws DataFormatException{
	    int inputNumber = 0;
	    int outputNumber = 0;
	    boolean weightsFound = false;
	    
		CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(pathToCSV)).withCSVParser(parser).build()){
			String [] nextLine = reader.readNext();
			if (nextLine == null || nextLine.length < 1) {
				throw new DataFormatException("No CSV header row found");
			}
			
			//parse the header row
			for(int i = 0; i < nextLine.length; i ++) {
				//if a weight column is included, it must be the last column
				if(weightsFound) {
					throw new DataFormatException("CSV headers are named incorrectly.");
				}
			
				//input columns must come first
				if(inputNumber == 0 && !nextLine[i].equals("input")) {
					throw new DataFormatException("CSV headers are named incorrectly.");
				}
			
				//output columns must come after all input columns
				if(outputNumber > 0 && !nextLine[i].equals("output") && !nextLine[i].equals("weight")) {
					throw new DataFormatException("CSV headers are named incorrectly.");
				}
			
				if(nextLine[i].equals("input")) {
					inputNumber ++;
				}
				else if(nextLine[i].equals("output")) {
					outputNumber ++;
				}
				else if(nextLine[i].equals("weight")) {
					weightsFound = true;
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
		    	if(weightsFound) {
			    	if((inputNumber + outputNumber + 1) != nextLine.length) {
			    		throw new DataFormatException("CSV file is not formatted correctly. Entries do not align with column headers.");
			    	}
			        this.entries.add(new Double[ inputNumber + outputNumber + 1]);
		    	}
		    	else {
			    	if((inputNumber + outputNumber) != nextLine.length) {
			    		throw new DataFormatException("CSV file is not formatted correctly. Entries do not align with column headers.");
			    	}
			        this.entries.add(new Double[ inputNumber + outputNumber ]);
		    	}
		        for(int i = 0; i < nextLine.length; i ++) {
		        	try {
		        		this.entries.get(entries.size() - 1)[i] = Double.parseDouble(nextLine[i]);
		        	}
		        	catch(NumberFormatException e) {
		        		throw new DataFormatException("CSV file is not formatted correctly. All values must be numbers and cannot be blank.");
		        	}
		        }
		    }
		    reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			this.inputs = inputNumber;
			this.outputs = outputNumber;
			this.hasWeights = weightsFound;
		}
	}
	
	protected int getInputNumber() {
		return this.inputs;
	}

	protected int getOutputNumber() {
		return this.outputs;
	}
	
	protected int getNumberOfEntries() {
		return this.entries.size();
	}
	
	protected boolean isWeighted() {
		return this.hasWeights;
	}

	//return an array of doubles representing the inputs in a given row
	//row numbers start from 0
	//returns null if the supplied row number is invalid
	protected Double[] getInputsForRow(int rowNumber) {
		Double[] row = null;
		try {
			row = this.entries.get(rowNumber);
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}
		Double[] returnValue = new Double[this.inputs];
		for(int i = 0; i < this.inputs; i ++) {
			returnValue[i] = row[i];
		}
		return returnValue;
	}
	
	//return an array of doubles representing the outputs in a given row
	//row numbers start from 0
	//returns null if the supplied row number is invalid
	protected Double[] getOutputsForRow(int rowNumber) {
		Double[] row = null;
		try {
			row = this.entries.get(rowNumber);
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}		
		Double[] returnValue = new Double[this.outputs];
		for(int i = 0; i < this.outputs; i ++) {
			returnValue[i] = row[i + this.inputs];
		}
		return returnValue;
	}
	
	//return a double representing the weight of a given row
	//row numbers start from 0
	//returns null if the supplied row number is invalid
	//returns null if the DataSet does not have weights
	protected Double getWeightForRow(int rowNumber) {
		if(!this.hasWeights) {
			return null;
		}
		Double[] row = null;
		try {
			row = this.entries.get(rowNumber);
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}		
		return row[this.inputs + this.outputs];
	}
}
