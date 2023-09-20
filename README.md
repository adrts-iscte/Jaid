# Jaid

An operation-based merge tool for Java that relies on a form of representing the identity of the elements of the source code to achieve a more accurate merge process.

## Merge Tool

> [!IMPORTANT]
> All Java files of the project must have identity on their elements. See Section [Providing identity to a merge scenario](#providing-identity-to-a-merge-scenario).

There are two ways of using Jaid via command line:

### Providing a .revisions file
    
By using the following command
    
``
java -jar Jaid-1.0.jar revisionsFullPath
``

replacing `revisionsFullPath` with the full path to the .revisions file

#### Structure of the folders of a merge scenario

The merge scenario must be stored in the following folders tree structure:

```
└── Merge Scenario Main Folder
     ├── LeftVersion
     │     └── Left files...
     ├── BaseVersion
     │     └── Base files...
     ├── RightVersion
     │     └── Right files...
     └── MergeScenario.revisions
```

The .revisions file must only contain the following information:

```
LeftVersionFolderName/
BaseVersionFolderName/
RightVersionFolderName/
```

### Providing all three versions paths

By using the following command

``
java -jar Jaid-1.0.jar leftFullPath baseFullPath rightFullPath
``

## Results

In case of conflicts, a `conflicts.csv` file is created with information about all detected conflicts.
This file is stored in the parent folder of the base version.

If there are no conflicts, a `MergedVersions/` folder is created with the output of the merge process.
This folder is also created in the parent folder of the base version.

## Providing identity to a merge scenario

According to the published research paper, identity should be a property that is maintained throughout the development cycle by attaching UUIDs to source code elements.
However, this does not exist in current development practices, so in order to test Jaid on a larger scale than human-made merge scenarios, it is necessary to simulate some merge scenarios.
To accomplish this, a tool has been developed that uses a tree matcher, specifically [GumTree](https://github.com/GumTreeDiff/gumtree), to provide identity by assigning the same UUID to elements that GumTree considers equal and random ones to the remaining elements.

The tool can be used to provide identity to a merge scenario using any of the commands below, respecting the same rules of sections [Providing a .revisions file](#providing-a-.revisions-file) and [Providing all three versions paths](#providing-all-three-versions-paths):

``
java -jar IdentifyMergeScenario-1.0.jar leftFullPath baseFullPath rightFullPath
``

OR

``
java -jar IdentifyMergeScenario-1.0.jar revisionsFullPath
``

The output of this process is three different folders with names ending in "with_identity" containing the three versions (left, base, right) with augmented identity.
These folders are created at the parent folder of the corresponding "unidentified" version folder, so the folders tree structure becomes:

```
└── Merge Scenario Main Folder
     ├── LeftVersion
     │     └── Left files...
     ├── LeftVersion_with_identity
     │     └── ...
     ├── BaseVersion
     │     └── Base files...
     ├── BaseVersion_with_identity
     │     └── ...
     ├── RightVersion
     │     └── Right files...
     ├── RightVersion_with_identity
     │     └── ...
     └── MergeScenario.revisions
```

## Citing Jaid

If you use Jaid in an academic work you can refer to the following paper, which describes the main ideas behind Jaid:

```
@inproceedings{Teles23,
    author    = {Andr{\'{e}} R. Teles and Andr{\'{e}} L. Santos},
    title     = {Code Merging Using Transformations and Member Identity},
    booktitle = {ACM SIGPLAN International Symposium on New Ideas, New Paradigms,
    and Reflections on Programming and Software (Onward! ’23) - October 25 - 27, 2023},
    pages     = {313--324},
    year      = {2023},
    url       = {https://doi.org/10.1145/3622758.3622891},
    doi       = {10.1145/3622758.3622891}
}
```

## Contacts

In case you have any doubt about the implementation or any other question do not hesitate to contact us

- André R. Teles - adrts@iscte-iul.pt
- André L. Santos - andre.santos@iscte-iul.pt
