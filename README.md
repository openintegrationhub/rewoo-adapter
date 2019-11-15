# OIH Adapter for REWOO Scope

The low code platform REWOO Scope can be used to develop software to formalize business processes. 
In order to integrate these individual solutions into the company's IT landscape, the software can be 
coupled to other business software via the OIH using openintegrationhub/rewoo-adapter and openintegrationhub/rewoo-transformer.

In a first step this adapter can be used to transfer files. 

If you want to read files from REWOO Scope you have to choose the source layer in the form hierarchy and the 
source file container in this layer. In each run all changed files are extracted.

If you want to write files to REWOO Scope you have to choose the target layer in the form hierarchy, the target file 
container in this layer and the copy button to create new forms in this layer. In each run the given files are matched with 
the existing ones, changed files are replaced and for new files a new form is created via the copy button.