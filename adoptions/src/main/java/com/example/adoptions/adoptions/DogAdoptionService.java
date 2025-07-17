package com.example.adoptions.adoptions;

import com.example.adoptons.adoptions.grpc.AdoptionsGrpc;
import com.example.adoptons.adoptions.grpc.DogsResponse;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/hateoas/dogs")
class DogAdoptionHateoasController {

    private final DogEntityModelAssembler dogEntityModelAssembler = new DogEntityModelAssembler();

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionHateoasController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    private static class DogEntityModelAssembler
            implements RepresentationModelAssembler<Dog, EntityModel<Dog>> {

        @Override
        public EntityModel<Dog> toModel(Dog entity) {
            return EntityModel.of(entity)
                    .add(linkTo(DogAdoptionHateoasController.class).withRel("dogs"))
                    .add(linkTo(methodOn(DogAdoptionHateoasController.class).dogs()).withSelfRel());
        }
    }

    @GetMapping("/{dogId}")
    EntityModel<Dog> dog(@PathVariable int dogId) {
        return this.dogEntityModelAssembler.toModel(this.dogAdoptionService.byId(dogId));
    }

    @PostMapping("/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.dogAdoptionService.adopt(dogId, owner);
    }

    @GetMapping
    CollectionModel<EntityModel<Dog>> dogs() {
        return this.dogEntityModelAssembler.toCollectionModel(dogAdoptionService.all());
    }
}


@Service
class DogAdoptionGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService service;

    DogAdoptionGrpcService(DogAdoptionService service) {
        this.service = service;
    }

    @Override
    public void all(Empty request, StreamObserver<DogsResponse> responseObserver) {
        var all = this.service.all()
                .stream()
                .map(ourDog -> com.example.adoptons.adoptions.grpc.Dog.newBuilder()
                        .setId(ourDog.id())
                        .setDescription(ourDog.description())
                        .setName(ourDog.name())
                        .build())
                .toList();

        var response = DogsResponse.newBuilder()
                .addAllDogs(all)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}

@Controller
class DogAdoptionGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return this.dogAdoptionService.all();
    }
}

// leonard richardson maturity model
// Spring HATEOAS

@Controller
@ResponseBody
class DogAdoptionHttpController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionHttpController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.dogAdoptionService.adopt(dogId, owner);
    }

    @GetMapping("/dogs")
    Collection<Dog> all() {
        return this.dogAdoptionService.all();
    }

    @GetMapping("/assistant")
    String assistant(Principal principal, @RequestParam String question) {
        return this.dogAdoptionService.assistant(principal.getName(), question);
    }
}

/*

@Component
class YouIncompleteMeApplicationRunner implements ApplicationRunner {

    private final IncompleteEventPublications publications;

    YouIncompleteMeApplicationRunner(IncompleteEventPublications publications) {
        this.publications = publications;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LockRegistry registry;
        registry.executeLocked("incompleteEvents", new CheckedRunnable<Throwable>() {
            @Override
            public void run() throws Throwable {
                publications.resubmitIncompletePublications(ap -> true);
            }
        });

    }
}
*/

@Configuration
class AiConfiguration {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          DogRepository repository,
                          VectorStore vectorStore) {

        if (false) {
            repository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                vectorStore.add(List.of(dogument));
            });
        }
        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        return builder
                .defaultSystem(system)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }
}

@Transactional
@Service
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher publisher;
    private final ChatClient singularity;

    private final Map<String, PromptChatMemoryAdvisor> memory
            = new ConcurrentHashMap<>();

    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher publisher, ChatClient singularity) {
        this.dogRepository = dogRepository;
        this.publisher = publisher;
        this.singularity = singularity;
    }

    void adopt(int dogId, String owner) {
        this.dogRepository.findById(dogId).ifPresent(dog -> {
            var updated = this.dogRepository.save(new Dog(
                    dog.id(), dog.name(), owner, dog.description()
            ));
            System.out.println("adopted [" + updated + "]");
            this.publisher.publishEvent(new DogAdoptionEvent(dogId));
        });
    }

    Dog byId(int dogId) {
        return this.dogRepository.findById(dogId).orElse(null);
    }

    Collection<Dog> all() {
        return dogRepository.findAll();
    }

    String assistant(String user, String question) {

        var advisor = this.memory.computeIfAbsent(user, _ -> PromptChatMemoryAdvisor
                .builder(new InMemoryChatMemory())
                .build());

        return singularity
                .prompt()
                .user(question)
                .advisors(advisor)
                .call()
                .content();
    }
}

// spring data (mongodb, jdbc, jpa, redis, couchbase, neo4j, cassandra, elasticsearch, ...)

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

// look mom, no Lombok!
record Dog(@Id int id, String name, String owner, String description) {
}
