use chipub;

-- ----------------
-- Indexes      --
-- ----------------

-- Speed up lookup by author last name
CREATE INDEX a_lname ON author(last_name);

-- Speed up lookup by ISBN
CREATE INDEX isbn ON book(isbn);

-- ----------------
-- Triggers     --
-- ----------------

-- 0000-00-00 00:00:00 isn't a valid datetime format to MySql
-- However, populate from CSV can give this result for checked_in timestamps
-- Creating a trigger to enforce that this should be null instead

DELIMITER //
CREATE TRIGGER verify_datetime
BEFORE INSERT ON chipub.cardholder_copy
FOR EACH ROW
IF new.checked_in < '0000-01-01 00:00:00' THEN SET new.checked_in = NULL;
END IF; //

-- ----------------
--    Views     --
-- ----------------

-- View for only checked out copies

CREATE VIEW checkouts AS
	SELECT * FROM cardholder_copy 
    WHERE checked_in is NULL;
    
-- select * from checkouts;
	
-- ----------------
-- Temp Tables  --
-- ----------------
    
-- Creates a temp table to view the status of individual copies at their locations
-- TODO!! Status should include holds

CREATE TEMPORARY TABLE copystatus
	SELECT cid, book_id, branch_id,
    (
		SELECT
		CASE 
        WHEN checked_in IS NULL 
        THEN "out"
        WHEN checked_in IS NOT NULL
        THEN "in"
        END 
        FROM cardholder_copy
        WHERE cid = copy_id
	)AS copy_status
    FROM copy
    WHERE cid IN (select copy_id from cardholder_copy);
    
-- select * from copystatus;
