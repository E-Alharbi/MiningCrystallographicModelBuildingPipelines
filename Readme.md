# Mining Crystallographic Model Building Pipelines Research Papers
A tool for mining in research papers that used crystallographic model-building pipelines and recognises which pipeline is used based on the information in the research paper.
 
<a href="https://github.com/E-Alharbi/MiningCrystallographicModelBuildingPipelines/releases/latest/download/Mining.jar"> Download latest release  </a> 
## Mining authors information


For mining in the first author's organisation country as well as the co-authors' organisations countries.
```
java -Xmx3048m -jar Mining.jar MiningAuthors Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" CrossrefEmail=email ElsevierToken=token ApplicationIdBack4app=token APIKeyBack4app=apikey BingApiKey=apikey
```
- The above command will fetch all PDB ids as well as Pubmed ID from the PDB bank and then obtains the research paper from https://europepmc.org/ by using the research paper's PubMedID.
- The papers which do not have PubMedID or are not found in https://europepmc.org/ will be ignored.  
- Pipeline: The name before the colon (:) is the pipeline official name that is usually used in the research paper when they refer to the pipeline and the name after the colon (:) is to use in the CSV file. This helps when the pipeline names are mentioned in different forms in different research papers. [Compulsory]
- CrossrefEmail: your email that registered in Crossref. If you did not register, you could register from here https://apps.crossref.org/clickthrough/researchers. [Optional]
- ElsevierToken= API key for Elsevier. If you do not have an Elsevier Token, you can get it from here https://dev.elsevier.com and select get API key. [Optional]
- ApplicationIdBack4app= App id from back4app.com. [Optional]
- APIKeyBack4app= App key from back4app.com. [Optional]
- BingApiKey= Microsoft bing maps API key. You get an API key from here https://www.bingmapsportal.com. [Optional]  

Please note that 

- back4app.com: we use the countries public database in case only the city is mentioned in the author affiliation. You need to register in back4app.com to get the app id and API key.    
- we use Microsoft bing maps when the city name in two different countries. 
- if you do not use the optional keywords, this will reduce the results being collected. 

### The output of the above command is three CSV files that contain the following 

 
| Field | Description |
| ------------- | ------------- |
| ID | Pubmed ID |
| Resolution | PDB resolution  |
| PublicationYear | Publication year of the research paper  |
| Tool | the tool/pipeline that used  |
| PDB | PDB id  |
| MostCountry | The country that most repeated in the authors' affiliation  |
| ListOfCountries | All the countries that mentioned in the authors' affiliation  |
| FirstAuthor | The country of the first author   |
| PublishedInOnePaper | Set to T when this paper contains multiple PDB   |
| journal  | name of the journal   |
| occurrence  | how many times the pipeline mentioned in the paper    |
|NameAsInPaper | the tool name as mentioned in the paper |
|journalAbbreviation | the abbreviation of the journal name |
| PartOfSentence | the part of a sentence where the tool name in mentioned| 
| Confidence* | high= no negative words were used in the sentence |

* negative words = modification,density,refinement and refine.

The three CSV files: 
- AuthorsInformation.csv: each record corresponded to a PDB. So, the authors' information will be repeated in case of multiple PDB published by the same author. 
- NonDuplicatedPipelineAuthorsInformation.csv: all PDB published in the same paper combine in one record. 
- NonDuplicatedPubid.csv: the paper which has mentioned multiple pipelines are omitted. 

In addition to the above CSV files, the following files are also produced:
- journals.csv: contains the journals names and the number of papers in each journal.
- journalsbyPipelines.csv: same as "journals.csv", but with adding the number of tool mentions in each journal.  
- PublicationYear.csv: contains the number of papers in each publication year. 
- PublicationYearByPipeline.csv: same as "PublicationYear.csv", but with adding the number of tool mention times in publication year.  
 
### The resources from where we get the research papers:
- https://europepmc.org/
- https://www.elsevier.com/
- https://onlinelibrary.wiley.com 

Some of the above resources need a membership. Elsevier will recognise if you have a membership from your IP address when you connect from your organisation/university. However, you need to register in Crossref and Online library. 


### UseDownloadedPapers keyword 
The tool will download the research papers as well as the authors' affiliation and then extract the required information from the authors' affiliation. Use UseDownloadedPapers when the tool already downloaded the research papers, and you want only to create the CSV file. 

```
UseDownloadedPapers=T
```
    

## PDBList keyword

```
java -jar Mining.jar MiningAuthors Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" PDBList=PDB.txt CrossrefEmail=youremail@email.com ElsevierToken=aaaa
```

- PDBList: a text file containing the PDB ids that you want to mine about. A PDB id in each line. For example: 
```
4ZYC
4ZYF
```


## Filtering the PDB bank data 
You can filter that PDB that obtains from the PDB bank based on these fields:

| Field | Description |
| ------------- | ------------- |
| structureId | PDB id |
| pmc | PMC id  |
| pubmedId |Pubmed id  |
|structureTitle| research paper title |
|experimentalTechnique| such as X-RAY DIFFRACTION |
| publicationYear | Publication year of the research paper  |
| resolution | PDB resolution  |

- You need to pass the keyword FilterBy with the command 

```
java -Xmx3048m -jar Mining.jar MiningAuthors FilterBy="[experimentalTechnique:X-RAY DIFFRACTION,SOLUTION NMR][publicationYear:2015-2020] Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" 
```
- The above command will be mining in the PDB that is solved by only X-RAY DIFFRACTION or SOLUTION NMR and published between 2015-2020.
- You can select to search for the papers that published in specific years by using a comma instead of hyphen For example:

```
java -jar Mining.jar MiningAuthors FilterBy="[experimentalTechnique:X-RAY DIFFRACTION,SOLUTION NMR][publicationYear:2016,2018] Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" 
```
it will be mining in the papers published in 2016 or 2018. 

All the fields mentioned in the above table can be used in the same way as in the above command.

- experimentalTechnique options (case sensitive ) 
1. X-RAY DIFFRACTION
2. SOLUTION NMR
3. FIBER DIFFRACTION
4. ELECTRON CRYSTALLOGRAPHY
5. ELECTRON MICROSCOPY
6. NEUTRON DIFFRACTION
7. SOLID-STATE NMR
8. INFRARED SPECTROSCOPY
9. SOLUTION NMR, THEORETICAL MODEL
10. SOLUTION SCATTERING
11. POWDER DIFFRACTION
12. X-RAY DIFFRACTION, SOLUTION NMR
13. FLUORESCENCE TRANSFER
14. X-RAY DIFFRACTION, EPR
15. SOLUTION SCATTERING, SOLUTION NMR
16. SOLID-STATE NMR, SOLUTION NMR
17. SOLUTION NMR, EPR
18. SOLUTION NMR, SOLUTION SCATTERING
19. SOLUTION NMR, SOLID-STATE NMR
20. ELECTRON MICROSCOPY, SOLID-STATE NMR
21. SOLID-STATE NMR, ELECTRON MICROSCOPY
22. EPR, X-RAY DIFFRACTION
23. NEUTRON DIFFRACTION, X-RAY DIFFRACTION
24. FIBER DIFFRACTION, SOLID-STATE NMR
25. ELECTRON MICROSCOPY, SOLUTION SCATTERING
26. SOLUTION SCATTERING, ELECTRON MICROSCOPY
27. X-RAY DIFFRACTION, NEUTRON DIFFRACTION
28. SOLUTION SCATTERING, SOLID-STATE NMR, ELECTRON MICROSCOPY
29. ELECTRON MICROSCOPY, SOLUTION NMR
30. SOLUTION SCATTERING, X-RAY DIFFRACTION
31. ELECTRON MICROSCOPY, SOLID-STATE NMR, SOLUTION NMR
32. X-RAY DIFFRACTION, SOLUTION SCATTERING
33. SOLUTION NMR, ELECTRON MICROSCOPY
34. NEUTRON DIFFRACTION, SOLUTION NMR

## Run on a cluster server

You can run the tool on a cluster to speed up the download of the papers. 

```
java -Xmx3048m -jar Mining.jar Cluster FilterBy="[experimentalTechnique:X-RAY DIFFRACTION][publicationYear:2010-2020]" Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:PHENIX AutoBuild,phenix autobuild:PHENIX AutoBuild" CrossrefEmail=email ElsevierToken=token BingApiKey=apikey JobParameters="[--time#00:10:00][--mem#4000][--partition#preempt][module load chem/ccp4/7.0.066][module load lang/Java/1.8.0_212]"
```
Write the above command in a shell script and submit as a job the cluster. Please note that the tool can only run on clusters that use slurm. Change the job parameters as necessary and spilt the keyword slurm and its vale by #. You do not need to write #SBATCH before each slurm keyword. It is recommended to run the above command few times (one by one after all jobs are completed) as some of the publishers API may not response if too many jobs are trying to connect at the same time so some of the jobs will fail. These failed jobs may get success when re-running them. 
```
java -Xmx3048m -jar Mining.jar MiningAuthors Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:PHENIX AutoBuild,phenix autobuild:PHENIX AutoBuild" CrossrefEmail=email ElsevierToken=token BingApiKey=apikey UseDownloadedPapers=T
```
Once all the jobs are completed, run the above command to extract authors affiliation and create the CSV files  
## Multithreaded
- The tool supports multithreaded by setting Multithreaded=T 
- A large number of threads might cause the resources to block the Http connections and result in freezing the tool. 

## Sentences analysing 
We obtained negative words by finding the occurrence of the words. Then we wrote the regular expression depending on the words with high occurrence. You can perform your sentences analysing using the following command.   

```
java -Xmx3048m -jar Mining.jar SentencesAnalysing Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:PHENIX AutoBuild,phenix autobuild:PHENIX AutoBuild" PapersFolder=the path to the folder contains the papers
```

The outputs of the above command are 

- Sentences.txt: contains the sentences were mentioned the names of the tools from different papers.  
- Words.csv: contains the words and their occurrence
 
Please note that you need to run first MiningAuthors command and this will download the papers, and then you can do sentences analysing.
