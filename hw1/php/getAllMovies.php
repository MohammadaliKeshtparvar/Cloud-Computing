<?php

$user_name = "ihweb_31109160";
$server_name = "sql206.ihweb.ir";
$pass_word = "qwqwqw";
$db_name = "ihweb_31109160_cloud_computing";

$data = $_GET;

$conn = new mysqli($server_name, $user_name, $pass_word, $db_name);
header('Content-Type: application/json');
if ($conn->connect_error) {
    echo json_encode(array(
        "size" => -1
    ));
    die("Connection failed: " . " Error");
}

$sql = "select * from film;";
$result = $conn->query($sql);
$result_array = array();
while ($row = mysqli_fetch_array($result, MYSQLI_ASSOC)) {
    array_push($result_array, $row);
}
mysqli_close($conn);

echo json_encode(array(
    "size" => sizeof($result_array),
    "movies" => $result_array
), JSON_PRETTY_PRINT);
