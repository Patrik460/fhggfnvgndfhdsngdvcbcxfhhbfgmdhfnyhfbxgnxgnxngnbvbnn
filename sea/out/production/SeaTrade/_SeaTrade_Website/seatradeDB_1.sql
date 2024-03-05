-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server-Version:               10.1.32-MariaDB - mariadb.org binary distribution
-- Server-Betriebssystem:        Win32
-- HeidiSQL Version:             12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Exportiere Datenbank-Struktur für seatradedb
CREATE DATABASE IF NOT EXISTS `seatradedb` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `seatradedb`;

-- Exportiere Struktur von Tabelle seatradedb.cargo
CREATE TABLE IF NOT EXISTS `cargo` (
  `CargoID` int(11) NOT NULL,
  `Value` int(11) DEFAULT NULL,
  `HarbourIDStart` int(11) DEFAULT NULL,
  `HarbourIDDest` int(11) DEFAULT NULL,
  `Status` tinyint(4) DEFAULT NULL COMMENT '0: Waiting 1: Loaded 2: Done 3: Lost',
  PRIMARY KEY (`CargoID`),
  KEY `HarbourIDStart` (`HarbourIDStart`),
  KEY `HarbourIDDest` (`HarbourIDDest`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle seatradedb.cargo: ~6 rows (ungefähr)
INSERT INTO `cargo` (`CargoID`, `Value`, `HarbourIDStart`, `HarbourIDDest`, `Status`) VALUES
	(1, 20000, 1, 5, 1),
	(2, 30000, 2, 4, 2),
	(3, 35000, 3, 2, 3),
	(4, 40000, 5, 4, 0),
	(5, 25000, 4, 1, 0),
	(6, 45000, 5, 4, 3);

-- Exportiere Struktur von Tabelle seatradedb.company
CREATE TABLE IF NOT EXISTS `company` (
  `CompanyID` int(11) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `Balance` int(11) DEFAULT NULL,
  PRIMARY KEY (`CompanyID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle seatradedb.company: ~2 rows (ungefähr)
INSERT INTO `company` (`CompanyID`, `Name`, `Balance`) VALUES
	(1, 'Quickstart', 2000000),
	(2, 'Heinrich', 1000000);

-- Exportiere Struktur von Tabelle seatradedb.harbour
CREATE TABLE IF NOT EXISTS `harbour` (
  `HarbourID` int(11) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `PosX` int(11) DEFAULT NULL,
  `PosY` int(11) DEFAULT NULL,
  PRIMARY KEY (`HarbourID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle seatradedb.harbour: ~4 rows (ungefähr)
INSERT INTO `harbour` (`HarbourID`, `Name`, `PosX`, `PosY`) VALUES
	(1, 'Ney York', 8, 14),
	(2, 'Brest', 15, 6),
	(3, 'Den Haag', 22, 17),
	(4, 'Hafen 4', 2, 6),
	(5, 'Karibik', 20, 10);

-- Exportiere Struktur von Tabelle seatradedb.ship
CREATE TABLE IF NOT EXISTS `ship` (
  `ShipID` int(11) NOT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `PosX` int(11) DEFAULT NULL,
  `PosY` int(11) DEFAULT NULL,
  `CargoID` int(11) DEFAULT NULL,
  `HarbourID` int(11) DEFAULT NULL,
  `CompanyID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ShipID`),
  KEY `CargoID` (`CargoID`),
  KEY `HarbourID` (`HarbourID`),
  KEY `CompanyID` (`CompanyID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle seatradedb.ship: ~4 rows (ungefähr)
INSERT INTO `ship` (`ShipID`, `Name`, `PosX`, `PosY`, `CargoID`, `HarbourID`, `CompanyID`) VALUES
	(1, 'AIDA', 11, 11, 2, NULL, 1),
	(2, 'Floos1', 8, 5, 1, NULL, 2),
	(3, 'Speedboot', 22, 17, NULL, 3, 1),
	(4, 'Kanu', 8, 14, NULL, 1, 2);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
