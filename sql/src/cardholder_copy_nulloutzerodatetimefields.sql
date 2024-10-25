use chipub;

SET sql_mode=(SELECT REPLACE(@@sql_mode,"NO_ZERO_DATE", ""));
SET sql_safe_updates=0;
UPDATE cardholder_copy SET checked_in = NULL WHERE checked_in = '0000-00-00 00:00:00';
SET sql_safe_updates=1;
