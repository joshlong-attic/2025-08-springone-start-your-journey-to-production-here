package com.example.batch;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication
@ImportRuntimeHints(BatchApplication.Hints.class)
public class BatchApplication {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            for (var c : new Class<?>[]{
                    FlatFileItemReader.class,
                    FlatFileItemReaderBuilder.class,
                    AbstractItemCountingItemStreamItemReader.class})
             hints.reflection().registerType(c, MemberCategory.values());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    FlatFileItemReader<Dog> flatFileItemReader(@Value("file://${HOME}/Desktop/talk/dogs.csv") Resource csv) {
        return new FlatFileItemReaderBuilder<Dog>()
                .resource(csv)
                .name("dogsCsvItemReader")
                .linesToSkip(1)
                .delimited()
                .names("id,name,description,dob,owner,gender,image".split(","))
                .fieldSetMapper(fieldSet -> new Dog(
                        fieldSet.readInt("id"),
                        fieldSet.readString("name"),
                        fieldSet.readString("description")))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<Dog> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Dog>()
                .dataSource(dataSource)
                .sql("insert into dog(id, name,description) values(?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id());
                    ps.setString(2, item.name());
                    ps.setString(3, item.description());
                })
                .build();
    }

    @Bean
    Step step1(JobRepository repository, PlatformTransactionManager transactionManager,
               FlatFileItemReader<Dog> dogFlatFileItemReader,
               JdbcBatchItemWriter<Dog> dogJdbcBatchItemWriter) {
        return new StepBuilder("step1", repository)
                .<Dog, Dog>chunk(10, transactionManager)
                .reader(dogFlatFileItemReader)
                .writer(dogJdbcBatchItemWriter)
                .build();
    }

    @Bean
    Job job(JobRepository repository, Step step) {
        return new JobBuilder("etl", repository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }
}

record Dog(int id, String name, String description) {
}
