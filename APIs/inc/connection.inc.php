<?php



function get_mysql_connection() {
	$connect_error = 'Could not connect';
	$mysql_host = 'localhost';
	$mysql_user = 'root';
	$mysql_pass = 'root';
	$mysql_data = 'csinseew_pg_tie';
	$connection = mysqli_connect($mysql_host , $mysql_user , $mysql_pass ,$mysql_data);

        if (mysqli_connect_errno()) {
	    die($connect_error);
        } 
	return $connection;
}
