package com.parkit.parkingsystem.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAOTest {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@BeforeEach
	void setUpPerTest() {
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;

		dataBasePrepareService = new DataBasePrepareService();
		dataBasePrepareService.clearDataBaseEntries();
	}

	@Test
	public void saveAndGetTicketTest() {

		// arrange
		String vehicleRegNumber = "ABCDEF";
		double price = 12.54;
		LocalDateTime inTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		// 1 - Save
		Ticket ticketToSave = new Ticket();
		ticketToSave.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
		ticketToSave.setVehicleRegNumber(vehicleRegNumber);
		ticketToSave.setPrice(price);
		ticketToSave.setInTime(inTime);
		ticketToSave.setOutTime(null);

		ticketDAO.saveTicket(ticketToSave);

		// 2 - Get
		Ticket ticketToGet = ticketDAO.getTicket(vehicleRegNumber);

		// 3 - Assert
		assertThat(ticketToGet).isNotNull();
		assertThat(ticketToGet.getId()).isGreaterThanOrEqualTo(0);
		assertThat(ticketToGet.getPrice()).isEqualTo(price);
		assertThat(ticketToGet.getInTime()).isEqualTo(inTime);

	}

	@Test
	public void saveUpdateAndGetTicketTest() {

		// arrange
		String vehicleRegNumber = "ABCDEF";
		double price = 12.54;
		double newPrice = 24.66;
		LocalDateTime inTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime outTime = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);

		// 1 - Save
		Ticket ticketToCreate = new Ticket();
		ticketToCreate.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
		ticketToCreate.setVehicleRegNumber(vehicleRegNumber);
		ticketToCreate.setPrice(price);
		ticketToCreate.setInTime(inTime);
		ticketToCreate.setOutTime(null);

		ticketDAO.saveTicket(ticketToCreate);

		// 2 - GET
		Ticket ticketToUpdate = ticketDAO.getTicket(vehicleRegNumber);

		// 2 - Update Ticket
		ticketToUpdate.setPrice(newPrice);
		ticketToUpdate.setOutTime(outTime);
		ticketDAO.updateTicket(ticketToUpdate);

		// 3 - Final Get
		Ticket ticketToTest = ticketDAO.getTicket(vehicleRegNumber);

		// 3 - Assert
		assertThat(ticketToTest).isNotNull();
		assertThat(ticketToTest.getPrice()).isEqualTo(newPrice);
		assertThat(ticketToTest.getOutTime()).isEqualTo(outTime);

	}

	@Test
	public void isRecurringVehicleTest() {

		// arrange
		String vehicleRegNumber = "ABCDEF";
		double price = 12.54;
		LocalDateTime inTime = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime outTime = LocalDateTime.now();

		// 1 - Save
		Ticket ticketToSave = new Ticket();
		ticketToSave.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
		ticketToSave.setVehicleRegNumber(vehicleRegNumber);
		ticketToSave.setPrice(price);
		ticketToSave.setInTime(inTime);
		ticketToSave.setOutTime(outTime);

		ticketDAO.saveTicket(ticketToSave);
		
		boolean vehicleIsRecurring = ticketDAO.isRecurringVehicle(vehicleRegNumber);
		
		assertThat(vehicleIsRecurring).isTrue();
		

	}

}
