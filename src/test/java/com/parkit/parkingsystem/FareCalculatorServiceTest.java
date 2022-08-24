package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.PricesUtil;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void calculateFareCar() {
		LocalDateTime inTime = LocalDateTime.now().minusHours(1);
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		LocalDateTime inTime = LocalDateTime.now().minusHours(1);
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	@Tag("ErrorTest")
	public void calculateFareUnkownType() {
		LocalDateTime inTime = LocalDateTime.now().minusHours(1); // inTime set to one hour in the past
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@Tag("ErrorTest")
	public void calculateFareBikeWithFutureInTime() {
		LocalDateTime inTime = LocalDateTime.now().plusHours(1); // inTime set to one hour in the future
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@Tag("ErrorTest")
	public void calculateFareBikeWithNoOutTime() {
		LocalDateTime inTime = LocalDateTime.now().plusHours(1); // inTime set to one hour in the future
		//LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(null);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}
	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		LocalDateTime inTime = LocalDateTime.now().minusMinutes(45); // 45 minutes parking time should give 3/4th
																		// parking fare
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(PricesUtil.roundToPrice(0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTimeTest() {
		LocalDateTime inTime = LocalDateTime.now().minusMinutes(45); // 45 minutes parking time should give 3/4th
																		// parking fare
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);

		assertEquals(PricesUtil.roundToPrice(0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		LocalDateTime inTime = LocalDateTime.now().minusHours(24); // 24 hours parking time should give 24 * parking
																	// fare per hour
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(PricesUtil.roundToPrice(24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareWhenUserStayLessThan30Minutes() {
		LocalDateTime inTime = LocalDateTime.now().minusMinutes(20); // 20 minutes parking should generate a free fare
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(0, ticket.getPrice());

	}

	@Test
	public void calculateFareForRecurringUser() {
		LocalDateTime inTime = LocalDateTime.now().minusHours(1); // 1 hour parking fare
		LocalDateTime outTime = LocalDateTime.now();

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket, true);
		assertEquals(PricesUtil.roundToPrice((1 - Fare.RECURRING_USER_DISCOUNT) * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

}
