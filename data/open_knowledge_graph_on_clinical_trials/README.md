# Data collection steps

## Download inputs

- [MRCOC](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html) ex: detailed_CoOccurs_2021.txt.gz
- [MeSH](https://www.nlm.nih.gov/databases/download/mesh.html) ex: mesh2022.nt.gz
- [MeSH Vocabulary](https://www.nlm.nih.gov/databases/download/mesh.html) ex: 
- [AACT](https://aact.ctti-clinicaltrials.org/pipe_files) ex: 20220126_pipe-delimited-export.zip
- [ICTRP](https://www.who.int/clinical-trials-registry-platform) ex: ICTRPFullExport-682359-12-01-2021.zip

## AACT database download steps

- go to AACT [URL](https://aact.ctti-clinicaltrials.org/snapshots)
- go to Download Package Containing Static Copy of AACT Database -> Monthly Archive of Static Copies
- Download full snapshot file for the recent month. It would be in zip format. Unzip it.
- You should now see a file named _'postgres_data.dmp'_ dump file.
- Restore dump file to PostgreSQL server using following command :
        
`pg_restore --host "host_address" --port "port_number" --username "User Name" --no-password --role "postgres" --dbname "Database Name" --no-owner --no-privileges --verbose "filePath/postgres_data.dmp"`
