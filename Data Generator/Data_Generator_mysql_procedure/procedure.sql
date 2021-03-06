DROP PROCEDURE IF EXISTS generate_data;
DELIMITER //
CREATE PROCEDURE generate_data () 
BEGIN
	DECLARE serial_num   CHAR(12);
	DECLARE packt_date   TIMESTAMP;
	DECLARE voltg        INT;
	DECLARE currnt       INT;
	DECLARE appa_power   INT;
	DECLARE acti_power   INT;
	DECLARE acti_powerT1 INT;
	DECLARE react_power  INT;

	DECLARE current_minute   INT;
	DECLARE finish_procedure BOOLEAN DEFAULT FALSE;

	DECLARE packets CURSOR FOR SELECT serial_number, date, voltage, current, apparent_power, active_power, active_powerT1, reactive_power from smarthomedb.MinutesConsumptionAux;

	DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET finish_procedure = TRUE;	

	OPEN packets;
	
	START TRANSACTION;

	DROP TABLE IF EXISTS smarthomedb.MinutesConsumption;
	CREATE TABLE smarthomedb.MinutesConsumption LIKE smarthomedb.MinutesConsumptionAux;

	FETCH packets INTO serial_num, packt_date, voltg, currnt, appa_power, acti_power, acti_powerT1, react_power;
	WHILE NOT finish_procedure DO
		SET current_minute = 0;
		REPEAT
			INSERT INTO smarthomedb.MinutesConsumption (serial_number, date, voltage, current, apparent_power, active_power, active_powerT1, reactive_power) VALUES(serial_num, TIMESTAMPADD(MINUTE,current_minute,packt_date), voltg, currnt, appa_power, acti_power, acti_powerT1, react_power);
			SET current_minute	= current_minute + 1;
		UNTIL (current_minute > 59) END REPEAT;
		FETCH packets INTO serial_num, packt_date, voltg, currnt, appa_power, acti_power, acti_powerT1, react_power;
	END WHILE;

	CLOSE packets;
	#DROP TABLE smarthomedb.MinutesConsumptionAux;
	COMMIT;
END //
DELIMITER ;
