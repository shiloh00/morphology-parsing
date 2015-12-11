# Wiktionary Re-processing for Morphology Analysis

This is a Eclipse project to generate a simple database to analyze the morphology features for a given word.

All the required Python scripts are in directory scripts

## Run Eclipse project

Configure the Java code to specify the name of the raw database.


## Generate hierarchy using raw database

```
python ./scripts/build_db.py raw_db.txt hierarchy.json
```

And there will be a file named d.pickle containing the inverted python-based map


## Get vector for given word


```
python ./scripts/to_vec.py d.pickle
```
