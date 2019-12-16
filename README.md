# OIH Adapter for REWOO Scope

The low code platform [REWOO Scope](https://rewoo.de) can be used to develop software to formalize business processes. 
In order to integrate these individual solutions into the company's IT landscape, the software can be 
coupled to other business software via the OIH using the Scope-to-OIH connector.

This connector consists of two parts: this adapter and a [transformer](https://github.com/openintegrationhub/rewoo-transformer).
The adapter connects to the Scope [REST API](https://rewoo.de/manual/T-rest-api.html) to read and write data as JSON. The transformer transforms the Scope JSON format to OIH JSON and vice versa.

This adapter can be used to read and write files.

## The Scope server

First you need the URL of the Scope server you want to connect to and a valid account. 
If you do not yet have access to a Scope server or you would like to test this interface, you can [request access](https://rewoo.de/unternehmen/kontakt.html) 
to our test systems. 

Actually we at REWOO GmbH use [trial.rewoo.net/oih](https://trial.rewoo.net/oih) as source and [customer.rewoo.net/oih](https://customer.rewoo.net/oih) as target. 

## Read files

After you checked the credentials you have to choose the source layer in the form hierarchy and the 
source file container in this layer. 

In our test system the source layer is **Rechnung** and the file container is **Rechnungsdokument**. 

In the first run all existing files are read out. 
Since the adapter stores the time of the last synchronization, only changed files are considered in all further runs.

## Write files

If you want to write files to Scope you have to choose the target layer in the form hierarchy, the target file 
container in this layer and the copy button to create new forms in this layer. 

In our test system the target layer is **Einzelrechnung**, the file container is **Dateien**, 
the layer of the copy button is **Rechnungen Aspect** and the copy button is **neue Rechnung**. 

In each run the given files are matched with the existing ones, changed files are replaced and for new files a new form is created via the copy button.

## Triggers

### Get all changed files

This trigger (GetAllChangedFiles.java) is polling for changed files since the last run.
The trigger consists of the three steps login, read data and logout.

If you use the output of this trigger as input of the [rewoo-transformer](https://github.com/openintegrationhub/rewoo-transformer) 
you have to split the array into single object, for example you can use the splitter component from elasticio.  

## Actions

### Upsert changed files

This action (UpsertChangedFiles.java) writes new and changed files to Scope. 
The action consists of the three steps login, write data and logout. The input must be a JSON array. 

If you use the output of the [rewoo-transformer](https://github.com/openintegrationhub/rewoo-transformer) as input of this trigger 
you have to encapsulate the single JSON object into an array. In elasticio configure the input in developer mode:  
```
{
  "files": $append(files.{
      "id": id,
      "authorId": authorId,
      "size": size,
      "creationTimestamp": creationTimestamp,
      "name": name,
      "elementName": elementName,
      "elementId": elementId,
      "entryId": entryId,
      "extension": extension,
      "mimeType": mimeType,
      "hash": hash,
      "url": url,
      "type": type
    }, [])
}   
```
