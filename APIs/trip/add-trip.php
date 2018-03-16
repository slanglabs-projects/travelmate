<?php

	require_once '../inc/connection.inc.php';
	require_once '../inc/function.inc.php';

	$response = array();

        $connection = get_mysql_connection();

        if (defined('STDIN')) {
          $user_id = $argv[1];
          $trip_title = $argv[2];
          $trip_start_time = $argv[3];
          $trip_city = $argv[4];
        } else { 
          $user_id = $_GET['user'];
          $trip_title = $_GET['title'];
          $trip_start_time = $_GET['start_time'];
          $trip_city = $_GET['city'];
        }

	if (isset($_GET['end_time'])) {
		$trip_end_time = (int)$_GET['end_time'];
		$query = "INSERT INTO `trips` (`city_id`,`title`,`start_time`,`end_time`)
			VALUES ('$trip_city','$trip_title','$trip_start_time','$trip_end_time')";
	} else {
		$query = "INSERT INTO `trips` (`city_id`,`title`,`start_time`)
			VALUES ('$trip_city','$trip_title','$trip_start_time')";
	}

	if (mysqli_query($connection, $query)) {
		$trip_id = (int)mysqli_insert_id($connection);

		$query_ins = "INSERT INTO `trip_users` (`trip_id`,`user_id`)
			VALUES ('$trip_id', '$user_id')";

		if (mysqli_query($connection, $query_ins)) {
                        echo "success";
			$success = 1;
			increase_trip_count($connection, $trip_city);
		} else {
                        echo "fail 1";
			$success = 0;
		}
	} else {
        	echo "Failed to query to MySQL: " . mysqli_error($connection);
		$success = 0;
	}

	$response['success'] = (bool)$success;

	echo json_encode($response);
