# MiningCrystallographicModelBuildingPipelines
A tool to mining in research papers that used crystallographic model building pipelines.
## Mining authors information


You can get a research paper first author's country as well as the co-authors countries.
```
java -jar Mining.jar  MiningAuthors Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild"
```
- The above command will fetch all PDB ids as well as Pubmed ID from the PDB bank and then obtains the research paper from https://europepmc.org/ by using the research paper PUB MED ID.
- The papers which do not have PUB MED ID or not found in https://europepmc.org/ will be ignored.  
- Pipeline: The name before the colon (:) is the pipeline official name that usually uses in the research paper when they refer to the pipeline and the name after the colon (:) is to use in the CSV file. This help when the pipeline mentions in different names in different research papers.
### The output of the above command is a CSV file that contains the following
| Field  | Description |
| ------------- | ------------- |
| ID  | Pubmed ID  |
| Resolution  | PDB resolution   |
| PublicationYear  | Publication year of the research paper    |
| Tool  | the tool/pipeline that used    |
| PDB  | PDB id   |
| MostCountry  | The country that most repeated in the authors' affiliation   |
| ListOfCountries  | All the countries that mentioned in the authors' affiliation   |
| FirstAuthor  | The country of the first author     |
| PublishedInOnePaper  | Set to T when this paper contains multiple PDB     |

### UseExistsPapers keyword 
The tool will download the research papers as well as the authors information and then extract the authors information. Use UseExistsPapers when the tool already downloaded the research papers and you want to only create the CSV file. 
```
UseExistsPapers=T
```
      

## Mining pipelines
You can be mining only about the pipelines used in the research papers without mining in the authors' information. The difference here from the "Mining authors information"  is that here we use more resources to obtain the research papers. 
- The resources that use to obtain the research papers:
- https://europepmc.org/
- https://www.elsevier.com/
- https://onlinelibrary.wiley.com 

Some of the above resources need to a membership. Elsevier will recognise if you have a membership from your IP address when you connect from your organisation/university. However, you need to register in Crossref and Onlinelibrary. 

```
java -jar Mining.jar MiningPipeline Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" PDBList=PDB.txt CrossrefEmail=youremail@email.com ElsevierToken=aaaa
```

- PDBList :  a text file contains the PDB ids that you want to mining about. A PDB id in each line. For example: 
```
4ZYC
4ZYF
```
If you did not provide this keyword, it would be mining in all the PDB that obtain from the PDB bank.
- CrossrefEmail: your email that registered in Crossref. If you did not register, you could register from here https://apps.crossref.org/clickthrough/researchers.
- ElsevierToken= API key for Elsevier. If you did not have an Elsevier Token, you can get it from here  https://dev.elsevier.com and selects get API key. 

### The outputs are  three CSV files contain the following
- FoundPapers.csv: The papers that are found and there were used the pipeline/tool
- PapersFoundButNotUsePipeline.csv: The papers that are found but there were not used the pipeline/tool
- PapersNOTFound.csv: The papers that are not found.

The CSV file contains the following:

| Field  | Description |
| ------------- | ------------- |
| PDB  | PDB id  |
| Pipeline  | the tool/pipeline that used. Only in FoundPapers.csv   |
| PaperLink  | DOI link    |

## Filtering the PDB bank data 
You can filter that PDB that obtains from the PDB bank based on these fields:

| Field  | Description |
| ------------- | ------------- |
| structureId  | PDB id  |
| pmc  | PMC id   |
| pubmedId  |Pubmed id    |
|structureTitle| research paper title |
|experimentalTechnique| such as X-RAY DIFFRACTION |
| PublicationYear  | Publication year of the research paper    |
| resolution  | PDB resolution    |

- You need to pass the keyword FilterBy with the command 
```
java -jar Mining.jar MiningAuthors FilterBy="[experimentalTechnique:X-RAY DIFFRACTION,SOLUTION NMR][publicationYear:2015-2020] Pipeline="arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild" 
```
- The above command will be mining in the PDB that solved by only X-RAY DIFFRACTION or SOLUTION NMR and published between 2015-2020.
- You can select to search for the papers that published in a specific year by using a comma instead of hyphen For example:
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

## Multithreaded
- The tool supports Multithreaded by setting Multithreaded=T 
- Large number of threads might cause the resources to block the http connections and result in freezing the tool.  
