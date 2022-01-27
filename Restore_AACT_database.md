# Steps

1) go to URL :https://aact.ctti-clinicaltrials.org/snapshots 
2) got to Download Package Containing Static Copy of AACT Database -> Monthly Archive of Static Copies
3) Download recent month file in zip format and unzip the zip file
4) There will be file named 'postgres_data.dmp' dump file.
5) Restore dump file to PostgreSQL server using following command :
  	pg_restore --host "host_address" --port "port_number" --username "User Name" --no-password --role "postgres" --dbname "Database Name" --no-owner --no-privileges --verbose "filePath/postgres_data.dmp"
