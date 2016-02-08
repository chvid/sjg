<?php

$mysql_link = false;
$num_querys=0;

function open_connection() {
	global $num_queries;
	global $mysql_link;
	require("config.php");
	$mysql_link = mysql_connect($mysql_server, $mysql_user, $mysql_pwd) or die("Error in: $PHP_SELF Error: " .mysql_error());
	mysql_select_db($mysql_database,$mysql_link)or die("Error in: $PHP_SELF Error: " .mysql_error());
}

function k_query($request) {
	global $num_queries;
	global $mysql_link;
	if ($mysql_link==false){
		open_connection();
	}
	$num_queries = $num_queries +1;
	$result=mysql_query($request,$mysql_link) or die("$request<br>Error in: " .mysql_error());
	return $result;
}

// filter for insertion into db

function i_filter($i) {
  return $i;
}

// text to html filter for pretty output

function o_filter($i) {
  // text that starts html: gets passed through

  if (substr($i, 0, 5) == "html:") return substr($i, 5, strlen($i));

  $i = ereg_replace("(<)", "&lt;", $i);  
  $i = ereg_replace("(>)", "&gt;", $i);  

  // links to true links

  $i = ereg_replace("(\n)", "<p>\n\n", $i);

  return $i;
}

?>
