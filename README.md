# About

A basic clojure library for working with HL7 messages.  Provides core functions for working with HL7 message streams, and built-in task functionality that use the core functions.  C

## Task - Router

A simple task that routes a source of HL7 messages to a target process.  This task is configured via commandline options:

* -s, --source : The type of source to retrieve the HL7 messages from.  Defaults to 'resource'.
* -t, --target : The type of target to send the HL7 messages to.  Defaults to 'counter'.
* -so, --source-options : Key/Value options used by the source.  Theses are source specific.  Is passed in the form of URL parameters and must be quoted.
* -to, --target-options : Key/Value options used by the target.  Theses are target specific.  Is passed in the form of URL parameters and must be quoted.

### Router Source - 'resource'
Pulls the HL7 messages from a file off the classpath. Uses the following source options:

* path : The path to the resource from the root of the classpath.

### Router Source - 'file'
Pulls the HL7 messages from a file off the file system. Uses the following source options:

* path : The path to the file from the root of the file system.

### Router Source - 'uri'
Pulls the HL7 messages from a URI location. Uses the following source options:

* uri : The fully formed URI to the HL7 messages.


### Router Target - 'counter'
Counts all of the messages from the source and prints it out to the console.  Good for debugging.  No Parameters are needed.



### Router Target - 'splitter'
Writes the messages to batch files, partitioning to files based off of a partitioning function.  Uses the following source options:

* directory : The path to the directory where the files are saved to.
* prefix : A prefix to use when naming the output files.
* suffix : A suffix to use when naming the output files.  This would include the extension.
* partitioner : The name of the partitioner to use.  The partitioner provides a string that, along with the prefix and suffix, determine the name of the file to save the messages to.  The current partitioners are:
    + msg-hour : Returns a the date of the HL7 message in the format of YYYYMMDDhh.

### Examples

```
    lein run -m pliant.hl7.task.router -so "uri=file:/hl7/ADT_201310.hl7" -to "partitioner=msg-hour&directory=/temp/work/hl7&prefix=ADT_&suffix=.hl7" -s uri -t splitter
```


## License

Copyright © 2013 Daniel Rugg

Distributed under the Eclipse Public License, the same as Clojure.
