# Data collection steps

## Download inputs

- [MRCOC](https://lhncbc.nlm.nih.gov/ii/information/MRCOC.html) ex: detailed_CoOccurs_2021.txt.gz
- [MeSH](https://www.nlm.nih.gov/databases/download/mesh.html) ex: mesh2022.nt.gz
- [MeSH Vocabulary](https://www.nlm.nih.gov/databases/download/mesh.html) ex: 
- [AACT](https://aact.ctti-clinicaltrials.org/pipe_files) ex: 20220126_pipe-delimited-export.zip
- [ICTRP](https://www.who.int/clinical-trials-registry-platform) ex: ICTRPFullExport-682359-12-01-2021.zip

## AACT database download/restore steps

- Go to AACT [URL](https://aact.ctti-clinicaltrials.org/snapshots)
- Go to Download Package Containing Static Copy of AACT Database -> Monthly Archive of Static Copies
- Download full snapshot file for the recent month. It would be in zip format. Unzip it.
- You should now see a file named _'postgres_data.dmp'_ dump file.
- Restore dump file to PostgreSQL server using following command :
        
`pg_restore --host "host_address" --port "port_number" --username "User Name" --no-password --role "postgres" --dbname "Database Name" --no-owner --no-privileges --verbose "filePath/postgres_data.dmp"`

## ICTRP database download/restore steps



### Source Data
 - [URL](https://worldhealthorg-my.sharepoint.com/personal/karamg_who_int/_layouts/15/onedrive.aspx?id=%2Fpersonal%2Fkaramg%5Fwho%5Fint%2FDocuments%2FICTRP%20weekly%20updates)

    ![Ictrp_download_page](/home/reddaiah/Bala/Projects/ictrp/weekly_steps/ictrp_download_list.png")

 - Select weekly data and click download.
 - Unzip the downloaded zip file.
 
    ![Files after unzip](/home/reddaiah/Bala/Projects/ictrp/weekly_steps/unzip_file.png")

### Import to Database

- Connect to PostgresSQL server:

    `psql -U "user_name" -p "ort_numberr" -h "host" "database_name"`

- Chek for row count :

    `SELECT COUNT(*) FROM ictrp.ictrp_full_weekly_data;`

- Copy unzipped .csv file to ictrp_full_weekly_data, and ictrp_weekly-data tables of ictrp schema and check whether row count increased or not changed :


    `\COPY ictrp."ictrp_weekly_data" FROM '/home/reddaiah/Bala/Projects/ictrp/Weekly/ICTRPWeek24January2022.csv' DELIMITER ',' CSV QUOTE AS '"' HEADER;`

    `\COPY ictrp."ictrp_full_weekly_data" FROM '/home/reddaiah/Bala/Projects/ictrp/Weekly/ICTRPWeek24January2022.csv' DELIMITER ',' CSV QUOTE AS '"' HEADER;`

    `SELECT COUNT(*) FROM ictrp.ictrp_full_weekly_data;`

### Cleaning and Transformation of data

- Run `data_transforamtion.sql` script in dbeaver IDE, which perform following actions
    - Delete duplicate records 
    - Drop and create new ictrp_rpt table 
    - Data cleaning by removing un wanted symbols like ""  , : , <br> from key columns
    - Adding New Columns and updating data to new columns
    - Date format standardization


- Run `CTRI_cond_update.sql` script in dbeaver IDE, which update 'conditions_std' of CTRI Registry

### Generation of graphs, diagrams and tables 
- Connect to jhub server
- Run ictrp.ipynb


## MRCOC

- EDA - exploratory data analysis

```
$ wc -l data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt 
1734959115 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt

$ cut -d '|' -f1,9,15 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt

$ sort -u  data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt 

$ wc -l data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt 
1734950157 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields_sorted.txt

$ wc -l data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt 
1734959115 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_selected_fields.txt

$ cut -d'|' -f 7 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt  | grep 2020 | wc -l
65919771

$ grep -F "|2020|" data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020.txt 

$ wc -l data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020.txt 
65919771 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020.txt

$ cut -d '|' -f1,9,15 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020.txt > data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020_selected_fields.txt

$ wc -l data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020_selected_fields.txt 
65919771 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020_selected_fields.txt

$ head -n 5 data/open_knowledge_graph_on_clinical_trials/detailed_CoOccurs_2021_with_Mesh_year_2020_selected_fields.txt
31493778|D000001|D000067128
30557043|D000001|D000069261
31926265|D000001|D000073658
31926265|D000001|D000074662
30557043|D000001|D000075702

```
