<?php

/* Get the name of the uploaded file */
$file_name = $_FILES['file']['name'];
$file_type = $_FILES['file']['type'];
$username = $_POST['username'];
$film_name = $_POST['film'];

$user_name = "ihweb_31109160";
$server_name = "sql206.ihweb.ir";
$pass_word = "qwqwqw";
$db_name = "ihweb_31109160_cloud_computing";

$conn = new mysqli($server_name, $user_name, $pass_word, $db_name);
header('Content-Type: application/json');
if ($conn->connect_error) {
    echo json_encode(array(
        "result" => "Fail!!"
    ));
    die("Connection failed: " . $conn->connect_error);
}

$location = "./upload/" . $file_name;

/* Save the uploaded file to the local filesystem */
if (move_uploaded_file($_FILES['file']['tmp_name'], $location)) {
    $comment_text = speech_to_text($file_name, $file_type);
    $comment_text = str_replace("'", "", $comment_text);
    $allowed = checkAngry($comment_text);
    $sql_query = "INSERT INTO comment (text, film, username, allowed) VALUES ('" . $comment_text . "', '" . $film_name . "', '" . $username . "', '" . $allowed . "');";
    if ($conn->query($sql_query) === TRUE) {
        echo json_encode(array(
        "result" => "Success and (Allowed to wive : " . $allowed . ")"
        ));
    }else {
        echo $conn->error;
    }
    mysqli_close($conn);
} else {
    echo 'Failure';
}

function speech_to_text($file_name, $file_type)
{
    $url = "https://api.eu-gb.speech-to-text.watson.cloud.ibm.com/instances/dde8ffe2-1f83-4d35-a9f4-1d74bb4e5a7c/v1/recognize";

    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);

    $headers = array(
        "Content-Type: " . $file_type,
        "Authorization: Basic YXBpa2V5OnVjNzU2blJvV2gyS3FjeVR3QllIdFUzNHNVLU95SERsOGY3QWJJOEc0bDJt",
    );
    curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);

    $data = "./upload/" . $file_name;

    curl_setopt($curl, CURLOPT_POSTFIELDS, file_get_contents($data));

    //for debug only!
    curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);

    $resp = curl_exec($curl);
    if (curl_errno($curl)) {
        echo 'Error:' . curl_error($curl);
    }
    $resp = json_decode($resp, true);
    return $resp["results"][0]["alternatives"][0]["transcript"];
}

function checkAngry($text)
{
    if (strpos($text, '****') === 0) {
        return 0;
    }
    $url = "https://api.eu-gb.natural-language-understanding.watson.cloud.ibm.com/instances/c836267d-b792-40fa-bce7-575713301615/v1/analyze?version=2019-07-12";

    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_POST, true);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);

    $headers = array(
        "Content-Type: application/json",
        "Authorization: Basic YXBpa2V5OnlCdnNZTmg3ZEZYampEV2lNSXNvemNORFZWWWw0dm5OMTZRdkMyODJ5N2Nh",
    );
    curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);

    $data = '{ "text": "'.$text.'", "features": { "sentiment": { "targets": [ "apples", "oranges", "broccoli" ] }, "keywords": { "emotion": true } } }';

    curl_setopt($curl, CURLOPT_POSTFIELDS, $data);

    //for debug only!
    curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);

    $resp = curl_exec($curl);
    curl_close($curl);
    $resp = json_decode($resp, true);

    if (isset($resp["error"]))
        return true;

    $keywords = $resp["keywords"];
    $anger = 0;
    $sadness = 0;
    $joy = 0;
    $size = sizeof($keywords);
    if ($size == 0)
        return true;

    for ($i = 0; $i < $size; $i++) {
        $emotion = $resp["keywords"][$i]["emotion"];
        $anger += $emotion["anger"];
        $sadness += $emotion["sadness"];
        $joy += $emotion["joy"];
    }
    $anger /= $size;
    $sadness /= $size;
    $joy /= $size;
    if ($sadness > 0.9)
        return 0;
    if (($anger > 0.2) and $joy < 0.4)
        return 0;
    else
        return true;
}
