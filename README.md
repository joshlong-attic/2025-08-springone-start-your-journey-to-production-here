# A Walking tour of Springdom in 2025

hi, Spring fans! The full video [in which this code was introduced is here](https://www.youtube.com/watch?v=GAgelbsTb9M&t=178s)!



## demo flow  
- desk check: IDEs, jvms, `banner.txt`, devtools, `direnv`, sdkman, etc.
- data oriented programming
- dependency injection 
- Spring Framework 
- BeanPostProcessors, BeanFactoryPostProcessors
- event listeners
- Boot 
- auto-configuration
- containers
- testing
- AOT/GraalVM
- `Environment`
- Spring Cloud Config Server
- Spring Batch  `batch`  (config client, postgresql, jdbc, batch)
	- config
	- move `~/Desktop/talk/dogs-simplified-*sql` to `src/main/resources/schema.sql`
	- read config from `~/Desktop/talk/dogs.csv` into `dogs` table
	- `Job` with `incremented`
	- `Step` with `<Dog,Dog>chunk`, `reader`, `writer` 
	- `FlatFileItemReader<Dog>` with `linesToSkip`, `resource`, `name`, `fieldSetMapper`, `delimited`, `names`
	- `JdbcBatchItemWriter<Dog>` with `dataSource`, `assertUpdates`, `itemPreparedStatementSetter`, `sql` 
- JDBC and `JdbcClient` - implement a simple repository using records and `JdbcClient`
- Flyway/Liquibase
- Spring Data (JDBC)
- Spring MVC
- Spring Modulith 
	- outbox pattern 
	- testing
	- externalization (add `spring-modulith-events-messaging`)
- Spring Integration
- Spring AMQP
	- define `Queue`, `Binding`, and `Binding`
- Observability
	- actuator
	- micrometer
	- SBOMs
	- `git-commit-id`
	- health check
- Spring AI 
	- `ChatModel` && `ChatClient`
	- user/system
	- chat memory
	- RAG
		- vector stores
		- `EmbeddingModel`
	- tools: `schedule an appointment to adopt a dog`
	- MCP
- virtual threads
- Spring GraphQL 
- Spring gRPC 
- Spring Shell 
	- build a simple client that uses `RestClient` to invoke all the `customers`
	- do this _before_ the security! 
- Spring Security & Auth Server
	- username/passwords
	- security filter chains
	- password management with `PasswordEncoder`
	- webauthn 
	- OTT
	- with: `formLogin`, `webauthn`, and `with(authorizationServer())`
- OAuth Resource Servers 
- Spring Authorization Server 
- OAuth Clients 
- Spring Cloud Config Client 
- Spring Cloud Gateway 
	- `TokenRelayFilterFunctions`
- Spring Boot 

