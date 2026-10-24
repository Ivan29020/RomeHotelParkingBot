-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Creato il: Dic 19, 2024 alle 12:00
-- Versione del server: 10.4.32-MariaDB
-- Versione PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `romehotelparkingdb`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `parking`
--

CREATE TABLE `parking` (
  `id_parking` varchar(50) NOT NULL,
  `is_occupied` tinyint(1) NOT NULL,
  `name` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `parking`
--

INSERT INTO `parking` (`id_parking`, `is_occupied`, `name`) VALUES
('P001', 1, 'Car1'),
('P002', 1, 'Car2'),
('P003', 1, 'Car6'),
('P004', 1, 'Moto1'),
('P005', 1, 'Car5'),
('P006', 1, 'MotoGP'),
('P007', 0, NULL),
('P008', 0, NULL),
('P009', 0, NULL),
('P010', 0, NULL),
('P011', 0, NULL),
('P012', 0, NULL),
('P013', 0, NULL),
('P014', 0, NULL),
('P015', 0, NULL),
('P016', 0, NULL),
('P017', 0, NULL),
('P018', 0, NULL),
('P019', 0, NULL),
('P020', 0, NULL);

-- --------------------------------------------------------

--
-- Struttura della tabella `users`
--

CREATE TABLE `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(50) NOT NULL,
  `surname` varchar(50) NOT NULL,
  `role` enum('Client','Admin') DEFAULT NULL,
  `cf` varchar(15) DEFAULT NULL,
  `telephoneNumber` varchar(30) DEFAULT NULL,
  `salt` varchar(255) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `users`
--

INSERT INTO `users` (`username`, `password`, `name`, `surname`, `role`, `cf`, `telephoneNumber`, `salt`) VALUES
('asmith', 'XN8M7CTKoGSPUtBwV6AEpzzMYdDpRnmLA3zov1yiRQE=', 'Alice', 'Smith', 'Admin', 'ASMI123456789', '234-567-8901', 'ZlzhcxkJIZABHoxjmnXNOw=='),
('bjones', 'h0Yuw2JFDQOk6FZU/nsQhacSR9FnvkSLdkYsu1KaM6s=', 'Bob', 'Jones', 'Client', 'BJON123456789', '345-678-9012', 'tT7My2aQkFnukTAiStA8KA=='),
('cwhite', 'CNGB2kWAanJLI5FCcueoiWkPjPRqjPFuYytuAq9IXEw=', 'Carol', 'White', 'Client', 'CWHI123456789', '456-789-0123', '0phV3gUMs7wqds1uAmbHyg=='),
('david', 'HcmHTzUqmEe6QHLoegVWnA8Iwf/FskRwyozvWH5tB28=', 'David', 'Moonsmee', 'Client', 'DDSRGF23ADSF12', '56874923957', 'HEymHfdlYX0NxXjinx7y3w=='),
('dblack', 'JgI8AvzFaQpng4L4InM0Ga63+LC77t7LwXINBPp428A=', 'Dave', 'Black', 'Admin', 'DBLA123456789', '567-890-1234', 'R9L6ahnJXMJRmVI3iX7sbw=='),
('jdoe', 'Ud//qoUrygaBXWHqSj9TSS6Xzt/GqHszhCUov3EssRs=', 'John', 'Doe', 'Client', 'JDOE123456789', '123-456-7890', 'NPunVGXQtUYgEQyZhkYU1g==');

-- --------------------------------------------------------

--
-- Struttura della tabella `vehicle`
--

CREATE TABLE `vehicle` (
  `name` varchar(50) NOT NULL,
  `licensePlate` varchar(10) NOT NULL,
  `color` varchar(20) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `numberOfWheels` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `type` enum('Car','Bike') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `vehicle`
--

INSERT INTO `vehicle` (`name`, `licensePlate`, `color`, `brand`, `numberOfWheels`, `username`, `type`) VALUES
('Car1', 'ABC123', 'Red', 'Toyota', 4, 'jdoe', 'Car'),
('Car2', 'DEF456', 'Blue', 'Honda', 4, 'asmith', 'Car'),
('Car3', 'GHI789', 'Green', 'Ford', 4, 'bjones', 'Car'),
('Car4', 'JKL012', 'Yellow', 'Chevrolet', 4, 'cwhite', 'Car'),
('Car5', 'MNO345', 'Black', 'Tesla', 4, 'dblack', 'Car'),
('Car6', 'VWX234', 'Gray', 'BMW', 4, 'bjones', 'Car'),
('Car7', 'YZA567', 'Pink', 'Audi', 4, 'cwhite', 'Car'),
('Car8', 'BCD890', 'Purple', 'Mercedes', 4, 'dblack', 'Car'),
('Moto1', 'PQR678', 'White', 'Yamaha', 2, 'jdoe', 'Bike'),
('Moto2', 'STU901', 'Silver', 'Kawasaki', 2, 'asmith', 'Bike'),
('MotoGP', 'SD22SD6', 'Red', 'Suzuki', 2, 'david', 'Bike'),
('Pippo', 'SDF123D', 'red', 'SUV', 4, 'david', 'Car'),
('Puppo', 'FSG35SA2', 'Black', 'Suv', 4, 'david', 'Car');

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `parking`
--
ALTER TABLE `parking`
  ADD PRIMARY KEY (`id_parking`),
  ADD KEY `name` (`name`);

--
-- Indici per le tabelle `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`username`),
  ADD UNIQUE KEY `name` (`name`),
  ADD UNIQUE KEY `name_2` (`name`);

--
-- Indici per le tabelle `vehicle`
--
ALTER TABLE `vehicle`
  ADD PRIMARY KEY (`name`),
  ADD UNIQUE KEY `licensePlate` (`licensePlate`),
  ADD KEY `username` (`username`);

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `parking`
--
ALTER TABLE `parking`
  ADD CONSTRAINT `Parking_ibfk_1` FOREIGN KEY (`name`) REFERENCES `vehicle` (`name`) ON DELETE CASCADE;

--
-- Limiti per la tabella `vehicle`
--
ALTER TABLE `vehicle`
  ADD CONSTRAINT `Vehicle_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
