package ca.mcgill.ecse321.eventregistration.controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ca.mcgill.ecse321.eventregistration.model.*;
import ca.mcgill.ecse321.eventregistration.dto.*;
import ca.mcgill.ecse321.eventregistration.service.EventRegistrationService;

@CrossOrigin(origins = "*")
@RestController
public class EventRegistrationRestController {

	@Autowired
	private EventRegistrationService service;

	// POST Mappings

	// @formatter:off
	// Turning off formatter here to ease comprehension of the sample code by
	// keeping the linebreaks
	// Example REST call:
	// http://localhost:8088/persons/John
	@PostMapping(value = { "/persons/{name}", "/persons/{name}/" })
	public PersonDto createPerson(@PathVariable("name") String name) throws IllegalArgumentException {
		// @formatter:on
		Person person = service.createPerson(name);
		return convertToDto(person);
	}
	
	@PostMapping(value = { "/promoters/{name}", "/promoters/{name}/" })
	public PromoterDto createPromoter(@PathVariable("name") String name) throws IllegalArgumentException {
		// @formatter:on
		Promoter p = service.createPromoter(name);
		return convertToDto(p);
	}

	// @formatter:off
	// Example REST call:
	// http://localhost:8080/events/testevent?date=2013-10-23&startTime=00:00&endTime=23:59
	@PostMapping(value = { "/events/{name}", "/events/{name}/" })
	public EventDto createEvent(@PathVariable("name") String name, @RequestParam Date date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime startTime,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime endTime)
			throws IllegalArgumentException {
		// @formatter:on
		Event event = service.createEvent(name, date, Time.valueOf(startTime), Time.valueOf(endTime));
		return convertToDto(event);
	}
	
	// @formatter:off
		// Example REST call:
		// http://localhost:8080/events/testevent?date=2013-10-23&startTime=00:00&endTime=23:59&make=Tesla
		@PostMapping(value = { "/carshows/{name}", "/carshows/{name}/" })
		public EventDto createCarShow(@PathVariable("name") String name, @RequestParam Date date,
				@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime startTime,
				@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime endTime,
				@RequestParam String make)
				throws IllegalArgumentException {
			// @formatter:on
			CarShow event = service.createCarShow(name, date, Time.valueOf(startTime), Time.valueOf(endTime),make);
			return convertToDto(event);
		}

	// @formatter:off
	@PostMapping(value = { "/register", "/register/" })
	public RegistrationDto registerPersonForEvent(@RequestParam(name = "person") PersonDto pDto,
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {
		// @formatter:on

		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());
		Event e = service.getEvent(eDto.getName());

		Registration r = service.register(p, e);
		return convertToDto(r, p, e);
	}
	
	@PostMapping(value = { "/assign", "/assign/" })
	public void assign(@RequestParam(name = "promoter") PromoterDto pDto,
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {
		// @formatter:on

		// Both the person and the event are identified by their names
		Promoter p = service.getPromoter(pDto.getName());
		Event e = service.getEvent(eDto.getName());
		service.promotesEvent(p, e);
	}
	
	
	// @formatter:off
			// Example REST call:
			// http://localhost:8080/events/testevent?date=2013-10-23&startTime=00:00&endTime=23:59&make=Tesla
	@PostMapping(value = { "/pay/", "/pay" })
	public void pay(@RequestParam PersonDto person,
		@RequestParam EventDto event,
		@RequestParam String deviceId,
		@RequestParam int amount)
		throws IllegalArgumentException {
			Person p = service.getPerson(person.getName());
			Event e = service.getEvent(event.getName());
			Registration r = service.getRegistrationByPersonAndEvent(p, e);
			if(r == null) {
				throw new IllegalArgumentException("Registration does not exsit");
			}
			CreditCard c = service.createCreditCardPay(deviceId, amount);
			service.pay(r, c);
	}

	// GET Mappings

	@GetMapping(value = { "/events", "/events/" })
	public List<EventDto> getAllEvents() {
		List<EventDto> eventDtos = new ArrayList<>();
		for (Event event : service.getAllEvents()) {
			eventDtos.add(convertToDto(event));
		}
		return eventDtos;
	}

	// Example REST call:
	// http://localhost:8088/events/person/JohnDoe
	@GetMapping(value = { "/events/person/{name}", "/events/person/{name}/" })
	public List<EventDto> getEventsOfPerson(@PathVariable("name") PersonDto pDto) {
		Person p = convertToDomainObject(pDto);
		return createAttendedEventDtosForPerson(p);
	}

	@GetMapping(value = { "/persons/{name}", "/persons/{name}/" })
	public PersonDto getPersonByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getPerson(name));
	}

	@GetMapping(value = { "/registrations", "/registrations/" })
	public RegistrationDto getRegistration(@RequestParam(name = "person") PersonDto pDto,
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {
		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());
		Event e = service.getEvent(eDto.getName());

		Registration r = service.getRegistrationByPersonAndEvent(p, e);
		return convertToDtoWithoutPerson(r);
	}
	
	
	@GetMapping(value = { "/getregistrations", "/getregistrations/" })
	public List<RegistrationDto> getRegistration() throws IllegalArgumentException {
		// Both the person and the event are identified by their names
		List<RegistrationDto> rs = new ArrayList<>();
		List<Registration> registrations = service.getAllRegistrations();
		for(Registration r : registrations) {
			rs.add(this.convertToDto(r));
		}
		return rs;
	}
	
	@GetMapping(value = { "/registrations/person/{name}", "/registrations/person/{name}/" })
	public List<RegistrationDto> getRegistrationsForPerson(@PathVariable("name") PersonDto pDto)
			throws IllegalArgumentException {
		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());

		return createRegistrationDtosForPerson(p);
	}

	@GetMapping(value = { "/persons", "/persons/" })
	public List<PersonDto> getAllPersons() {
		List<PersonDto> persons = new ArrayList<>();
		for (Person person : service.getAllPersons()) {
			persons.add(convertToDto(person));
		}
		return persons;
	}
	
	@GetMapping(value = { "/carShows", "/carShows/" })
	public List<CarShowDto> getAllCarShows() {
		List<CarShowDto> shows = new ArrayList<>();
		for (CarShow show : service.getAllCarShows()) {
			shows.add(convertToDto(show));
		}
		return shows;
	}
	
	@GetMapping(value = { "/promoters", "/promoters/" })
	public List<PromoterDto> getAllPromoters() {
		List<PromoterDto> ps = new ArrayList<>();
		for (Promoter p : service.getAllPromoters()) {
			ps.add(convertToDto(p));
		}
		return ps;
	}

	@GetMapping(value = { "/events/{name}", "/events/{name}/" })
	public EventDto getEventByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getEvent(name));
	}
	
	
	// Model - DTO conversion methods (not part of the API)

	private EventDto convertToDto(Event e) {
		if (e == null) {
			throw new IllegalArgumentException("There is no such Event!");
		}
		if(e instanceof CarShow) {
			return convertToDto ((CarShow)e);
		}
		EventDto eventDto = new EventDto(e.getName(), e.getDate(), e.getStartTime(), e.getEndTime());
		return eventDto;
	}
	
	private CarShowDto convertToDto(CarShow e ) {
		if (e == null) {
			throw new IllegalArgumentException("There is no such Event!");
		}
		CarShowDto eventDto = new CarShowDto(e.getName(), e.getDate(), e.getStartTime(), e.getEndTime(),e.getMake());
		return eventDto;
	}

	private PersonDto convertToDto(Person p) {
		if (p == null) {
			throw new IllegalArgumentException("There is no such Person!");
		}
		PersonDto personDto = new PersonDto(p.getName());
		personDto.setEventsAttended(createAttendedEventDtosForPerson(p));
		return personDto;
	}

	private PromoterDto convertToDto(Promoter p) {
		if (p == null) {
			throw new IllegalArgumentException("There is no such Promoter!");
		}
		PromoterDto promoterDto = new PromoterDto(p.getName());
		promoterDto.setEventsAttended(createAttendedEventDtosForPerson(p));
		promoterDto.setPromotes(createPromoteDtosForPromoter(p));
		return promoterDto;
	}
	private List<EventDto> createPromoteDtosForPromoter(Promoter p) {
		Set<Event> es = p.getPromotes();
		List<Event> eventsForPerson = new ArrayList<>();
		if(es != null) {
			for (Event e : es) {
				eventsForPerson.add(e);
			}
		}
		List<EventDto> events = new ArrayList<>();
		for (Event event : eventsForPerson) {
			events.add(convertToDto(event));
		}
		return events;
	}

	// DTOs for registrations
	private RegistrationDto convertToDto(Registration r, Person p, Event e) {
		EventDto eDto = convertToDto(e);
		PersonDto pDto = convertToDto(p);
		return new RegistrationDto(pDto, eDto);
	}

	private RegistrationDto convertToDto(Registration r) {
		EventDto eDto = convertToDto(r.getEvent());
		PersonDto pDto = convertToDto(r.getPerson());
		CreditCard c = r.getCreditCard();
		
		RegistrationDto rDto;
		if(c == null) {
			rDto = new RegistrationDto(pDto, eDto);
		}
		else {
			CreditCardDto cDto = converToDto(c);
			rDto = new RegistrationDto(pDto, eDto, cDto);
		}
		
		return rDto;
	}

	private CreditCardDto converToDto(CreditCard creditCard) {
		// TODO Auto-generated method stub
		return new CreditCardDto(creditCard.getAccountNumber(),creditCard.getAmount());
	}

	// return registration dto without peron object so that we are not repeating
	// data
	private RegistrationDto convertToDtoWithoutPerson(Registration r) {
		RegistrationDto rDto = convertToDto(r);
		rDto.setPerson(null);
		return rDto;
	}

	private Person convertToDomainObject(PersonDto pDto) {
		List<Person> allPersons = service.getAllPersons();
		for (Person person : allPersons) {
			if (person.getName().equals(pDto.getName())) {
				return person;
			}
		}
		return null;
	}

	// Other extracted methods (not part of the API)

	private List<EventDto> createAttendedEventDtosForPerson(Person p) {
		List<Event> eventsForPerson = service.getEventsAttendedByPerson(p);
		List<EventDto> events = new ArrayList<>();
		for (Event event : eventsForPerson) {
			events.add(convertToDto(event));
		}
		return events;
	}

	private List<RegistrationDto> createRegistrationDtosForPerson(Person p) {
		List<Registration> registrationsForPerson = service.getRegistrationsForPerson(p);
		List<RegistrationDto> registrations = new ArrayList<RegistrationDto>();
		for (Registration r : registrationsForPerson) {
			registrations.add(convertToDtoWithoutPerson(r));
		}
		return registrations;
	}
}
