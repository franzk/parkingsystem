package com.parkit.parkingsystem.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;

public class ParkingSpotDAOTest {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

	private static ParkingSpotDAO parkingSpotDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@BeforeEach
	void setUpPerTest() {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
		dataBasePrepareService.clearDataBaseEntries();
	}

	@Test
	public void getNextAvailableSlotTest() {
		ParkingType parkingType = ParkingType.CAR;
		
		int availableSlot = parkingSpotDAO.getNextAvailableSlot(parkingType);
		
		assertThat(availableSlot).isGreaterThanOrEqualTo(0);
	}

	@Test
	 public void updateParkingTest() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		assertThat(parkingSpotDAO.updateParking(parkingSpot)).isTrue();
	}
}
