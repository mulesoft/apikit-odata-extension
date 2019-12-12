-- Cleans up Order inserted by "post-entity" test case.
DELETE FROM `Orders` WHERE OrderID=20000;

-- Cleans up Order inserted by "post-entity-as-json" test case.
DELETE FROM `Customers` WHERE CustomerID='JAB';

-- Restores Order deleted by "delete-entity" test case
SET FOREIGN_KEY_CHECKS=0;
INSERT INTO `Orders` SELECT 10480,'FOLIG',6,'1997-03-20 00:00:00','1997-04-17 00:00:00','1997-03-24 00:00:00',2,1.3500,'Folies gourmandes','184, chausse de Tournai','Lille',NULL,'59000','France',NULL,1 WHERE NOT EXISTS (SELECT 1 FROM `Orders` WHERE OrderID=10480 AND ShipName='Folies gourmandes');
SET FOREIGN_KEY_CHECKS=1;