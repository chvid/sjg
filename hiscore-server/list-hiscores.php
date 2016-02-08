<?php 
require_once "./db.php";

$game = $HTTP_GET_VARS["game"];

$rows = k_query("SELECT text, round, score FROM hiscore_entry WHERE game = '$game' ORDER BY score DESC LIMIT 10");

while ($row = mysql_fetch_array($rows)) {
  $text = "$row[0] ";
  $round = $row[1];
  $score = $row[2];

  echo "$text,$round,$score\n";
}
  
?>
