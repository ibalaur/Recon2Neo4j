# Recon2Neo4j

The EpiGeNet framework is a graph database for storage and querying of conditional relationships between molecular (genetic and epigenetic) events observed at different stages of colorectal (CRC) oncogenesis. Data were i) extracted from <a href="http://statepigen.sci-sym.dcu.ie/index.php">StatEpigen</a>, a manually curated and annotated database, containing information on interdependencies between genetic and epigenetic signals, and specialized currently for CRC research, and ii) imported to our newly-developed EpiGeNet, which offers improved capability for management and visualization of data on molecular events specific to CRC initiation and progression

## How to access the EpiGeNet Framework
The EpiGeNet Framework can be accessed online <a href="https://diseaseknowledgebase.etriks.org/epigenet/browser/"> here</a>

A non-exhaustive file with Cypher query examples for the EpiGeNet Framework is available for download <a href="epigenet_framework_sample_queries.docx" download="epigenet_framework_sample_queries.docx"> here</a>.
	
### For developers
The EpiGeNet Framework is freely available for non-commercial purposes and the java code used for data integration and mapping into the  framework is available <a href="https://github.com/ibalaur/EpiGeNet">here</a>.<br><br>
	
## How to get Involved
We would be happy to hear from your experience and for feedback, any issues/ suggestions on this, please contact us by email to <a href="mailto:ibalaur@eisbm.org">ibalaur@eisbm.org</a>.</p>

##Please cite our paper on this work<
<p> Balaur I., Saqi M., Barat A., Lysenko A., Mazein A., Rawlings C.J., Ruskin H.J. and Auffray, C. (2016), <a href="http://online.liebertpub.com/doi/10.1089/cmb.2016.0095">EpiGeNet: A Graph Database of Interdependencies Between Genetic and Epigenetic Events in Colorectal Cancer</a>, Journal of Computational Biology, September 2016, ePub(ahead of print). DOI: 0.1089/cmb.2016.0095.




## Requirements

 - Java 7 
 - Maven (tested with Maven 3.5)

## Install

After cloning the repository and getting into its directory:

`mvn clean`

`mvn install`

The executable jar can be directly downloaded from the [Release page](https://github.com/ibalaur/EpiGeNet/releases).

## How to contribute

If you have any suggestions or want to report a bug, don't hesitate to create an [issue](https://github.com/ibalaur/EpiGeNet/issues). Pull requests and all forms of contribution will be warmly welcomed.

## Contributors

Irina Balaur, [EISBM](http://www.eisbm.org/), Lyon, France - idea, specified the translation rules, developed the code
Mansoor Saqi, [EISBM](http://www.eisbm.org/), Lyon, France - advice on the translation rules
Ana Barat, [RCSI](https://www.rcsi.com/dublin/], Dublin, Ireland - advice on the input dataset
Artem Lysenko, [Rothamsted Research](https://www.rothamsted.ac.uk/), Hertfordshire, UK - advice on the Neo4j functionality
Alexander Mazein, [EISBM](http://www.eisbm.org/), Lyon, France - advice on the translation rules
Chris J. Rawlings, [Rothamsted Research](https://www.rothamsted.ac.uk/), Hertfordshire, UK - advice on the Neo4j functionality
Heather J. Ruskin [Dublin City University], Dublin, Ireland - advice on the input dataset and on the translation rules
Charles Auffray, [EISBM](http://www.eisbm.org/), Lyon, France - strategic advice  

## Useful links

 - [eTRIKS](https://www.etriks.org/) 
 - [Disease Maps Project](http://disease-maps.org/) 

## Acknowledgements
This work has been supported by the Innovative Medicines Initiative Joint Undertaking under grant agreement no. IMI 115446 (eTRIKS), resources of which are composed of financial contribution from the European Union’s Seventh Framework Programme (FP7/2007-2013) and EFPIA companies.


