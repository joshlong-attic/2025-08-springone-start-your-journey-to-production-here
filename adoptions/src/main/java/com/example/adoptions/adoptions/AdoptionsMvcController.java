package com.example.adoptions.adoptions;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
@ResponseBody
class AdoptionsMvcController {

    private final DogAdoptionService dogAdoptionService;

    AdoptionsMvcController(DogAdoptionService dogAdoptionService) {
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
}

@Controller
@ResponseBody
class MeController {

    @GetMapping("/me")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

@Controller
class DogAdoptionsGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionsGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @BatchMapping
    Map<Dog, Shelter> shelter(Collection<Dog> dogs) {
        var map = new HashMap<Dog, Shelter>();
        for (var dog : dogs) {
            map.put(dog, new Shelter(Math.random() > 0.5 ? "Denver" : "San Francisco"));
        }
        System.out.println("getting shelters [" + map + "] for ids [" + dogs + "]");
        return map;
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return this.dogAdoptionService.all();
    }
}


record Shelter(String location) {
}

//@Component
//class YouIncompleteMeListener {
//
//     YouIncompleteMeListener(IncompleteEventPublications eventPublications) {

//            LockRegistry lockRegistry;
//            lockregistry.tryAcquire("myLock" , lambda)
//         eventPublications.resubmitIncompletePublications(evt -> evt.);
//    }
//}

@Service
@Transactional
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher publisher;

    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher publisher) {
        this.dogRepository = dogRepository;
        this.publisher = publisher;
    }


    Collection<Dog> all() {
        return this.dogRepository.findAll();
    }

    void adopt(int dogId, String owner) {
        this.dogRepository.findById(dogId).ifPresent(dog -> {
            var adopted = new Dog(dog.id(), dog.name(), dog.description(), owner);
            this.dogRepository.save(adopted);
            System.out.println("adopted [" + adopted + "]");
            this.publisher.publishEvent(new DogAdoptedEvent(dogId));
        });
    }
}

record Dog(@Id int id, String name, String description, String owner) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}