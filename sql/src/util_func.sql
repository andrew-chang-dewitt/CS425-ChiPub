use chipub;


-- ----------------
-- Stored Procs -- 
-- ----------------

DELIMITER //
CREATE PROCEDURE SelectBookBaseIndexes()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_BaseIndex AS
	SELECT bid, isbn, title FROM book;
END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE SelectBookByAuthorLastName()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_AuthLName AS
	SELECT book.bid, author.last_name as author_last_name
	FROM book
	LEFT JOIN author_book ab
	ON ab.book_id = book.bid
	INNER JOIN author
	ON ab.author_id = author.aid;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE SelectBookByAuthorFirstName()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_AuthFName AS
	SELECT book.bid, author.first_name as author_first_name
	FROM book
	LEFT JOIN author_book ab
	ON ab.book_id = book.bid
	INNER JOIN author
	ON ab.author_id = author.aid;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE SelectBookByGenre()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_genre AS
	SELECT book.bid, genre.name as genre
	FROM book
	INNER JOIN genre
	ON book.genre_id = genre.gid;
END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE SelectBookBySubject()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_subject AS
	SELECT book.bid, subject.name as subject
	FROM book
	LEFT JOIN subject_book bsubj
	ON bsubj.book_id = book.bid
	INNER JOIN subject
	ON bsubj.subject_id = subject.sid;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE SelectBookByLanguage()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS booktempCLI_language AS
	SELECT book.bid, language.name as language
	FROM book
	LEFT JOIN language_book blang
	ON blang.book_id = book.bid
	INNER JOIN language
	ON blang.language_id = language.lid;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE CreateMasterBookIndex()
BEGIN
	CALL SelectBookBaseIndexes();
	CALL SelectBookByAuthorLastName();
	CALL SelectBookByAuthorFirstName();
	CALL SelectBookByGenre();
	CALL SelectBookBySubject();
	CALL SelectBookByLanguage();

	DROP TABLE IF EXISTS MasterBookIndex;

	CREATE TEMPORARY TABLE IF NOT EXISTS MasterBookIndex AS
		SELECT * FROM booktempCLI_BaseIndex
		LEFT OUTER JOIN booktempCLI_AuthLName USING (bid)
		LEFT OUTER JOIN booktempCLI_AuthFName USING (bid)
		LEFT OUTER JOIN booktempCLI_genre USING (bid)
		LEFT OUTER JOIN booktempCLI_subject USING (bid)
		LEFT OUTER JOIN booktempCLI_language USING (bid);
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE CreateCardholderTableWithPhone()
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS cardholdertempCLI_phone AS
	SELECT card_num, first_name, last_name, addr_num, addr_street, addr_apt, addr_city, addr_state, addr_zip, email, phone_number
	FROM cardholder
	LEFT JOIN cardholder_phone cp
	ON cp.cardholder_id = cardholder.chid;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE DropTempSearchTables ()
BEGIN
	DROP TEMPORARY TABLE IF EXISTS cardholdertempCLI_phone, MasterBookIndex, booktempCLI_BaseIndex, booktempCLI_AuthLName, booktempCLI_AuthFName, booktempCLI_genre, booktempCLI_subject, booktempCLI_language;
END //
DELIMITER ;

-- Stored proc to check in a book and update its checked_in timestamp
DELIMITER //
CREATE PROCEDURE Check_in(copid int, chid int)
BEGIN
	UPDATE cardholder_copy
		SET checked_in = current_timestamp()
		WHERE copy_id = copid AND cardholder_id = chid AND checked_in IS NULL;
END //
DELIMITER ;

-- CALL Check_in(3811930,0);

-- Stored proc to check out a book and update its checked_out timestamp
DELIMITER //
CREATE PROCEDURE Check_out(copid int, chid int)
BEGIN
	INSERT INTO cardholder_copy VALUES(
		copid, chid, current_timestamp(), null
	);
END //
DELIMITER ;

-- TODO! Stored proc to use the copystatus temp table and get available
-- copies of specific books at specific branches

-- ----------------
-- Functions    -- 
-- ----------------


-- Function to return the due date-- 2 weeks from the checkout date
DELIMITER //
CREATE FUNCTION calculate_due_date(checkoutdate TIMESTAMP)
RETURNS TIMESTAMP DETERMINISTIC
BEGIN
	RETURN date_add(checkoutdate, interval 2 week);
END//
DELIMITER ;

-- SELECT cardholder_id, calculate_due_date(checked_out) AS due_dates FROM cardholder_copy WHERE checked_in IS NULL;
