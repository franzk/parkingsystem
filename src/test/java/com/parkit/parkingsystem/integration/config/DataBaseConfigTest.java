package com.parkit.parkingsystem.integration.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;

public class DataBaseConfigTest {

	private static final Logger logger = LogManager.getLogger("DataBaseConfigTest");
	
	public DataBaseConfig dataBaseConfigUnderTest = new DataBaseConfig();

	@Test
	public void getConnectionTest() {
		Connection con = null;

		try {
			con = dataBaseConfigUnderTest.getConnection();
			assertThat(con).isNotNull();
			assertThat(con.isClosed()).isFalse();
		} catch (Exception ex) {
			fail("Error while connecting to DB");
		} finally {
			dataBaseConfigUnderTest.closeConnection(con);
		}
	}

	@Test
	public void closePreparedStatementTest() {
		Connection con = null;
		try {
			con = dataBaseConfigUnderTest.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_VEHICLE_TICKET_COUNT);
			ps.setString(1, "");
			ResultSet rs = ps.executeQuery();
			dataBaseConfigUnderTest.closeResultSet(rs);
			dataBaseConfigUnderTest.closePreparedStatement(ps);
			assertThat(ps.isClosed()).isTrue();
		} catch (Exception ex) {
			logger.error("Error while closing Prepared Statement", ex);
		} finally {
			dataBaseConfigUnderTest.closeConnection(con);
		}
	}
	
	@Test
	public void closeResultSetTest() {
		Connection con = null;
		try {
			con = dataBaseConfigUnderTest.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.GET_VEHICLE_TICKET_COUNT);
			ps.setString(1, "");
			ResultSet rs = ps.executeQuery();
			dataBaseConfigUnderTest.closeResultSet(rs);
			
			assertThat(rs.isClosed()).isTrue();
			
			dataBaseConfigUnderTest.closePreparedStatement(ps);
		} catch (Exception ex) {
			logger.error("Error while closing Prepared Statement", ex);
		} finally {
			dataBaseConfigUnderTest.closeConnection(con);
		}		
	}

}
