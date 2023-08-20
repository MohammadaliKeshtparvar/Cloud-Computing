<?php

$user_name = "ihweb_31109160";
$server_name = "sql206.ihweb.ir";
$pass_word = "qwqwqw";
$db_name = "ihweb_31109160_cloud_computing";

$film = $_GET['film'];
$language = $_GET['language'];

$conn = new mysqli($server_name, $user_name, $pass_word, $db_name);
header('Content-Type: application/json');
if ($conn->connect_error) {
    echo json_encode(array(
        "size" => -1
    ));
    die("Connection failed: " . " Error");
}

$sql = "select text, username from comment where film = '" . $film . "' and allowed = 1;";
$result = $conn->query($sql);
$result_array = array();
$translate_array = array();
while ($row = mysqli_fetch_array($result, MYSQLI_ASSOC)) {
    array_push($translate_array, $row['text']);
    array_push($result_array, $row);
}
mysqli_close($conn);

if ($language == 'German') {
    $result_array = translator($translate_array, 'en-de', $result_array);
} else if ($language == 'Spanish') {
    $result_array = translator($translate_array, 'en-es', $result_array);
}

echo json_encode(array(
    "size" => sizeof($result_array),
    "comments" => $result_array
), JSON_PRETTY_PRINT);

function translator($comments, $language, $result)
{
    $url = "https://api.eu-gb.language-translator.watson.cloud.ibm.com/instances/71188fe6-6b17-4f5b-853a-cd697ce5c0cb/v3/translate?version=2018-05-01";
    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
    $headers = array(
        "Content-Type: application/json",
        "Authorization: Basic YXBpa2V5OnZfUWZjVS0yaGhNRkc1WFJQM0s4QTE1RWlJR0RsTlhCZXd3bmo0RE55Vy1M",
    );
    curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);

    $data = '{"text":[' . '"' . implode('", "', $comments) . '"],"model_id":"' . $language . '"}';
    curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
    //for debug only!
    curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
    $resp = curl_exec($curl);
    $resp = json_decode($resp, true);
    for ($x = 0; $x < sizeof($resp['translations']); $x++) {
        $result[$x]['text'] = $resp['translations'][$x]['translation'];
    }
    curl_close($curl);
    return $result;
}
