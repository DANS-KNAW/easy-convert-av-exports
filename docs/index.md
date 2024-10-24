easy-convert-av-exports
=======================

SYNOPSIS
--------

    easy-convert-av-exports [--move] <input-dir> <output-dir>

DESCRIPTION
-----------

This project is a rewrite of [easy-preprocess-av-bag](https://github.com/DANS-KNAW/easy-preprocess-av-bag). It is a command line tool that preprocesses bags exported by [easy-fedora-to-bag] which contain AV
materials. The resulting bags are then further processed to deposits by [easy-convert-bag-to-deposit] and ingested into a Data Station.

[easy-fedora-to-bag]: https://github.com/DANS-KNAW/easy-fedora-to-bag

[easy-convert-bag-to-deposit]: https://github.com/DANS-KNAW/easy-convert-bag-to-deposit

### Processing

The input is a directory containing bags exported from EASY by `easy-fedora-to-bag`. Per dataset one or two bags are exported, depending on whether there
was a folder called `original` in the dataset. The `orginal` folder contains the original files of the dataset as deposited by the user. 

This tool completes the second version with the streaming copies of the audio and video files and subtitles, if present. The pseudo-files
(empty files that refer to files in the dark archive) are removed. It is envisioned that the files these refer to will be added to the dataset in the Data Station
in a later stage.


```yaml
sources:
  springfieldDir: # ...location of directory containing files from Springfield (streaming copies)
  path: # ...location of a CSV file detailing where to find the files  

stagingDir: # ...location of directory where the files are staged

```

The CSV file should have the following format:

| easy_file_id    | dataset_id         | path_in_springfield_dir | 
|-----------------|--------------------|-------------------------|
| easy-file:12345 | easy-dataset:67890 | path/to/file2           |
| easy-file:23456 | easy-dataset:67890 | path/to/file4           |

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/easy-convert-av-exports` and the configuration files to `/etc/opt/dans.knaw.nl/easy-convert-av-exports`.

### Development

When running the program with the `start.sh` script, you must set `JAVA_HOME` to point to a JDK 8 installation. However, when building the project with Maven, 
you must set `JAVA_HOME` to point to a JDK 17 installation.


BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 8
* Maven 3.3.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/easy-convert-av-exports.git
    cd easy-convert-av-exports 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.
