<?php
$servername = "127.0.0.1:3302";
$username = "root";
$password = "";
$dbname = "seatradedb";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch data from the cargo table
$sqlCargo = "SELECT CargoID, value FROM cargo";
$resultCargo = $conn->query($sqlCargo);

if ($resultCargo->num_rows > 0) {
    $cargoData = [];
    while($rowCargo = $resultCargo->fetch_assoc()) {
        $cargoData[] = $rowCargo;
    }
} else {
    $cargoData = "No data found in cargo table";
}

// Fetch data from the company table
$sqlCompany = "SELECT name, balance FROM company";
$resultCompany = $conn->query($sqlCompany);

if ($resultCompany->num_rows > 0) {
    $companyData = [];
    while($rowCompany = $resultCompany->fetch_assoc()) {
        $companyData[] = $rowCompany;
    }
} else {
    $companyData = "No data found in company table";
}

// Fetch data from the harbour table
$sqlHarbour = "SELECT * FROM harbour";
$resultHarbour = $conn->query($sqlHarbour);

if ($resultHarbour->num_rows > 0) {
    $harbourData = [];
    while($rowHarbour = $resultHarbour->fetch_assoc()) {
        $harbourData[] = $rowHarbour;
    }
} else {
    $harbourData = "No data found in harbour table";
}

// Fetch data from the ship table
$sqlShip = "SELECT * FROM ship";
$resultShip = $conn->query($sqlShip);

if ($resultShip->num_rows > 0) {
    $shipData = [];
    while($rowShip = $resultShip->fetch_assoc()) {
        $shipData[] = $rowShip;
    }
} else {
    $shipData = "No data found in ship table";
}

$conn->close();

// Return the data as JSON
header('Content-Type: application/json');
echo json_encode(['cargoData' => $cargoData, 'companyData' => $companyData, 'harbourData' => $harbourData, 'shipData' => $shipData]);
?>
