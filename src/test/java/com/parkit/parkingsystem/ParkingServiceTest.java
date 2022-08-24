package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ErrorMessages;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import nl.altindag.log.LogCaptor;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	private static LogCaptor logCaptor;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;
	@Mock
	private static FareCalculatorService fareCalculatorService;

	@BeforeAll
	public static void setupLogCaptor() {
		logCaptor = LogCaptor.forClass(ParkingService.class);
	}

	@AfterEach
	public void clearLogs() {
		logCaptor.clearLogs();
	}

	@AfterAll
	public static void tearDown() {
		logCaptor.close();
	}

	@BeforeEach
	void setUpPerTest() {
		try {
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void processIncomingCarTest() {

		when(inputReaderUtil.readSelection()).thenReturn(1);

		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
	public void processIncomingBikeTest() {

		when(inputReaderUtil.readSelection()).thenReturn(2);

		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
	@Tag("ErrorTest")
	public void processIncomingWrongVehicleTypeTest() {

		when(inputReaderUtil.readSelection()).thenReturn(500);

		assertThrows(IllegalArgumentException.class, () -> parkingService.processIncomingVehicle());
	}

	@Test
	@Tag("ErrorTest")
	public void processIncomingWhenParkingIsFullTest() {

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(LocalDateTime.now().minusHours(1));
		ticket.setOutTime(null);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		assertThrows(RuntimeException.class, () -> parkingService.processIncomingVehicle());
	}

	@Test
	@Tag("ErrorTest")
	public void processIncomingWithErrorReadingVehicleRegNumberTest() {

		when(inputReaderUtil.readSelection()).thenReturn(1);

		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(Exception.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());
	}

	@Test
	@Tag("ErrorTest")
	public void processIncomingVehicleWithVehicleAllreadyInParkingTest() {

		Ticket ticket = new Ticket();
		ticket.setOutTime(null);

		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

		assertThrows(RuntimeException.class, () -> parkingService.processIncomingVehicle());
		
		
	}

	@Test
	@Tag("ErrorTest")
	public void processIncomingCarWithRecurringUserTest() {

		Ticket ticket = new Ticket();
		ticket.setOutTime(LocalDateTime.now());

		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}
	
	
	@Test
	@Tag("ErrorTest")
	public void processIncomingCarWithErrorWhileGettingNextAvailableSlotTest() {
		try {
			when(inputReaderUtil.readSelection()).thenReturn(1);
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());
		
	}
	
	@Test
	@Tag("ErrorTest")
	public void getNextParkingNumberIfAvailableWithWrongParkingTypeTest() {

		when(inputReaderUtil.readSelection()).thenReturn(500);
		assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
	}

	public void processExitingVehicleForTests() {

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(LocalDateTime.now().minusHours(1));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parkingService.processExitingVehicle();

	}

	@Test
	public void processExitingVehicleTest() {
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		processExitingVehicleForTests();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
	@Tag("ErrorTest")
	public void processExitingVehicleWithUpdateTicketErrorTest() {
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
		processExitingVehicleForTests();
		assertThat(logCaptor.getErrorLogs().contains(ErrorMessages.UPDATE_TICKET));
	}

	@Test
	@Tag("ErrorTest")
	public void processExitingVehicleTestWithWrongRegNumberTest() {
		when(ticketDAO.getTicket(anyString())).thenReturn(null);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEFG");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertThrows(RuntimeException.class, () -> parkingService.processExitingVehicle());
	}

	@Test
	@Tag("ErrorTest")
	public void processExitingVehicleTestWithErrorGettingVehicleRegNumberTest() {

		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(Exception.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		parkingService.processExitingVehicle();
		assertThat(logCaptor.getErrorLogs().contains(ErrorMessages.PROCESS_EXITING_VEHICLE_REGNUMBER_PROBLEM));
	}

}