<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script>
window.onload = function(){setInterval(function(){
$.ajax(
			 {
					 url: 'notif.php',
					 type: 'GET'
				}
		);
},10000);}
</script>

<?php

function sendFCM($mess,$id,$isSup,$isOrganic,$price,$weight,
			$originalWeight,$originalLat,$originalLon,$originalAddress,$originalBid,$originalDateFrom,$originalDateTo,
			$overlapDateFrom,$overlapDateTo,$pairNumber,$pairAddress,$pairName,$transactionID) {
$url = 'https://fcm.googleapis.com/fcm/send';
$fields = array (
        'to' => $id,
        'notification' => array (
                "body" => $mess,
                "title" => "You have a match!",
                "icon" => "myicon",
								"sound" => "default",
								"click_action" => "OPEN_MATCHED_DIALOG"
        ),
				'data' => array (
								"isSup" => $isSup,
								"isOrganic" => $isOrganic,
								"price" => $price,
								"weight" => $weight,
								"originalWeight" => $originalWeight,
								"originalLat" => $originalLat,
								"originalLon" => $originalLon,
								"originalAddress" => $originalAddress,
								"originalBid" => $originalBid,
								"originalDateFrom" => $originalDateFrom,
								"originalDateTo" => $originalDateTo,
								"overlapDateFrom" => $overlapDateFrom,
								"overlapDateTo" => $overlapDateTo,
								"pairNumber" => $pairNumber,
								"pairAddress" => $pairAddress,
								"pairName" => $pairName,
								"transactionID" => $transactionID
				)
);
$fields = json_encode ( $fields );
$headers = array (
        'Authorization: key=' . "AAAAkCCO7T8:APA91bEHoSDiLzQ3ey9NH5iKOIPZAMO_L_dIU0fZf25pi8IgOv1EcOoES6nUkJoQq-0Jm1a1SVujd98PBucW-HUDKUxnesDr_5HtFF3jftmAkMvEgPjwhG-648eKvf71-pGErot-IVK7",
        'Content-Type: application/json'
);

$ch = curl_init ();
curl_setopt ( $ch, CURLOPT_URL, $url );
curl_setopt ( $ch, CURLOPT_POST, true );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
curl_setopt ( $ch, CURLOPT_POSTFIELDS, $fields );

$result = curl_exec ( $ch );
curl_close ( $ch );
}



$db = mysqli_connect('147.8.133.49:3306','shake','shake','s2s')
  or die('Error connecting to MySQL server.');
$query =
"SELECT
    s2s.matched_orders.*,
    sh_S.address addressSup,
    sh_D.address addressDem,
    u_i_S.name nameSup,
    u_i_D.name nameDem,
    sh_S.phone_number phoneSup,
    sh_D.phone_number phoneDem,
    sh_S.dateFrom dateFromSup,
    sh_D.dateFrom dateFromDem,
    sh_S.dateTo dateToSup,
    sh_D.dateTo dateToDem,
    sh_S.weight weightSup,
    sh_D.weight weightDem,
    sh_S.latitude latSup,
    sh_D.latitude latDem,
    sh_S.longitude lonSup,
    sh_D.longitude lonDem,
		sh_S.bid bidSup,
    sh_D.bid bidDem,
    sh_S.organic isOrganic,
    u_S.token tokenSup,
    u_D.token tokenDem
FROM
    s2s.matched_orders
        LEFT JOIN
    s2s.share_history sh_S ON sh_S.id = matched_orders.idS
        LEFT JOIN
    s2s.share_history sh_D ON sh_D.id = matched_orders.idD
        JOIN
    s2s.user u_S ON u_S.phone_number = sh_S.phone_number
        JOIN
    s2s.user u_D ON u_d.phone_number = sh_D.phone_number
		JOIN
	s2s.user_info u_i_S ON u_i_S.phone_number = u_S.phone_number
		JOIN
    s2s.user_info u_i_D on u_i_D.phone_number = u_D.phone_number
WHERE
	notified = 0;";

function setNotified($id, $db) {
	$updateQuery =
	"UPDATE
			s2s.matched_orders
	SET
	    notified = 1
	WHERE
	    id = " . $id;
	$result = mysqli_query($db, $updateQuery) or die('Error querying database.');
}

function setDone($idS, $idD, $db) {
	$updateQuery =
	"UPDATE
			s2s.share_history
	SET
	    done = 1
	WHERE
	    id = " . $idS . " OR  id = " . $idD;
	$result = mysqli_query($db, $updateQuery) or die('Error querying database.');
}

function queryDatabase($query, $db) {
	$result = mysqli_query($db, $query) or die('Error querying database.');
	while ($row = mysqli_fetch_array($result)) {
		sendFCM("Found a demander", $row['tokenSup'], "1", $row['isOrganic'],
						$row['price'], $row['weight'], $row['weightSup'], $row['latSup'],
						$row['lonSup'], $row['addressSup'], $row['bidSup'], $row['dateFromSup'],
						$row['dateToSup'], $row['overlap_dateFrom'], $row['overlap_dateTo'],
						$row['phoneDem'], $row['addressDem'], $row['nameDem'], $row['idS']);
		sendFCM("Found a supplier", $row['tokenDem'], "0", $row['isOrganic'],
						$row['price'], $row['weight'], $row['weightDem'], $row['latSup'],
						$row['lonSup'], $row['addressDem'], $row['bidDem'], $row['dateFromSup'],
						$row['dateToSup'], $row['overlap_dateFrom'], $row['overlap_dateTo'],
						$row['phoneSup'], $row['addressSup'], $row['nameSup'], $row['idD']);
		setNotified($row['id'], $db);
	}
}

queryDatabase($query, $db);


function match($idS, $idD, $db) {
		setDone($idS, $idD, $db);
		$querySup =
		"SELECT *
		FROM s2s.share_history
		WHERE id = " . $idS;
		$queryDem =
		"SELECT *
		FROM s2s.share_history
		WHERE id = " . $idD;
		$resultSup = mysqli_query($db, $querySup) or die('Error querying database.');
		$priceSup = 0;
		$weightSup = 0;
		$dateFromSup = 0;
		$dateToSup = 0;
		$priceDem = 0;
		$weightDem = 0;
		$dateFromDem = 0;
		$dateToDem = 0;
		while ($row = mysqli_fetch_array($resultSup)) {
			$priceSup = $row['bid'];
			$weightSup = $row['weight'];
			$dateFromSup = DateTime::createFromFormat('Y/m/d', $row['dateFrom']);
			$dateToSup = DateTime::createFromFormat('Y/m/d', $row['dateTo']);
		}
		$resultDem = mysqli_query($db, $queryDem) or die('Error querying database.');
		while ($row = mysqli_fetch_array($resultDem)) {
			$priceDem = $row['bid'];
			$weightDem = $row['weight'];
			$dateFromDem = DateTime::createFromFormat('Y/m/d', $row['dateFrom']);
			$dateToDem = DateTime::createFromFormat('Y/m/d', $row['dateTo']);
		}
		$weight = min($weightSup,$weightDem);
		$price = min($priceSup,$priceDem);
		$ranges = array(
    array('start' => $dateFromSup, 'end' => $dateToSup),
    array('start' => $dateFromDem, 'end' => $dateToDem));
		$overlapDates = checkDateOverlap($ranges);
		$overlapDateFrom = $overlapDates['start']->format('Y/m/d');
		$overlapDateTo = $overlapDates['end']->format('Y/m/d');
		$values = $idS . "," . $idD . "," . "\"" . date("Y/m/d") . "\"" . "," . $price . "," . "0," . $weight . ",\"" . $overlapDateFrom . "\",\"" . $overlapDateTo . "\"";
		$insertQuery =
		"INSERT INTO s2s.matched_orders
		(idS, idD, date, price, notified, weight, overlap_dateFrom, overlap_dateTo)
		VALUES (" . $values . ");";
		$result = mysqli_query($db, $insertQuery) or die('Error querying database.');
}

function checkDateOverlap($ranges) {
    $res = $ranges[0];
    $countRanges = count($ranges);

    for ($i = 0; $i < $countRanges; $i++) {

        $r1s = $res['start'];
        $r1e = $res['end'];

        $r2s = $ranges[$i]['start'];
        $r2e = $ranges[$i]['end'];

        if ($r1s >= $r2s && $r1s <= $r2e || $r1e >= $r2s && $r1e <= $r2e || $r2s >= $r1s && $r2s <= $r1e || $r2e >= $r1s && $r2e <= $r1e) {
            $res = array(
                'start' => $r1s > $r2s ? $r1s : $r2s,
                'end' => $r1e < $r2e ? $r1e : $r2e
            );
        } else
            return false;
    }
    return $res;
}

mysqli_close($db);

?>
