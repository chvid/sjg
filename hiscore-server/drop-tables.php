<?php

require_once "./db.php";

$sql = "DROP TABLE hiscore_entry;";
echo "<pre>$sql</pre>";
k_query($sql);

echo "<p>done ...</p>"

?>
