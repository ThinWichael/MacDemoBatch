package com.example.demo.batch.itemProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.example.demo.beans.batch.Person;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	private final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);
	
	@Override
	public Person process(final Person person) {
		final String firstName = person.getFirstName().toUpperCase();
		final String lastName = person.getLastName().toUpperCase();
		
		final Person upperCasePerson = new Person(firstName, lastName);
		
		log.info("Converting (" + person + ") into (" + upperCasePerson + ")");
		
		return upperCasePerson;
	}
}
