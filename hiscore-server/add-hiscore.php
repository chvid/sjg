<?php 
require_once "./db.php";

$game = $HTTP_GET_VARS["game"];
$text = $HTTP_GET_VARS["text"];
$score = $HTTP_GET_VARS["score"];
$round = $HTTP_GET_VARS["round"];

k_query("INSERT INTO hiscore_entry (game, text, score, round, date) values('$game', '$text', '$score', '$round', NOW())");

// make sure that there are only 50 entries in the hiscore list
	
$entries = k_query("SELECT COUNT(*) FROM hiscore_entry WHERE game = '$game';");
$row = mysql_fetch_row($entries);
	
if ($row[0] > 50) {
  $rows = k_query("SELECT min(score) FROM hiscore_entry WHERE game = '$game'");
  $rows = mysql_fetch_row($rows);
  $lowest_score = $rows[0];
  
  k_query("DELETE FROM hiscore_entry WHERE score = '$lowest_score' and game = '$game'");
}

?>
