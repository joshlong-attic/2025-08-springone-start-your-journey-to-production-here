package com.example.adoptions.vet;

import com.example.adoptions.adoptions.DogAdoptedEvent;
import com.example.adoptions.adoptions.validation.Validation;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class Dogtor {



    @ApplicationModuleListener
    void checkup(DogAdoptedEvent dogId) throws Exception {
        Thread.sleep(5000);
        System.out.println("checking up on  " + dogId);
    }
}
