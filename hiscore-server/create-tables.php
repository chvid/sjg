<?php

require_once "./db.php";

$sql = "CREATE TABLE hiscore_entry (";
$sql = "$sql\n  id int(10) NOT NULL auto_increment,";
$sql = "$sql\n  date datetime NOT NULL,";
$sql = "$sql\n  game text,";
$sql = "$sql\n  text text,";
$sql = "$sql\n  round text,";
$sql = "$sql\n  score int(10),";
$sql = "$sql\n  PRIMARY KEY (id)";
$sql = "$sql\n);";

echo "<pre>$sql</pre>";
k_query($sql);

echo "<p>done ...</p>"

?>
