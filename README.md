# Meetings Search - A simple command line app
This is a simple command line app that parses data from wikipedia dump in .bz2 file to Elastic Search instance.

To run the project, simply build it and then execute task `packageDistribution` which creates a directory /dist where 
the executable can be found. From that, here is the help printed from base program:

```
Usage: meetings [OPTIONS] COMMAND [ARGS]...

Options:
-h, --help  Show this message and exit

Commands:
parse
search
import
```
 ---
## Parse module
To parse, specify `parse` command into the app:

```
Usage: meetings parse [OPTIONS]

Options:
  -s, --source TEXT       Source file of dump
  -d, --destination TEXT  Destination file of parsed dump
  -h, --help              Show this message and exit
```

---
## Import module
To import, specify `import` command into the app:

```
Usage: meetings import [OPTIONS]

Options:
  -s, --source TEXT      Parsed file from parser command to import into
                         Elastic
  -h, --host TEXT        Custom host of elasticsearch instance, default is
                         localhost
  -p, --port INT         Custom port of elasticsearch instance, default is
                         9200
  -g, --granularity INT  Import logging granularity, 0 will disable logging,
                         default is 5000
  --help                 Show this message and exit
```

---
## Search module
To search, specify `search` command into the app:

```
Usage: meetings search [OPTIONS]

Options:
  --first-name TEXT   First person name to find
  --second-name TEXT  Second person name to find
  -h, --host TEXT     Custom host of elasticsearch instance, default is
                      localhost
  -p, --port INT      Custom port of elasticsearch instance, default is 9200
  --help              Show this message and exit
```

**Note**: It's better to specify first name and second name after running program because current 
implementation doesn't support spaces
