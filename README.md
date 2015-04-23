# Coref
This system combines Latvian text processing tools including morphological tagger, named entity recognizer, syntactical parser (Maltparser), semantic dependency parser (mate-tools), corereference resolution and named entity linking (Stanford NER).

## License
The software is licensed under the full GPL. Please see the file LICENCE.txt
The included text corpus data, books, newspaper extracts and dictionaries are copyrighted by their respective authors, and are available for research purposes only.

## System requirements
JRE 1.7+ should be installed and available on path.
~3Gb memory should be available.

## Downloads
The binary downloads and pretrained models are excluded from this repository due their size.

## Compiling from source
Eclipse project files and Ant build scripts are included.
The build scripts assume that the <a href="https://github.com/PeterisP/morphology">morphological analysis module</a> and <a href="https://github.com/PeterisP/LVTagger">LVTagger</a> will be located next to this project: `../morphology` and `../LVTagger`
The following commands should build a working system on MacOS/Linux, assuming that Java/ant is installed.
```
git clone https://github.com/PeterisP/morphology.git
git clone https://github.com/PeterisP/LVTagger.git
git clone https://github.com/chaosfoal/Coref.git
cd morphology/
ant
cd ../LVTagger/
ant
cd ../Coref
ant
```

## Contact
For information, bug reports and any problems, contact arturs.znotins@gmail.com.

## Usage
All input and output should be in UTF-8.
Use `./pipe.sh`
Run `./pipe.sh -h` for information about arguments.
File interaction with standard UNIX stdin/stdout: `./pipe.sh <inputfile.txt >outputfile.txt`


### Arguments

Argument | Description
----------------------------|----------------------------
-prop                	      |	Property file
-solve               	      |	Do coreference resolution [true, false]
-pipe.tools          	      | Runned pipe tools [tokenizer, tagger, ner, parser, spd, coref, nel] separated by commas. Default: run all
-pipe.input          	      | Pipe input format [text, json_meta]. Default: json_meta
-pipe.output         	      | Pipe output format [json, json_array]. Default: json
-coref.remSingletons 	      |	Remove singleton mention clusters during postprocessing
-coref.remCommonUnknownSingletons | Remove common unknown category singletons during postprocessing
-coref.remDescriptors	      | Remove descriptor mentions for professions
-coref.debugMentionStrings	  |	Print decisions for head or exact match mentions separate with pipe char: &#124;
-knb.url             	      |	Knowledge base url. E.g., jdbc:postgresql://localhost:5432/knb
-knb.user            	      |	Knowledge base username
-knb.password        	      |	Knowledge base user password
-knb.enable          	      |	Use knowledge base for NEL [true,false]. Default: true
-knb.dataset         	      |	KNB dataset [int]. Uesd for writing info to DB. Default: 0
-nel.showDisambiguation	      | Prints NEL disambiguation information to std.err [true,false]. Default: false
-nel.showInserts     	      |	Print NEL inserted entities [true,false]. Default: false
-nel.showEntities    	      |	Print NEL linked entities [true,false]. Default: false
-nel.upload          	      |	Allow NEL to upload entities to database [true,false]. Experimental. Default: false
-nel.verbose         	      |	Print all verbose NEL decisions [true,false]. Default: false


## Input formats
### Text
Plain text. Separate multiple documents with 2 blank lines. Pipe is closed if after seeing empty document.

### JSON with meta info
Separate multiple doucuments with 3 blank lines.
```
{"text": "Pēteris Vasks ir komponists .", "document": "#1", "date": "2015-03-24 15:59:34.250750"}

{"text": "Pēteris Vasks ir komponists .", "document": "#2", "date": "2015-03-24 15:59:34.250750"}
```

## Output formats

###JSON output format

```
{
    "date" : "2015-03-24 15:59:34.250750",
	"sentences" : [{
			"tokens" : [{
					"features" : "Īpašvārda_veids=Priekšvārds|Skaitlis=Vienskaitlis|Avota_pamatforma=Pēteris|Lietvārda_tips=Īpašvārds|Pamatforma=Pēteris|Vārdšķira=Lietvārds|Lielo_burtu_lietojums=Sākas_ar_lielo_burtu|Locījums=Nominatīvs|Dzimte=Vīriešu|LETA_lemma=Pēteris|Distsim=188",
					"parentIndex" : 4,
					"form" : "Pēteris",
					"pos" : "n_msn_",
					"namedEntityType" : "person",
					"index" : 1,
					"lemma" : "Pēteris",
					"tag" : "npmsn2",
					"dependencyLabel" : "namedEnt:basElem",
					"sdp" : [{
							"label" : "namedEnt:basElem",
							"target" : 2
						}
					]
				}, {
					"features" : "Skaitlis=Vienskaitlis|Avota_pamatforma=vasks|Lietvārda_tips=Sugas_vārds|Pamatforma=vasks|Vārdšķira=Lietvārds|Lielo_burtu_lietojums=Sākas_ar_lielo_burtu|Locījums=Nominatīvs|Dzimte=Vīriešu|LETA_lemma=vasks|Distsim=86",
					"parentIndex" : 4,
					"form" : "Vasks",
					"pos" : "n_msn_",
					"namedEntityType" : "person",
					"mentions" : [{
							"start" : 1,
							"end" : 2,
							"id" : 1,
							"type" : "person"
						}
					],
					"index" : 2,
					"lemma" : "vasks",
					"tag" : "ncmsn1",
					"dependencyLabel" : "phdep:subj",
					"sdp" : [{
							"label" : "phdep:subj",
							"target" : 4
						}
					]
				}, {
					"features" : "Laiks=Tagadne|Skaitlis=Nepiemīt|Persona=3|Darbības_vārda_tips=Palīgverbs_'būt'|Atgriezeniskums=Nē|Avota_pamatforma=būt|Pamatforma=būt|Noliegums=Nē|Transitivitāte=Nepārejošs|Vārdšķira=Darbības_vārds|LETA_lemma=būt|Izteiksme=Īstenības|Kārta=Darāmā|Distsim=41",
					"parentIndex" : 4,
					"form" : "ir",
					"pos" : "v__i___30__",
					"index" : 3,
					"lemma" : "būt",
					"tag" : "vcnipii30an",
					"dependencyLabel" : "xPred:auxVerb",
					"sdp" : [{
							"label" : "xPred:auxVerb",
							"target" : 4
						}
					]
				}, {
					"features" : "Skaitlis=Vienskaitlis|Avota_pamatforma=komponists|Lietvārda_tips=Sugas_vārds|Pamatforma=komponists|Vārdšķira=Lietvārds|Locījums=Nominatīvs|Dzimte=Vīriešu|LETA_lemma=komponists|Distsim=124|Gaz=LOMA,PROF_FULL,PROFESIJA|GazFile=AZ_profesijas.txt,AZ_roles.txt,AZ_profesijas_full_lem.txt",
					"parentIndex" : 0,
					"form" : "komponists",
					"pos" : "n_msn_",
					"namedEntityType" : "profession",
					"mentions" : [{
							"start" : 4,
							"end" : 4,
							"id" : 1,
							"type" : "profession"
						}
					],
					"index" : 4,
					"lemma" : "komponists",
					"tag" : "ncmsn1",
					"dependencyLabel" : "sent:pred",
					"sdp" : [{
							"label" : "_root_",
							"target" : 0
						}
					]
				}, {
					"features" : "Pieturzīmes_tips=Punkts|Vārdšķira=Pieturzīme|LETA_lemma=.|Avota_pamatforma=.|Pamatforma=.|Distsim=126",
					"parentIndex" : 4,
					"form" : ".",
					"pos" : "zs",
					"index" : 5,
					"lemma" : ".",
					"tag" : "zs",
					"dependencyLabel" : "sent:punct",
					"sdp" : [{
							"label" : "sent:punct",
							"target" : 4
						}
					]
				}
			],
			"text" : "Pēteris Vasks ir komponists ."
		}
	],
	"document" : "#1",
	"namedEntities" : {
		"1" : {
			"isTitle" : true,
			"aliases" : ["Pēteris Vasks", "komponists"],
			"globalId" : 2205307,
			"id" : 1,
			"type" : "person",
			"representative" : "Pēteris Vasks",
			"inflections" : {
				"Akuzatīvs" : "Pēteri Vasku",
				"Ģenitīvs" : "Pētera Vaska",
				"Nominatīvs" : "Pēteris Vasks",
				"Datīvs" : "Pēterim Vaskam",
				"Dzimte" : "Vīriešu",
				"Lokatīvs" : "Pēterī Vaskā"
			}
		}
	}
}
```

## Coreference module
LVCoref is based on applying rules one at a time from the highest to lowest precision. In the beginning each mention represents new entity. Rules are used to merge mentions thus also their represented coreference chains by looking at mention and their represeted entity agreement. Rules uses features based on morphological gender, number, case, syntactic constraints one does not dominate another, i-within-i, NER category and head modifiers.
