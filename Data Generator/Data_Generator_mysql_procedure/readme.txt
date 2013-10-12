Instructions:
	-First of all, run the server (SmartHomeServer)
	-Execute SHdataGenerator
	-Open a terminal
	-Enter into mysql
		-mysql smarthomedb -u SHclient -p 
			-SHclient11235 
	-Execute de sql file (generate_data.sql)
		-source path/to/file.sql
		#the path cannot contain spaces
	-call the procedure
		-call generate_data ()
