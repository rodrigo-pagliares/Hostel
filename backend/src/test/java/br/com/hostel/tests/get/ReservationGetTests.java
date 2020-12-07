package br.com.hostel.tests.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.hostel.controller.dto.LoginDto;
import br.com.hostel.controller.dto.ReservationDto;
import br.com.hostel.controller.form.LoginForm;
import br.com.hostel.controller.form.ReservationForm;
import br.com.hostel.model.CheckPayment;
import br.com.hostel.model.Guest;
import br.com.hostel.model.Reservation;
import br.com.hostel.model.Room;
import br.com.hostel.repository.GuestRepository;
import br.com.hostel.repository.PaymentsRepository;
import br.com.hostel.repository.ReservationRepository;
import br.com.hostel.repository.RoomRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ReservationGetTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static URI uri;
	private static HttpHeaders headers = new HttpHeaders();
	private static Reservation reservation1, reservation2;
	private static List<Room> reservation2RoomsList;
	
	@BeforeAll
    public static void beforeAll(@Autowired ReservationRepository reservationRepository, 
    		@Autowired PaymentsRepository paymentsRepository, @Autowired GuestRepository guestRepository, 
    		@Autowired RoomRepository roomRepository, @Autowired MockMvc mockMvc, 
    		@Autowired ObjectMapper objectMapper) throws JsonProcessingException, Exception {
		
		CheckPayment checkPayment = new CheckPayment();
		ReservationForm reservationForm = new ReservationForm();
		List<Long> rooms_ID = new ArrayList<>();
		Guest guest = new Guest();
		Set<Reservation> reservationsList = new HashSet<>();
		LoginForm login = new LoginForm();
		
		uri = new URI("/api/reservations/");
		
		//setting login variables to autenticate
		login.setEmail("admin@email.com");
		login.setPassword("123456");

		//posting on /auth to get token
		MvcResult resultAuth = mockMvc
				.perform(post("/auth")
				.content(objectMapper.writeValueAsString(login)).contentType("application/json"))
				.andReturn();	
			
		String contentAsString = resultAuth.getResponse().getContentAsString();

		LoginDto loginObjResponse = objectMapper.readValue(contentAsString, LoginDto.class);
		
		// seting header to put on post and delete request parameters
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + loginObjResponse.getToken());

		//setting reservation object
		reservationForm.setCheckinDate(LocalDate.of(2021, 04, 01));
		reservationForm.setCheckoutDate(LocalDate.of(2021, 04, 04));
		reservationForm.setNumberOfGuests(4);
		reservationForm.setGuest_ID(1L);
		
		checkPayment.setAmount(3000);
		checkPayment.setDate(LocalDateTime.of(LocalDate.of(2020, 01, 25), LocalTime.of(21, 31)));
		checkPayment.setBankId("01");
		checkPayment.setBankName("Banco do Brasil");
		checkPayment.setBranchNumber("1234-5");
		
		reservationForm.setPayment(checkPayment);
		
		rooms_ID.add(2L);
		reservationForm.setRooms_ID(rooms_ID);
		
		paymentsRepository.save(reservationForm.getPayment());
		
		reservation1 = reservationRepository.save(reservationForm.returnReservation(paymentsRepository, roomRepository));
		reservationsList.add(reservation1);

		reservationForm.setCheckinDate(LocalDate.of(2021, 05, 01));
		reservationForm.setCheckoutDate(LocalDate.of(2021, 05, 04));
		rooms_ID.remove(2L);
		rooms_ID.add(3L);
		reservation2 = reservationRepository.save(reservationForm.returnReservation(paymentsRepository, roomRepository));
		reservationsList.add(reservation2);
		
		guest = guestRepository.findById(reservationForm.getGuest_ID()).get();
		guest.setReservations(reservationsList);
		guestRepository.save(guest);   
		
		reservation2RoomsList = reservation2.getRooms().stream().collect(Collectors.toList());
	}
	
	@Test
	public void shouldReturnAllReservationsWithoutParam() throws Exception {

		MvcResult result = 
				mockMvc.perform(get(uri)
						.headers(headers))
						.andDo(print())
						.andExpect(status().isOk())
						.andReturn();

		String contentAsString = result.getResponse().getContentAsString();

		ReservationDto[] reservationObjResponse = objectMapper.readValue(contentAsString, ReservationDto[].class);

		/// Verify request succeed
		assertEquals(reservation1.getCheckinDate(), reservationObjResponse[0].getCheckinDate());
		assertEquals(reservation2.getCheckoutDate(), reservationObjResponse[1].getCheckoutDate());
		assertEquals(reservation2RoomsList.get(0).getNumber(), reservationObjResponse[1].getRooms().stream()
																						.collect(Collectors.toList())
																						.get(0).getNumber());
		assertEquals(2, reservationObjResponse.length);
	}
	
	@Test
	public void shouldReturnAllReservationsByGuestName() throws Exception {

		MvcResult result = 
				mockMvc.perform(get(uri)
						.param("name", "admin")
						.headers(headers))
						.andDo(print())
						.andReturn();

		String contentAsString = result.getResponse().getContentAsString();

		ReservationDto[] reservationObjResponse = objectMapper.readValue(contentAsString, ReservationDto[].class);

		/// Verify request succeed
		assertEquals(reservation1.getCheckinDate(), reservationObjResponse[0].getCheckinDate());
		assertEquals(reservation2.getCheckoutDate(), reservationObjResponse[1].getCheckoutDate());
		assertEquals(reservation2RoomsList.get(0).getNumber(), reservationObjResponse[1].getRooms().stream()
																						.collect(Collectors.toList())
																						.get(0).getNumber());
		assertEquals(2, reservationObjResponse.length);
	}

	@Test
	public void shouldReturnNotFoundStatusAndNullBodyByWrongParam() throws Exception {

		MvcResult result = 
				mockMvc.perform(get(uri)
						.param("name", "admin333")
						.headers(headers))
						.andDo(print())
						.andReturn();
		
		String contentAsString = result.getResponse().getContentAsString();

		ReservationDto[] reservationObjResponse = objectMapper.readValue(contentAsString, ReservationDto[].class);
		
		assertEquals(0, reservationObjResponse.length);
	}
}
