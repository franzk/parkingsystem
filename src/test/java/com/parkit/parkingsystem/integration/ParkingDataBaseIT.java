package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.util.PricesUtil;

import junit.framework.AssertionFailedError;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	void setUpPerTest() throws Exception {
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	static void tearDown() {

	}

	@Test
	public void testParkingACar() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testParkingAVehicle();
	}

	@Test
	public void testParkingABike() {
		when(inputReaderUtil.readSelection()).thenReturn(2);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testParkingAVehicle();
	}

	@Test
	public void testParkingANonExistingVehicleType() {
		when(inputReaderUtil.readSelection()).thenReturn(500);
		assertThrows(AssertionFailedError.class, () -> testParkingAVehicle());
	}

	public void testParkingAVehicle() {

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		ParkingType parkingType = null;

		switch (inputReaderUtil.readSelection()) {
			case 1:
				parkingType = ParkingType.CAR;
				break;
			case 2:
				parkingType = ParkingType.BIKE;
				break;
			default:
				throw new AssertionFailedError("Error with selection");
		}

		// store the next available parking before the incoming process
		// if the new next available place after the process is not the same as the one
		// before, it means that the place has been occupied.
		int nextAvailableParkingPlace = parkingSpotDAO.getNextAvailableSlot(parkingType);

		parkingService.processIncomingVehicle();

		// check that a ticket is actualy saved in DB and Parking table is updated with
		// availability
		try {

			Ticket ticketToCheck = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
			assertThat(ticketToCheck.getInTime()).isNotNull();

			// verify that the new available place is not the one that was free before the
			// process
			int newNextAvailableParkingPlace = parkingSpotDAO.getNextAvailableSlot(parkingType);
			assertThat(newNextAvailableParkingPlace).isNotEqualTo(nextAvailableParkingPlace);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error while checking ticket existence");
		}

	}

	@Test
	@Tag("ErrorTest")
	public void testNotExistingVehicleType() {
		when(inputReaderUtil.readSelection()).thenReturn(500);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());
	}

	@Test
	public void testParkingLotExit() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle(LocalDateTime.now().minusMinutes(5));

		parkingService.processExitingVehicle(LocalDateTime.now());

		// check that the fare generated and out time are populated correctly in
		// the database

		try {

			Ticket ticketToCheck = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());

			assertEquals(inputReaderUtil.readVehicleRegistrationNumber(), ticketToCheck.getVehicleRegNumber());
			assertThat(ticketToCheck.getOutTime()).isNotNull();
			assertThat(ticketToCheck.getPrice()).isNotNull();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error while checking ticket existence");
		}
	}

	@Test
	@Tag("ErrorTest")
	public void testVehicleNotPresentInTheParkingExit() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		assertThrows(RuntimeException.class, () -> parkingService.processExitingVehicle());
	}

	@Test
	@Tag("ErrorTest")
	public void testParkingACarThatIsAlreadyInTheParking() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 1 - Vehicle enters
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();

		// 2 - Vehicle enters again
		ParkingService parkingService2 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		assertThrows(RuntimeException.class, () -> parkingService2.processIncomingVehicle());

	}
	
	@Test
	public void testParkingLotExitWithRecurentUser() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 1 - Vehicle enters two days before now
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle(LocalDateTime.now().minusDays(2));

		// 2 - Vehicle exits one hour later
		parkingService.processExitingVehicle(LocalDateTime.now().minusDays(2).plusHours(1));

		// 3 - Vehicle enters today, one hour before now
		parkingService.processIncomingVehicle(LocalDateTime.now().minusHours(1));

		// 4 - Vehicle exits now
		parkingService.processExitingVehicle();

		// Assert
		try {

			Ticket ticketToCheck = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());

			assertEquals(inputReaderUtil.readVehicleRegistrationNumber(), ticketToCheck.getVehicleRegNumber());
			assertThat(ticketToCheck.getOutTime()).isNotNull();

			// 5% reduction because this is a recurrent client
			assertThat(ticketToCheck.getPrice()).isEqualTo(PricesUtil.roundToPrice(Fare.CAR_RATE_PER_HOUR * (1 - Fare.RECURRING_USER_DISCOUNT)));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error while checking ticket existence");
		}

	}

}
