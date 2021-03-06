package com.bridgeLabz.addressBookProgram;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AddressBook {

	public enum IOServiceType {
		FILE_IO, CSV_IO, JSON_IO, DB_IO
	}

	public enum SearchBy {
		CITY, STATE
	}

	private static final Logger logger = LogManager.getLogger(AddressBook.class);
	static Scanner sc = new Scanner(System.in);
	static Map<String, AddressBook> nameToAddressBookMap = new HashMap<String, AddressBook>();
	public String name;
	public Map<String, List<Contact>> cityToContactsMap;
	public Map<String, List<Contact>> stateToContactsMap;
	public List<Contact> contactList;
	private AddressBookDBIoservice addressBookDBIoservice;

	public AddressBook(String name) {
		super();
		this.name = name;
		this.cityToContactsMap = new TreeMap<String, List<Contact>>();
		this.stateToContactsMap = new TreeMap<String, List<Contact>>();
		this.addressBookDBIoservice = new AddressBookDBIoservice();
	}
	
	public AddressBook(String name, List<Contact> contactList) {
		this(name);
		this.contactList=new LinkedList<Contact>(contactList);
	}
	
	
	//Rest IO Starting
	public int countEntries() {
		return this.contactList.size();
	}
	

	public void addContactToContactList(Contact contactToBeAddedToAddressBook) {
		this.contactList.add(contactToBeAddedToAddressBook);
	}
	
	public void updateContactInContactsList(Contact contactToBeUpdated) {
		Contact contactInList=this.getContactFromList(contactToBeUpdated.getFirstName(), contactToBeUpdated.getLastName());
		contactInList.setAddress(contactToBeUpdated.getAddress());
		contactInList.setCity(contactToBeUpdated.getCity());
		contactInList.setState(contactToBeUpdated.getState());
		contactInList.setZip(contactToBeUpdated.getZip());
		contactInList.setPhoneNumber(contactToBeUpdated.getPhoneNumber());
		contactInList.setEmail(contactToBeUpdated.getEmail());
		contactInList.setId(contactToBeUpdated.getId());
	}

	public void deleteContactFromList(String firstName, String lastName) throws AddressBookDBIoException {
		Contact contactToBeDeleted=this.getContactFromList(firstName, lastName);
		if(contactToBeDeleted==null) {
			throw new AddressBookDBIoException("No contact with given name present in adressbook memory");
		}
		this.contactList.remove(contactToBeDeleted);
	}
	
	public boolean checkIfAddressBookInSyncWithResIO(String firstName, String lastName, Contact contactInRestIO) {
		Contact contactInList=this.getContactFromList(firstName, lastName);
		if(contactInList==null) {
			if(contactInRestIO==null) return true;
			else return false;
		}
		return contactInList.equals(contactInRestIO);
	}
	
	//Rest IO Ending	

	public void getContactsIntoListFromDataBase() throws AddressBookDBIoException {
		this.contactList = this.addressBookDBIoservice.readContactDetails();
	}

	//File Input Output and Manipulation Start
	public List<Contact> readContactListFromIO(IOServiceType ioServiceType) throws AddressBookDBIoException {
		switch (ioServiceType) {
		case FILE_IO:
			return new AddressBookFileIOService("addressBook-" + this.name + "-File.txt").readContactDetails();
		case CSV_IO:
			return new AddressBookCsvIOService("addressBook-" + this.name + "-Csvfile.csv").readContactDetails();
		case JSON_IO:
			return new AddressBookJsonIOService("addressBook-" + this.name + "-Jsonfile.json").readContactDetails();
		case DB_IO:
			return this.addressBookDBIoservice.readContactDetails();
		default:
			return null;
		}
	}

	public void writeContactListToIO(IOServiceType ioServiceType, List<Contact> contacts) {
		switch (ioServiceType) {
		case FILE_IO:
			new AddressBookFileIOService("addressBook-" + this.name + "-File.txt").writeContactDetails(contacts);
			break;
		case CSV_IO:
			new AddressBookCsvIOService("addressBook-" + this.name + "-Csvfile.csv").writeContactDetails(contacts);
			break;
		case JSON_IO:
			new AddressBookJsonIOService("addressBook-" + this.name + "-Jsonfile.json").writeContactDetails(contacts);
			break;
		}
	}

	/**
	 * uc7
	 */
	public void addContacts() {
		List<Contact> contacts = new LinkedList<Contact>();
		do {
			logger.info(
					"Enter the contact details in order: \nfirst_name\nlastname\naddress\ncity\nstate\nzip\nphone no.\nemail");
			Contact newContact = new Contact(sc.nextLine(), sc.nextLine(), sc.nextLine(), sc.nextLine(), sc.nextLine(),
					Integer.parseInt(sc.nextLine()), Long.parseLong(sc.nextLine()), sc.nextLine());
			if (contacts.stream().anyMatch(contact -> contact.equals(newContact))) {
				logger.info("Same entry already present. Cannot allow duplicate entries in an address book.");
			} else {
				contacts.add(newContact);
			}
			logger.info("Enter 1 to add another contact, else enter 0: ");
		} while (Integer.parseInt(sc.nextLine()) == 1);
		writeContactListToIO(IOServiceType.JSON_IO, contacts);
	}

	public List<Contact> sortContactsByName() throws AddressBookDBIoException {
		return readContactListFromIO(IOServiceType.JSON_IO).stream()
				.sorted((contact1, contact2) -> contact1.getFirstName().compareTo(contact2.getFirstName()))
				.collect(Collectors.toList());
	}

	public List<Contact> sortByCityStateOrZip() throws AddressBookDBIoException {
		logger.info("Enter 1 to sort by city\n2 to sort by state\n3 to sort by zip");
		switch (Integer.parseInt(sc.nextLine())) {
		case 1:
			return readContactListFromIO(IOServiceType.JSON_IO).stream()
					.sorted((contact1, contact2) -> contact1.getCity().compareTo(contact2.getCity()))
					.collect(Collectors.toList());
		case 2:
			return readContactListFromIO(IOServiceType.JSON_IO).stream()
					.sorted((contact1, contact2) -> contact1.getState().compareTo(contact2.getState()))
					.collect(Collectors.toList());
		case 3:
			return readContactListFromIO(IOServiceType.JSON_IO).stream().sorted(
					(contact1, contact2) -> ((Integer) contact1.getZip()).compareTo((Integer) contact2.getZip()))
					.collect(Collectors.toList());
		default:
			logger.info("Invalid Input.");
			return readContactListFromIO(IOServiceType.JSON_IO);
		}
	}

	public static void getPersonsByCityOrState() {
		logger.info("Choose \n1 To search by city\n2 To search by state\nEnter your choice: ");
		SearchBy searchByParameter = (Integer.parseInt(sc.nextLine()) == 1) ? SearchBy.CITY : SearchBy.STATE;
		logger.info("Enter the name of " + searchByParameter.name() + ": ");
		String cityOrStateName = sc.nextLine();
		nameToAddressBookMap.keySet().stream().forEach(addressBookName -> {
			AddressBook addressBook = nameToAddressBookMap.get(addressBookName);
			logger.info("Persons in the " + searchByParameter.name() + " " + cityOrStateName + " in the address book "
					+ addressBookName + " are: ");
			try {
				addressBook.readContactListFromIO(IOServiceType.JSON_IO).stream().filter(
						contact -> ((searchByParameter == SearchBy.CITY ? contact.getCity() : contact.getState())
								.equals(cityOrStateName)))
						.forEach(contact -> logger.info(contact));
			} catch (AddressBookDBIoException e) {
				logger.info(e.getMessage());
			}
			logger.info("");
		});
	}

	public void generateContactsListByCityAndState() throws AddressBookDBIoException {
		Set<String> cityNames = readContactListFromIO(IOServiceType.JSON_IO).stream().map(contact -> contact.getCity())
				.collect(Collectors.toSet());
		Set<String> stateNames = readContactListFromIO(IOServiceType.JSON_IO).stream()
				.map(contact -> contact.getState()).collect(Collectors.toSet());
		this.cityToContactsMap = cityNames.stream().collect(Collectors.toMap(cityName -> cityName, cityName -> {
			try {
				return readContactListFromIO(IOServiceType.JSON_IO).stream()
						.filter(contact -> contact.getCity().equals(cityName)).sorted((c1, c2) -> {
							return c1.getFirstName().compareTo(c2.getFirstName());
						}).collect(Collectors.toList());
			} catch (AddressBookDBIoException e) {
				logger.info(e.getMessage());
				return null;
			}
		}));
		this.stateToContactsMap = stateNames.stream().collect(Collectors.toMap(stateName -> stateName, stateName -> {
			try {
				return readContactListFromIO(IOServiceType.JSON_IO).stream()
						.filter(contact -> contact.getState().equals(stateName)).sorted((c1, c2) -> {
							return c1.getFirstName().compareTo(c2.getFirstName());
						}).collect(Collectors.toList());
			} catch (AddressBookDBIoException e) {
				logger.info(e.getMessage());
				return null;
			}
		}));
	}

	public static void viewPersonsByCityOrState() {
		logger.info("Choose \n1 To view by city\n2 To view by state\nEnter your choice: ");
		SearchBy viewByParameter = (Integer.parseInt(sc.nextLine()) == 1) ? SearchBy.CITY : SearchBy.STATE;
		nameToAddressBookMap.keySet().stream().forEach(addressBookName -> {
			AddressBook addressBook = nameToAddressBookMap.get(addressBookName);
			try {
				addressBook.generateContactsListByCityAndState();
			} catch (AddressBookDBIoException e) {
				logger.info(e.getMessage());
			}
			logger.info("In the address book " + addressBookName);
			logger.info("");
			(viewByParameter == SearchBy.CITY ? addressBook.cityToContactsMap.keySet()
					: addressBook.stateToContactsMap.keySet()).stream().forEach(cityOrStateName -> {
						logger.info(viewByParameter.name() + ": " + cityOrStateName);
						(viewByParameter == SearchBy.CITY ? addressBook.cityToContactsMap.get(cityOrStateName)
								: addressBook.stateToContactsMap.get(cityOrStateName)).stream()
										.forEach(contact -> logger.info(contact));
						logger.info("");
					});
			logger.info("");
		});
	}

	public static void displayCountByCityAndState() {
		nameToAddressBookMap.keySet().stream().forEach(addressBookName -> {
			AddressBook addressBook = nameToAddressBookMap.get(addressBookName);
			logger.info("In the address book " + addressBookName);
			logger.info("");
			logger.info("Contact counts by city");
			addressBook.cityToContactsMap.keySet().stream().forEach(
					cityName -> logger.info(cityName + ": " + addressBook.cityToContactsMap.get(cityName).size()));
			logger.info("\nContact counts by state");
			addressBook.stateToContactsMap.keySet().stream().forEach(
					stateName -> logger.info(stateName + ": " + addressBook.stateToContactsMap.get(stateName).size()));
			logger.info("");
		});
	}

	public Map<String, Contact> getNameToContactMap(List<Contact> contacts) {
		return contacts.stream().collect(
				Collectors.toMap(contact -> contact.getFirstName() + " " + contact.getLastName(), contact -> contact));
	}

	public void editContact() throws AddressBookDBIoException {
		List<Contact> contacts = readContactListFromIO(IOServiceType.JSON_IO);
		Map<String, Contact> nameToContactMap = getNameToContactMap(contacts);
		do {
			logger.info("Enter name of person whose contact details are to be edited: ");
			String name = sc.nextLine();
			logger.info("Enter the new fields in order: \naddress\ncity\nstate\nzip\nphone no.\nemail");
			try {
				Contact toBeEditedContact = nameToContactMap.get(name);
				toBeEditedContact.setAddress(sc.nextLine());
				toBeEditedContact.setCity(sc.nextLine());
				toBeEditedContact.setState(sc.nextLine());
				toBeEditedContact.setZip(Integer.parseInt(sc.nextLine()));
				toBeEditedContact.setPhoneNumber(Long.parseLong(sc.nextLine()));
				toBeEditedContact.setEmail(sc.nextLine());
				logger.info("Contact after editing:\n" + toBeEditedContact);
			} catch (NullPointerException e) {
				logger.info("No contact found with that name.");
			}
			logger.info("Enter 1 to edit another contact, else enter 0: ");
		} while (Integer.parseInt(sc.nextLine()) == 1);
		writeContactListToIO(IOServiceType.JSON_IO, contacts);
	}

	public void deleteContact() throws AddressBookDBIoException {
		List<Contact> contacts = readContactListFromIO(IOServiceType.JSON_IO);
		Map<String, Contact> nameToContactMap = getNameToContactMap(contacts);
		do {
			logger.info("Enter the name of Contact person to be deleted: ");
			String name = sc.nextLine();
			Contact toBeDeletedContact = nameToContactMap.get(name);
			contacts.remove(toBeDeletedContact);
			nameToContactMap.remove(name);
			logger.info("Enter 1 to delete another contact, else enter 0: ");
		} while (Integer.parseInt(sc.nextLine()) == 1);
		writeContactListToIO(IOServiceType.JSON_IO, contacts);
		logger.info("Address Book after deletion of contacts: \n" + this);
	}

	public static void addAddressBooks() {
		while (true) {
			logger.info("1.Add an address book\n2.Exit\nEnter your choice: ");
			int choice = Integer.parseInt(sc.nextLine());
			if (choice == 1) {
				logger.info("Enter name of the address book");
				String name = sc.nextLine();
				nameToAddressBookMap.put(name, new AddressBook(name));
			} else if (choice == 2) {
				break;
			} else {
				logger.info("Invalid choice. Try again.");
			}
		}
	}

	@Override
	public String toString() {
		int size = -1;
		try {
			size = readContactListFromIO(IOServiceType.JSON_IO).size();
		} catch (AddressBookDBIoException e) {
			logger.info(e.getMessage());
		}
		return "Address Book " + name + " with " + size + (size == 1 ? " contact" : " contacts");
	}

	public static void main(String[] args) throws AddressBookDBIoException {
		addAddressBooks();
		do {
			logger.info("Enter the name of the address book to continue: ");
			AddressBook addressBook = nameToAddressBookMap.get(sc.nextLine());
			if (addressBook == null) {
				logger.info("No address book found with that name.");
			} else {
				addressBook.addContacts();
				logger.info(addressBook);
				List<Contact> sortedContacts = addressBook.sortContactsByName();
				logger.info("Contacts sorted by name are: ");
				sortedContacts.forEach(logger::info);
				sortedContacts = addressBook.sortByCityStateOrZip();
				logger.info("Contact sorted according to input are: ");
				sortedContacts.forEach(logger::info);
				addressBook.editContact();
				addressBook.deleteContact();
				addressBook.generateContactsListByCityAndState();
			}
			logger.info("Enter 1 to continue with another address book, else enter 0: ");
		} while (Integer.parseInt(sc.nextLine()) == 1);
		getPersonsByCityOrState();
		viewPersonsByCityOrState();
		displayCountByCityAndState();
		sc.close();
	}
	//File IO And Manipulation End
	
	//Database IO and Multi-threading	
	public boolean checkIfAddressBookInSyncWithDataBase(String firstName, String lastName)
			throws AddressBookDBIoException {
		Contact contactInList = this.getContactFromList(firstName, lastName);
		List<Contact> contactInDataBase = this.addressBookDBIoservice.getContactFromDataBase(firstName, lastName);
		if (contactInDataBase.isEmpty()) {
			if (contactInList == null)
				return true;
			else
				return false;
		}
		return contactInDataBase.get(0).equals(contactInList);
	}

	public Contact getContactFromList(String firstName, String lastName) {
		return this.contactList.stream()
				.filter(contact -> contact.getFirstName().equals(firstName) && contact.getLastName().equals(lastName))
				.findFirst().orElse(null);
	}

	public void updateContactDetailsInDataBase(String firstName, String lastName, String address, String city,
			String state, int zip, long phoneNumber, String email) throws AddressBookDBIoException {
		this.addressBookDBIoservice.updateAddressDetails(firstName, lastName, address, city, state, zip, phoneNumber,
				email);
		Contact toBeUpdate = this.getContactFromList(firstName, lastName);
		toBeUpdate.setAddress(address);
		toBeUpdate.setCity(city);
		toBeUpdate.setState(state);
		toBeUpdate.setZip(zip);
		toBeUpdate.setPhoneNumber(phoneNumber);
		toBeUpdate.setEmail(email);
	}

	public List<Contact> getContactsAddedInDateRange(LocalDate startDate, LocalDate endDate) throws AddressBookDBIoException {
		return this.addressBookDBIoservice.getContactsAddedInDateRange(startDate,endDate);
	}

	public int getCountOnCityOrState(String cityOrStateName, SearchBy searchByParameter) throws AddressBookDBIoException {
		String whereClauseForSqlQuery=searchByParameter==SearchBy.CITY?"where city='"+cityOrStateName+"'":"where state='"+cityOrStateName+"'";
		return this.addressBookDBIoservice.getCountOnCityOrStateGivenWhereClause(whereClauseForSqlQuery);
	}

	public void addContactToDataBase(String firstName, String lastName, String address, String city, String state,
			int zip, long phoneNumber, String email) throws AddressBookDBIoException {
		this.addressBookDBIoservice.addContactToDataBase(firstName,lastName, address, city, state, zip, phoneNumber, email);
		this.contactList.add(new Contact(firstName, lastName, address, city, state, zip, phoneNumber, email));	
	}

	public void addMultipleContactsWithThreads(List<Contact> contactListToBeAdded) {
		Map<Integer, Boolean> contactAdditionStatus = new HashMap<Integer, Boolean>();
		contactListToBeAdded.forEach(contact->contactAdditionStatus.put(contact.hashCode(),false));
		for (Contact contact : contactListToBeAdded) {
			Runnable task = () -> {
				logger.info("Contact being added: " + Thread.currentThread().getName());
				try {
					this.addContactToDataBase(contact.getFirstName(), contact.getLastName(), contact.getAddress(), contact.getCity(), contact.getState(), contact.getZip(), contact.getPhoneNumber(), contact.getEmail());
				} catch (AddressBookDBIoException e) {
					e.printStackTrace();
				}
				contactAdditionStatus.put(contact.hashCode(), true);
				logger.info("Employee added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, contact.getFirstName());
			thread.start();
		}
		while (contactAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info(this.contactList);
	}

}
