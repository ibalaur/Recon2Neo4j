# Recon2Neo4j

The Recon2Neo4j Framework uses the neo4j graph platform for the management of data on human metabolism. Specifically, it facilitates i) identification and ii) visualization of subnetworks of interest from the <a href="https://vmh.uni.lu/"> Human Metabolic Reconstruction</a> (Recon2, Thiele et al., Nat Biotech, 2013) by using the powerful neo4j CYPHER query language and neo4j functionality, and iii) export in <a href="http://sbml.org/Main_Page">SBML standard</a> and <a href="http://wiki.cytoscape.org/Cytoscape_User_Manual/Network_Formats">SIF</a> formats in order to be integrated and shared among community using well-estabilished platforms (e.g. <a href="http://www.celldesigner.org/">CellDesigner</a>,  <a href="https://www.yworks.com/en/products/yfiles/yed/">yEd</a>, <a href="http://www.cytoscape.org/">Cytoscape</a>, <a href="http://www.ndexbio.org/">NDEx</a>).

## Graph database for human metabolic network 

## Tutorial available [here](https://www.youtube.com/embed/te6EUVAddUY).

## How to access the EpiGeNet Framework
The Recon2Neo4j Framework can be accessed online <a href="https://diseaseknowledgebase.etriks.org/metabolic/browser/"> here</a>.
	
A file with several Cypher query examples for the Recon2Neo4j Framework is available for download <a href="https://github.com/ibalaur/Recon2Neo4j/tree/master/sample%20queries"> here</a>. These queries can be extended to accommodate specific topics of interest. 

### For developers

The Recon2Neo4j Framework is freely available for non-commercial purposes and the java code used for data integration and mapping into the  framework is available <a href="https://github.com/ibalaur/Recon2Neo4j">here</a>.<br><br>
	
#### Requirements

 - Java 7 
 - Maven (tested with Maven 3.5)

#### Install

After cloning the repository and getting into its directory:

`mvn clean`

`mvn install`

The executable jar can be directly downloaded from the [Release page](https://github.com/ibalaur/EpiGeNet/releases).

## How to contribute

If you have any suggestions or want to report a bug, don't hesitate to create an [issue](https://github.com/ibalaur/EpiGeNet/issues). Pull requests and all forms of contribution will be warmly welcomed.

## Please cite our paper on this work

Balaur I., Mazein A., Saqi M., Lysenko A., Rawlings C.J. and Auffray C. (2016), <a href="http://bioinformatics.oxfordjournals.org.gate1.inist.fr/content/early/2017/01/05/bioinformatics.btw731.full">Recon2Neo4j: applying graph database technologies for managing comprehensive genome-scale networks</a>. <i>Bioinformatics</i>. 2016 Dec 19. pii: btw731. DOI: 10.1093/bioinformatics/btw731.

## Contributors

Irina Balaur, [EISBM](http://www.eisbm.org/), Lyon, France - specified the translation rules, developed the code
Alexander Mazein, [EISBM](http://www.eisbm.org/), Lyon, France - idea, advice on the translation rules
Mansoor Saqi, [EISBM](http://www.eisbm.org/), Lyon, France - advice on the translation rules
Artem Lysenko, [Rothamsted Research](https://www.rothamsted.ac.uk/), Hertfordshire, UK - advice on the Neo4j functionality
Chris J. Rawlings, [Rothamsted Research](https://www.rothamsted.ac.uk/), Hertfordshire, UK - advice on the Neo4j functionality
Charles Auffray, [EISBM](http://www.eisbm.org/), Lyon, France - strategic advice  

## Useful links

 - [eTRIKS](https://www.etriks.org/) 
 - [Disease Maps Project](http://disease-maps.org/) 

## Acknowledgements
This work has been supported by the Innovative Medicines Initiative Joint Undertaking under grant agreement no. IMI 115446 (eTRIKS), resources of which are composed of financial contribution from the European Union’s Seventh Framework Programme (FP7/2007-2013) and EFPIA companies.


