package com.example.demo.batch.configuration;

import java.util.function.Function;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.example.demo.batch.itemProcessor.PersonItemProcessor;
import com.example.demo.beans.batch.Person;

@Configuration
@EnableBatchProcessing
//This example uses a memory-based database (provided by @EnableBatchProcessing), meaning that when it’s done, the data is gone.
public class PersonBatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	/* readerwriterprocessor */
	@Bean
	public FlatFileItemReader<Person> reader() {
		return new FlatFileItemReaderBuilder<Person>().name("perosnItemReader")
				.resource(new ClassPathResource("sample-data.csv"))
				.delimited()
				.names(new String[] { "firstName", "lastName" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				}).build();
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {

		return new JdbcBatchItemWriterBuilder<Person>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)").dataSource(dataSource)
				.build();
	}
	/* readerwriterprocessor -end- */

	/* job step */
	@Bean
	public Job importUserJob(PersonJobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1)
				.end()
				.build();
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Step step1(JdbcBatchItemWriter<Person> writer) {
		return stepBuilderFactory.get("step1").<Person, Person>chunk(10)
				.reader(reader())
				.processor((ItemProcessor) processor())
				.writer(writer)
				.build();
	}
	
	@Bean
	public Job importUserJob1(PersonJobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob1")
				.incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1)
				.end()
				.build();
	}
	/* job step -end- */

//	@Bean
//    public DataSource dataSource() {
//         return DataSourceBuilder.create()
//    .url(env.getProperty("batchdb.url"))
//    .driverClassName(env.getProperty("batchdb.driver"))
//    .username(env.getProperty("batchdb.username"))
//    .password(env.getProperty("batchdb.password"))
//    .build();  
//    }

}
