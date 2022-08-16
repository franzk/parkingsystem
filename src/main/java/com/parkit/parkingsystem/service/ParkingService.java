package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ErrorMessages;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {

	private static final Logger logger = LogManager.getLogger(ParkingService.class);

	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle(LocalDateTime inTime) {

		String vehicleRegNumber = "";

		try {
			vehicleRegNumber = getVehichleRegNumber();
		} catch (Exception e) {
			logger.error(ErrorMessages.GET_VEHICLE_REG_NUMBER, e);
		}

		// first check if this vehicle is not already in the parking

		Ticket checkPreviousTicket = ticketDAO.getTicket(vehicleRegNumber);
		if ((checkPreviousTicket != null) && (checkPreviousTicket.getOutTime() == null)) {
			throw new RuntimeException("This vehicle is already in the parking");
		}
		
		ParkingSpot parkingSpot = null;
		try {
			parkingSpot = getNextParkingNumberIfAvailable();

		} 
		catch (IllegalArgumentException e) {
			logger.error("Unable to process incoming vehicle", e);
			throw new IllegalArgumentException(e);
		}
		catch (Exception e) {
			logger.error("Unable to process incoming vehicle", e);
			throw new RuntimeException(e);
		}

		try {

			if (parkingSpot != null && parkingSpot.getId() > 0) {
				parkingSpot.setAvailable(false);
				parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
															// false

				Ticket ticket = new Ticket();
				// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
				// ticket.setId(ticketID);
				ticket.setParkingSpot(parkingSpot);
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(0);
				ticket.setInTime(inTime);
				ticket.setOutTime(null);
				ticketDAO.saveTicket(ticket);
				System.out.println("Generated Ticket and saved in DB");
				System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
				System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
			}

		} catch (Exception e) {
			logger.error("Unable to process incoming vehicle", e);
		}

	}

	public void processIncomingVehicle() {
		processIncomingVehicle(LocalDateTime.now());
	}

	private String getVehichleRegNumber() throws Exception {
		System.out.println("Please type the vehicle registration number and press enter key");
		return inputReaderUtil.readVehicleRegistrationNumber();
	}

	public ParkingSpot getNextParkingNumberIfAvailable() {
		int parkingNumber = 0;
		ParkingSpot parkingSpot = null;
		ParkingType parkingType = getVehichleType();
		try {
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);

			if (parkingNumber > 0) {
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			} else {
				throw new RuntimeException("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("Error parsing user input for type of vehicle", ie);
		}/* catch (Exception e) {
			logger.error("Error fetching next available parking slot", e);
		}*/
		return parkingSpot;
	}

	private ParkingType getVehichleType() {
		System.out.println("Please select vehicle type from menu");
		System.out.println("1 CAR");
		System.out.println("2 BIKE");
		int input = inputReaderUtil.readSelection();
		switch (input) {
			case 1: {
				return ParkingType.CAR;
			}
			case 2: {
				return ParkingType.BIKE;
			}
			default: {
				logger.error("ParkingType::getVehichleType : Incorrect input provided");
				throw new IllegalArgumentException("Entered input is invalid");
			}
		}
	}

	public void processExitingVehicle(LocalDateTime outTime) {

		Ticket ticket = null;
		String vehicleRegNumber = "";

		try {
			vehicleRegNumber = getVehichleRegNumber();
			ticket = ticketDAO.getTicket(vehicleRegNumber);
		} catch (Exception e) {
			logger.error(ErrorMessages.PROCESS_EXITING_VEHICLE_REGNUMBER_PROBLEM, e);
			return;
		}

		if (ticket == null) {
			throw new RuntimeException("Error : This vehicle is not in the parking.");
		}

		try {
			ticket.setOutTime(outTime);

			fareCalculatorService.calculateFare(ticket, (ticketDAO.isRecurringVehicle(vehicleRegNumber)));

			if (ticketDAO.updateTicket(ticket)) {
				ParkingSpot parkingSpot = ticket.getParkingSpot();
				parkingSpot.setAvailable(true);
				parkingSpotDAO.updateParking(parkingSpot);
				System.out.println("Please pay the parking fare:" + ticket.getPrice());
				System.out.println(
						"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
			} else {
				logger.error(ErrorMessages.UPDATE_TICKET);
			}
		} catch (Exception e) {
			logger.error(ErrorMessages.PROCESS_EXITING, e);
		}
	}

	public void processExitingVehicle() {
		processExitingVehicle(LocalDateTime.now());
	}

}
