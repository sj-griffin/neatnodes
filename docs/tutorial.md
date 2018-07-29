---
layout: default
---
# Tutorial

## Installation

To get started, download the latest release here. Make sure you have an appropriate Java development environment set up. NeatNodes requires Java 8 or higher to run. Extract the downloaded package and add the provided jar file to your class path. By default, the jar searches for dependencies in the lib directory that is also provided, so you must keep the jar file and the lib directory together or add the lib directory to your class path. By default, the jar also searches for style sheets in the provided styles directory, but you can change this setting if needed. For more information, see the **[configuration](#configuration-files)** section.

If you are working with the source code directly, you can use Maven for dependency management using the provided pom.xml file.

## Creating Genomes

The NEAT algorithm is built around the concept of genomes. In this context, a genome is a neural network that implements a specific function. It is made up of nodes and connections. See the **[overview](/index.html)** page for more information.

The goal of the NEAT algorithm is to produce a genome that implements a given function as accurately as possible. However, the library also provides the capability to create and edit your own genomes manually.

```java
import com.neatnodes.genome.*;
import com.neatnodes.algorithm.InnovationManager;

InnovationManager iManager = new InnovationManager();
Genome genome = new Genome(iManager);
```

The above code creates a new Genome object with a single bias node. All Genomes contain a bias node.

An InnovationManager is an object that tracks mutations across many Genomes and allocates identifying labels for new connections. It is used by the NEAT algorithm. For our purposes, all we need to know is that we must provide one when creating new Genomes.

Once we have a genome, we can add some nodes to it. We will create a genome that computes the XOR function. XOR is a function that takes two binary inputs and returns 1 if they are different, or 0 if they are the same.

```java
genome.addNode(1, Node.INPUT);
genome.addNode(2, Node.INPUT);
genome.addNode(3, Node.OUTPUT);
genome.addNode(4, Node.HIDDEN);
```

When adding a node, we provide a label that will be used to refer to the node, and a node type. A valid label is a positive integer. Labels must be unique, so you cannot use the same one twice in the same genome. The bias node that is included in the genome by default has the label 0. Valid node types are Node.INPUT, Node.OUTPUT, and Node.HIDDEN. There is also a Node.BIAS type, but you cannot add this type of node because each genome is limited to one bias node only.

The number of input nodes in your genome determines how many inputs the function it represents will take. The same applies to output nodes.

Now that we have nodes, we can link them together with connections. This combination of connections creates a genome that is quite effective at computing the XOR function.

```java
genome.addConnection(0, 4, -0.36501593036936775, true, 1);
genome.addConnection(1, 4, -1.046399730632218, true, 2);
genome.addConnection(0, 3, 0.556513492344894, true, 3);
genome.addConnection(4, 3, -0.48188502205686023, true, 4);
genome.addConnection(2, 4, -0.576176328388935, true, 5);
genome.addConnection(1, 3, 0.8995301845901448, true, 6);
genome.addConnection(2, 3, -1.00442116184059, true, 7);
genome.addConnection(3, 3, -1.0283375490454472, true, 8);
genome.addConnection(3, 4, 2.0252790846258084, true, 9);
genome.addConnection(4, 4, 0.24926976625922484, true, 10);
```

When adding a connection, the first two arguments are the labels of the nodes to connect. The connection will run from the first of the two nodes to the second. The labels must match nodes that already exist in the genome.

The third argument is the weight of the connection. This is a multiplier that will be applied to values travelling through the link.

The fourth argument specifies whether the connection will be enabled or not. Disabled connections do not carry any data until they are enabled again.

The final argument is called the innovation number. It is the equivalent of the label for nodes- a positive integer that uniquely identifies the connection in the genome.

We now have a genome with nodes and connections that can be run to compute some function. You can retrieve the Node and Connection objects in the Genome with genome.getNodeGenes() and genome.getConnectionGenes(). For more information on working with Genomes, Nodes, and Connections, see the **[API reference](/javadoc/index.html)**.

## Running Genomes

Once you have a Genome, you can run it to compute the function it represents.

```java
import com.neatnodes.algorithm.Simulation;

Double[] inputs = {1.0, 0.0};
Double[] outputs = Simulation.runFunction(genome, inputs, 2);
```

This will give the genome the inputs 1 and 0, then run it for 2 steps and read the values from its output nodes. For more information on how the number of steps affects results, see the **[tuning](#tuning-tips)** section.

Since our genome only has a single output node, there will be a single result in the outputs array. When we check this result, we can see that it is 0.977147347431532 - very close to the result of 1 that we would expect our XOR function to produce.

## Saving and loading Genomes

The library provides the ability to save and load genomes to and from the file system so that you can reuse your results later. Genomes are saved in JSON format.

```java
import com.neatnodes.utils.JSONTools;
JSONTools.writeGenomeToFile(genome, "C:/genomes/saved_genome.json", "XOR genome");
```

The code above will save the genome as a file called saved_genome.json at the specified path. The third argument is a comment that will be added as a JSON field that you can use for recording metadata about the genome. Note that when saving genomes, connections that are disabled will be ignored and won’t appear in the file, as they have no effect on the behaviour of the genome.

```java
Genome loadedGenome = JSONTools.readGenomeFromFile("C:/genomes/saved_genome.json");
```

This method will load the saved genome from the JSON file and create a new Genome object from it.

## Creating a data set

A data set is a set of training data used to train the program to compute a specific function. It defines input values and their corresponding output values. As a simple example, here is a sample of a data set for teaching addition.

| Input | Input | Output |
| :---: | :---: | :---: |
| 1 | 1 | 2 |
| 1 | 2 | 3 |
| 2 | 1 | 3 |
| 2 | 2 | 4 |

The first row of data tells the program that if it receives input values of 1 and 1, it should produce the output value 2, and so on for each subsequent row.  We can use data sets like this one to tell the program what we want it to do, so that it can create a function that will produce these results. The idea is that after showing the program this data set, you could then give it a pair of inputs that don’t appear in the data and have it produce the correct output (e.g. 4 + 4 = 8). Note that this particular data set is only used as an example and would require further processing before actually being fed to the algorithm for reasons explained below.

The NEAT algorithm always requires a data set to run. To create a data set, you create a CSV file containing your data and then load it into your program. There are some sample CSV files available in the datasets directory in the release package. Here is the contents of AND.csv:

| input | input | output | weight |
| :---: | :---: | :---: | :---: |
| 0 | 0 | 0 | 1 |
| 1 | 1 | 1 | 1 |
| 0 | 1 | 0 | 1 |
| 1 | 0 | 0 | 1 |

This file defines the binary AND function. The function takes 2 inputs and produces 1 output. The file also contains an additional column called weight. Weight is an optional column that you can choose to add if you want greater control over how the algorithm is trained. It defines how important each row of data is when training the algorithm. When testing a potential function for it’s fitness, a row with a weight of 2 contributes twice as much to the fitness as a row with a weight of 1. A row of weight 3 would be considered 3 times as important when assessing fitness, and so on. If you do not add a weight column, all rows will have the default weight of 1.

When creating CSV files, you must adhere to the following rules:
    • There must be at least 1 input column and at least 1 output column
    • All input columns must come before all output columns
    • You can add an optional weight column as the last column
    • All cells must have entries
    • The column headers must be either ‘input’, ‘output’, or ‘weight’.

Once you have created a CSV file containing your data set, the next step is to load it into your program. The DataSet class is provided to do this. You simply provide it with the path to your CSV file.

```java
import com.neatnodes.utils.DataSet;
DataSet dataset = new DataSet("./datasets/AND.csv");
```

A DataSet object contains all the data from a CSV data set and can be passed to other functions provided by the library.

An important point to consider when creating data sets is that genomes expect node values to be between -1 and 1. Any values outside this range will in effect be capped at -1 or 1 because of the nature of how nodes process data. For binary logic functions this is no problem, but for other problems it means that you may need to find other ways of representing your data so that you can create a data set with values in the accepted range. The addition.csv file in the datasets directory provides an example of this. This data set represents a function that adds numbers with results between -100 and 100. In order to fit the data within the range -1 to 1, the values have been divided by 100. If we look at the first row, the inputs -1 and 0.1 with the output -0.9 actually represent the sum -100 + 10 = -90. It is simple enough to write your own pre and post-processing methods to convert the data into whatever format you require.

## Testing Genomes

We have seen how to run a genome a single time, but how do we test it against a whole data set? We can use the testFitness() method to find out how fit our genome is for computing a function represented by a data set. Using the genome we defined above:

```java
DataSet dataset = new DataSet("./datasets/XOR.csv");
double fitness = Simulation.testFitness(genome, dataset, 3);
```

This will test the genome against the data set, running the genome for 3 cycles each time before reading off the result. It returns a score out of 100 that represents how fit the genome is for reproducing the data set (in other words, how well it implements the desired function).

In this case, we can see it returns a score of … This tells us that the genome implements the XOR function with a high degree of accuracy.

## Configuration files

You can use a configuration file to change the parameters of the NEAT algorithm and other program settings. A sample configuration file is provided in the release package- sample_config.txt. You can use this as a template for your own configuration file and change the values as required. Configuration files use the Java property file format.

To load a configuration file into your program, use the Configuration class and provide it with the path to your file.

```java
import com.neatnodes.utils.Configuration;
Configuration configuration = new Configuration("./sample_config.txt");
```

A Configuration object contains all the parameters from a configuration file and can be passed to other functions provided by the library.

It is not necessary to provide every available parameter in a configuration file. Any parameters that are not found in the configuration file will be given their default value. If you do not want to use a configuration file at all and simply use the defaults for everything, you can create a Configuration object without giving it a path.

```java
Configuration configuration = new Configuration();
```

This Configuration object will contain the default values for all parameters.

## Running a simulation

Once you have a DataSet object and a Configuration object, you are ready to run a full simulation.

```java
import com.neatnodes.algorithm.Simulation;
import com.neatnodes.genome.Genome;
Genome result = Simulation.runSimulation(dataset, configuration, false);
```

This will run the full NEAT algorithm to produce a Genome that implements the function defined by your data set as closely as possible. The false argument indicates that the simulation will run at normal speed without visualisation. For more information see the **[visualisation](#visualisation)** section.

The simulation’s progress will be written to stdout as it runs. When the algorithm is complete, it will return a Genome object. A sample of the results produced by the genome and it’s fitness as a score out of 100 can also be seen on stdout.

![simulation-output.png](/images/simulation-output.png)

Note that simulations take longer to run with larger data sets as there are more results to test. The DEPTH and POPULATION_SIZE parameters will also affect the time it takes to run each generation.

## Tuning Tips

While the default parameters work well enough in many scenarios, often, you will have to tune the algorithm parameters a bit to get the best results for the task you are trying to teach. There are some important concepts to understand in order to improve your results.

### Randomness of results
Because the algorithm uses randomness, you can run it multiple times with the same parameters and get different results. If you are getting poor results, it is worth running the algorithm a few more times with the same parameters to see if a fitter group of genomes evolves.

### Generations
You can change the GENERATIONS parameter in your configuration file to control the number of generations that the simulation will run for. In general, the more generations a simulation runs for, the longer it has to evolve effective genomes, and the better results it will produce. However, you might also want to lower the number of generations. If the algorithm is producing effective genomes for a function in just a few generations, then more generations might only serve to make genomes more complex without improving fitness much.

### Depth
The DEPTH parameter in the configuration file controls how many steps genomes will be run for before a result is read from their output nodes. This parameter can have a dramatic effect on the algorithm results. Essentially it dictates how complex the genomes will be allowed to get. For example, if a genome only runs for 3 steps, then adding elements to it that perform a 4th step won’t improve it’s fitness, because that 4th step will never be reached. As a result, the algorithm will produce genomes optimised for 3 steps. If the function you are trying to teach the algorithm is a complex one with more than a few steps, you will want to raise the depth to allow more complex genomes to form. Of course, if you are trying to teach a simple function, then a higher depth will make the algorithm less effective, as it will try to use complex solutions to solve a simple problem. Also note that higher depths take longer to simulate. The greater the depth, the longer the simulation will take to run.

### Compatibility
The COMPATIBILITY_THRESHOLD parameter in the configuration file controls how closely the genomes in each species will be related to each other. A higher compatibility threshold means that genomes with less in common will be grouped together in the same species. If you set the threshold higher, you can increase the amount of genetic diversity available in each species, resulting in different breeding combinations and different genomes evolving. You should experiment with this value for the needs of the function you are trying to teach.

### Plateaus
There are some functions which will consistently hit a “plateau” where species do not evolve past a certain fitness level no matter how many generations you run the algorithm for. This is more likely to happen for complex functions. It occurs because the algorithm is greedy- it cannot predict that an element that doesn’t currently add anything to the fitness might eventually form part of a larger structure that adds a lot to the fitness. As a result, you can get situations where the genomes that would have best implemented the function die off early because they were overtaken by other genomes that made quick fitness gains at the expense of actually implementing the function effectively. In these situations, you may have to experiment with changes to the parameters to see what is most effective. It may be that you don’t have a high enough depth for the complexity of the function, 

### Easing
Easing can be a useful technique for breaking through plateaus and general improvements in how quickly effective genomes evolve. Easing is the practice of adding rows to the data set so that it forms a more obvious trend line. As an example, take the data set for the XOR function. XOR is a function that takes two binary inputs and returns 1 if they are different, or 0 if they are the same.

| input | input | output |
| :---: | :---: | :---: |
| 0 | 0 | 0 |
| 1 | 0 | 1 |
| 0 | 1 | 1 |
| 1 | 1 | 0 |

When we try to run the algorithm on this data set, we will see that it often creates genomes that get 2 or 3 of the results correct, but is dead wrong on the others. By plotting this data on a 3-dimensional graph, we can see why.

![easing-graph-1.png](/images/easing-graph-1.png)

What we have here is a group of points without a clear relationship. This makes it difficult for a cohesive function to evolve. The algorithm will try things at random, usually settling on an approach that gets good results for some of the data but is wrong for the other rows.

Now let’s try adding some additional rows to the data set to create a clearer pattern between the points we’ve already plotted. Since these results are not actually expected from the XOR function, we add a weight column and give them half the weight of the results that are actually important to us.

| input | input | output | weight |
| :---: | :---: | :---: | :---: |
| 0 | 0 | 0 | 2 |
| 1 | 0 | 1 | 2 |
| 0 | 1 | 1 | 2 |
| 1 | 1 | 0 | 2 |
| 0.5 | 0.5 | 0.5 | 1 |
| 0.25 | 0.75 | 0.75 | 1 |
| 0.25 | 0.25 | 0.25 | 1 |
| 0.75 | 0.25 | 0.75 | 1 |
| 0.75 | 0.25 | 0.75 | 1 |
| 0.75 | 0.75 | 0.25 | 1 |

If we add the new rows to our graph, we can see a clear lines emerging. 

![easing-graph-2.png](/images/easing-graph-2.png)

We have created a continuous function, rather than a series of unrelated results. The algorithm can now work towards implementing a pattern, rather than trying things that work for some results but not others.

Essentially we have created a data set that implements an “extended XOR” function. It still produces the results you would expect from the usual XOR function, but is far easier for the algorithm to learn.

## Visualisation

The library also includes visualisation capabilities built on the **[GraphStream](http://graphstream-project.org/)** library. Before doing any visualisation, you should make sure that you have your styles set up properly. The library uses CSS files to determine how to display genomes, similar to how web pages are styled. A set of style sheets is included in the release package in the styles directory. As long as this directory is in the same directory as the neatnodes jar file, the library will find the styles without further configuration. However, you can also customise the location that the library looks for style sheets by setting the STYLE_PATH parameter in your configuration file. Note that you must provide a full file path, not a relative path.

To view a genome, use the viewGenome() function.

```java
Simulation.viewGenome(genome, configuration);
```

This will open a window displaying the genome. You can re-position nodes by dragging them with the mouse. Nodes are colour coded so that you can see what type they are. Bias nodes are purple, input nodes are green, output nodes are red, and hidden nodes are yellow.

![view-genome.png](/images/view-genome.png)

By changing the RENDER_STYLE parameter in your configuration file, you can change how the genome is displayed. The parameter takes the name of style sheet to use. By default it uses the “normal” style, but other styles are also provided in the default styles directory (“minimal”, “stars”, and “glow”). You can also write your own custom style sheets. For more information, refer to the the **[GraphStream CSS reference](http://graphstream-project.org/doc/Advanced-Concepts/GraphStream-CSS-Reference/)**.

![styles-example.png](/images/styles-example.png)

Now that you have seen how to visualise a single genome, we will look at visualising a full simulation. When you run the NEAT algorithm in visualisation mode, a window will display each generation of genomes that are created as it runs. The simulation will be artificially slowed down to help make the visuals easier to follow.

Note that visualising a full simulation is a much more intensive task than visualising single genomes, as there can be thousands of genomes on screen at once. Ideally you should have a multi-core processor and graphics card to run visual simulations. This feature is intended as a visual aid for understanding the NEAT algorithm. For running large simulations you should turn visualisation mode off, as performance will degrade with the size of the simulation. In visualisation mode, you should consider changing your configuration file to lower the number of generations, the depth, and the initial population size for better results.

To run a simulation in visualisation mode, simply set the visualisation flag to true when starting the simulation.

```java
Simulation.runSimulation(dataset, configuration, true);
```

The visualisation shows the surviving genomes in each generation as they evolve.

![view-simulation.png](/images/view-simulation.png)

While the visualisation is running, you can drag the canvas with the mouse to move around, and zoom in and out using your mouse wheel. This way you can view individual genomes in detail. Note that dragging nodes around is disabled in this mode.

## What Next?

If you want to learn more, you can check out the **[API reference](/javadoc/index.html)** or view the source code on **[GitHub](https://github.com/sj-griffin/neatnodes)**.