package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    FlatFileItemReader<Dog> reader(@Value("file://${HOME}/Desktop/talk/dogs.csv") Resource resource) {
        return new FlatFileItemReaderBuilder<Dog>()
                .resource(resource)
                .name("csvToDbReader")
                .delimited()
                .names("id,name,description,dob,owner,gender,image".split(","))
                .linesToSkip(1)
                .fieldSetMapper(fieldSet -> new Dog(fieldSet.readInt("id"),
                        fieldSet.readString("name"),
                        fieldSet.readString("owner"),
                        fieldSet.readString("description")))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<Dog> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Dog>()
                .dataSource(dataSource)
                .sql("INSERT INTO DOG (id, name, description) VALUES (?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id());
                    ps.setString(2, item.name());
                    ps.setString(3, item.description());
                })
                .build();
    }

    @Bean
    Step step(JobRepository repository, JdbcBatchItemWriter<Dog> writer, FlatFileItemReader<Dog> reader, PlatformTransactionManager tx) {
        return new StepBuilder("csvToDb", repository)
                .<Dog, Dog>chunk(10, tx)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    Job job(JobRepository repository, Step step) {
        return new JobBuilder("etl", repository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    ApplicationRunner runner(JobLauncher jobLauncher, Job job) {
        return _ -> jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
    }
}

record Dog(int id, String name, String owner, String description) {
}