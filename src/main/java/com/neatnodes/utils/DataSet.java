package com.neatnodes.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * A set of input/output data representing a given function. A DataSet can be used to test Genomes for how well they 
 * implement the function it represents.
 * 
 * @author Sam Griffin
 */
public class DataSet {
	private final int inputs; //the number of inputs for the function represented by this dataset
	private final int outputs; //the number of outputs for the function represented by this dataset
	private List<Double[]> entries = new ArrayList<Double[]>(); //stores the dataset itself
	private boolean hasWeights;
		
	/**
	 * Creates a DataSet from the data in a CSV file. Each row is validated to make sure it contains values for 
	 * all specified inputs and outputs. The expected format for a CSV file is as follows:
	 * <pre>
	 * - There must be at least 1 input column and at least 1 output column
	 * - All input columns must come before all output columns
	 * - You can add an optional weight column as the last column
	 * - All rows must have numeric values for each header
	 * - All rows must align with the columns defined by the header row
	 * </pre>
	 * @param pathToCSV
	 * 		The path to the CSV file.
	 */
	public DataSet(String pathToCSV){
	    int inputNumber = 0;
	    int outputNumber = 0;
	    boolean weightsFound = false;
	    
		CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(pathToCSV)).withCSVParser(parser).build()){
			String [] nextLine = reader.readNext();
			if (nextLine == null || nextLine.length < 1) {
				throw new RuntimeException("No CSV header row found");
			}
			
			//parse the header row
			for(int i = 0; i < nextLine.length; i ++) {
				//if a weight column is included, it must be the last column
				if(weightsFound) {
					throw new RuntimeException("CSV headers are named incorrectly.");
				}
			
				//input columns must come first
				if(inputNumber == 0 && !nextLine[i].equals("input")) {
					throw new RuntimeException("CSV headers are named incorrectly.");
				}
			
				//output columns must come after all input columns
				if(outputNumber > 0 && !nextLine[i].equals("output") && !nextLine[i].equals("weight")) {
					throw new RuntimeException("CSV headers are named incorrectly.");
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
					throw new RuntimeException("CSV headers are named incorrectly.");
				}
			}
			
			if(inputNumber == 0 || outputNumber == 0) {
				throw new RuntimeException("CSV headers are named incorrectly. Both inputs and outputs must be provided.");
			}
			
			//parse the data rows
		    while ((nextLine = reader.readNext()) != null) {
		    	if(weightsFound) {
			    	if((inputNumber + outputNumber + 1) != nextLine.length) {
			    		throw new RuntimeException("CSV file is not formatted correctly. Entries do not align with column headers.");
			    	}
			        this.entries.add(new Double[ inputNumber + outputNumber + 1]);
		    	}
		    	else {
			    	if((inputNumber + outputNumber) != nextLine.length) {
			    		throw new RuntimeException("CSV file is not formatted correctly. Entries do not align with column headers.");
			    	}
			        this.entries.add(new Double[ inputNumber + outputNumber ]);
		    	}
		        for(int i = 0; i < nextLine.length; i ++) {
		        	try {
		        		this.entries.get(entries.size() - 1)[i] = Double.parseDouble(nextLine[i]);
		        	}
		        	catch(NumberFormatException e) {
		        		throw new RuntimeException("CSV file is not formatted correctly. All values must be numbers and cannot be blank.");
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
	
	/**
	 * Get the number of inputs for the function represented by the DataSet
	 * @return
	 * 		The number of inputs
	 */
	public int getInputNumber() {
		return this.inputs;
	}

	/**
	 * Get the number of outputs for the function represented by the DataSet
	 * @return
	 * 		The number of outputs
	 */
	public int getOutputNumber() {
		return this.outputs;
	}
	
	/**
	 * Get the number of rows in the DataSet
	 * @return
	 * 		The number of data rows.
	 */
	public int getNumberOfEntries() {
		return this.entries.size();
	}
	
	/**
	 * Check whether the DataSet includes a weight column.
	 * @return
	 * 		True if the DataSet has a weight column.
	 */
	public boolean isWeighted() {
		return this.hasWeights;
	}

	/**
	 * Get the input values for a specific row. Returns null if the provided row number is invalid.
	 * @param rowNumber
	 * 		The row to retrieve the inputs from. Rows are indexed starting from 0.
	 * @return
	 * 		The array of input values.
	 */
	public Double[] getInputsForRow(int rowNumber) {
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
	
	/**
	 * Get the output values for a specific row. Returns null if the provided row number is invalid.
	 * @param rowNumber
	 * 		The row to retrieve the outputs from. Rows are indexed starting from 0.
	 * @return
	 * 		The array of output values.
	 */
	public Double[] getOutputsForRow(int rowNumber) {
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
	
	/**
	 * Get the weight for a specific row. Returns null if the provided row number is invalid or if the DataSet 
	 * does not have weights.
	 * @param rowNumber
	 * 		The row to retrieve the weight from. Rows are indexed starting from 0.
	 * @return
	 * 		The weight.
	 */
	public Double getWeightForRow(int rowNumber) {
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
