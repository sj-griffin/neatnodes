---
layout: default
---
# Neat Nodes

Neat Nodes is an open-source Java library that implements a machine learning algorithm called NeuroEvolution of Augmenting Topologies (NEAT). The full specification for the algorithm is available in the **[original research paper](http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf)**. The algorithm is capable of teaching itself simple tasks without being shown how.

This project is intended as a teaching and learning tool rather than a production-ready machine learning library. To this end, it includes visualisation capabilities to help with understanding what the algorithm is doing and what it produces. 

## What is it?

NEAT is a genetic machine learning algorithm. The idea is that you give it a set of inputs and expected outputs, and it will independently teach itself a function that produces those results. For example if I wanted to teach it how to add numbers, I could tell it that the inputs 1 and 1 produce the output 2. The inputs 3 and 4 produce the output 7, etc. Based on this information it will then go away and figure out how to add numbers. I can then tell it to add a set of inputs it has never seen before, like 36 and 92, and it will know that the answer is 128.

Since computers can already add numbers, this example isn’t particularly useful. However, machine learning becomes extremely powerful when we apply it to more complex problems. For example if we use pictures of animals as inputs and their names as outputs, we can teach a program to identify animals. We can already identify animals ourselves, but we can’t articulate how we do it to a machine. Using machine learning, we can make a machine do things without having to explain how to do them. This opens up a whole new world of potential applications for computers that were previously impossible.

## How does it work?

NEAT is based on the principles of evolution. When you give it a set of input/output data, it starts with a group of potential functions that it thinks might produce the right results. It then tests each function against the data to see if it produces similar results, and gives it a score based on how accurate it was. After a round of testing, the functions with the highest scores get to ‘mate’ with each other and produce offspring functions that take on the traits of their parents. Functions with low scores get culled and do not reproduce. Eventually, after many generations of this, a function emerges that matches the data very closely. The algorithm has essentially taught itself to do whatever operation the data represents.

## How does it work in detail?

This section summarises the key elements of the NEAT algorithm.  It is helpful to have a basic understanding of neural networks first. If you would like further details, you can refer to the original research paper or to the source code itself.

### Genomes
A genome is a simple neural network that can compute a specific function. It is made up of a set of nodes and a set of connections that link the nodes together and transfer data between them. Each node holds a value and can be either an input, an output, a hidden node, or a bias node.

A connection has the following properties:
* An in-node (the node that it will transfer data from)
* An out-node (a node that it will transfer data to)
* A weight (a multiplier that gets applied to data passing through the link)
* An enable bit (determines whether the link is enabled or not)
* An innovation number (explained below)

Connections can come from any node but can only end on output or hidden nodes.

![genome-example.png](/images/genome-example.png)

In the screenshot, we can see values on the connections indicating their weights. The green nodes, are inputs, the red nodes are outputs, the yellow nodes are hidden nodes, and the purple node is a bias node. Hidden nodes are simply nodes that are not inputs or outputs. A bias node is a special node that always has a value of 1 on it. The inclusion of a bias node in a genome ensures that it will always be able to create any possible value by multiplication, even when all of it’s inputs are zero. This makes genomes significantly more versatile and able to compute a wider variety of functions. In this implementation, genomes always have exactly one bias node.

When a function is run, data starts on the input nodes and flows through the function via connections until it reaches the output nodes. Functions are run in steps, with each step making every connection in the genome fire once and transfer the data from it’s in-node to it’s out-node. Each node applies a sigmoid function to the data it receives from connections in order to generate a new value that it can then pass to it’s outgoing connections on the next step.  When you want to run a genome, you place data on it’s input nodes, run it for a set number of steps, and then read the values on the output nodes.

### The Algorithm
When you run the NEAT algorithm, you provide it with a data set that represents the function you want it to learn. A data set tells the algorithm what outputs you expect to get when you give the function certain inputs. The algorithm can use this information to test genomes and see how well they implement the function you are aiming for.

The algorithm starts with a population of identical genomes with input and output nodes to match the number of inputs and outputs expected by the data set. It then runs for a preset number of generations.

At each generation, genomes mate to produce offspring which inherit the traits of their parent genomes and also exhibit random mutations. Each member of the new generation is tested against the data set to determine how well it implements the goal function. Based on the results, it receives a score called it’s fitness. After every genome has been tested, the genomes with the lowest fitness are culled from the population, and the remaining genomes breed with each other to produce the next generation. In this way, the most effective genomes rise to the top, and you end up with genomes that get better and better at computing the goal function with each generation. After the set number of generations has passed, the genome with the greatest fitness is chosen as the result of the algorithm.

### Species
At each generation, genomes are divided up into species based on how similar they are to each other. When culling is done, genomes will only be measured against the other genomes in their species. By only allowing genomes that operate in the same niche to compete with each other, we allow them to optimise what they do best without being wiped out by more effective genomes that take a completely different approach. This way, we get different genomes taking different approaches to solving the problem, which gives us a better chance of arriving at the most effective approach.

Any two genomes can be compared to each other to get the compatibility distance between them. The higher the compatibility distance, the less evolutionary history they share. Compatibility distance is used to determine which species each genome should be sorted into.

The algorithm has a configurable value called the maximum compatibility distance which determines how closely related the members of each species will be to each other. For each new generation, a random genome is picked from each of the species of the previous generation to represent that species in the current generation. If a genome is within the maximum compatibility distance of a representative, it is sorted into that species. A genome can only be part of one species. If it does not fit into any species, a new species will be started to accommodate it.

### Reproduction
At each generation, the members of each species will breed with each other to produce the genomes for the next generation. Although genomes of different species are protected from direct competition with each other, species as a whole still compete with other species. The amount of offspring that each species will get to produce is proportional to it’s average fitness. Species that are fitter relative to other species will get to produce more of the following generation. As a result, fitter species tend to grow over time to make up more of the total population, while unfit species tend to shrink.

When a species is bred, it gets to produce a certain number of offspring. A configurable proportion of that number will come from randomly chosen genomes in the species breeding with each other, and the remaining number will come from random genomes in the species breeding with themselves.

When two genomes breed, the algorithm determines which elements (genes) of each genome correspond to which elements in their partner by using their innovation numbers (explained below). During reproduction, a gene is randomly picked from each pair of matching genes, and all the genes without a match are included from the fitter parent. If both parents are equally fit, they are inherited randomly. If a gene is disabled in either parent, there's a preset chance that it will be disabled in the offspring.

### Mutations
At each generation, there are two types of structural mutation which can occur:
    • Add connection: Two previously unconnected nodes are connected with a random weight.
    • Add node: An existing connection is disabled and replaced by a new node with two new connections.  The connection leading into the new node is weighted 1, and the connection leading out is given the weight of the old connection.
The weights of each connection can also mutate on a random basis.
Whenever a new connection appears, it is given an innovation number as an identifier. There is a class called an InnovationManager that is responsible for allocating these numbers. It’s goal is to ensure that if the same connection forms independently in different genomes from the same generation, that connection will be given the same innovation number in each genome. This ensures that connections in different genomes can be matched to each other for breeding purposes. Innovation numbers are passed down to the same genes in the next generation, so that a record is always kept of where connections originated.

For more detailed information, refer to the **[original research paper](http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf)** and to the source code on **[GitHub](https://github.com/sj-griffin/neatnodes)**.

## What Next?

To get started using the library, check out the **[tutorial](/tutorial)** page.