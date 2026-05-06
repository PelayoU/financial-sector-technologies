package com.uc3m.pu.web;
import com.uc3m.pu.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.atomic.AtomicLong;

@RestController //Le dice a Spring que esta clase es la que se enchufa a internet

public class HelloWorld {

    //AtomicLong es una clase para manejar los numeros de forma segura en red
    //Es decir, leer y sumar 1 ocurre como un solo bloque que no se podra interrumpir
    private final AtomicLong counter = new AtomicLong();


    @GetMapping("/greeting") //Define la direccion que sera para entrar
    //http://localhost:8080/greeting

    public Message getMessage(){ //metodo que devuelve un objeto Message

        //Counter es un contador al que no se puede acceder al mismo tiempo,
        //este contador vive mientras dura el microservicio.
        //Aqui retornamos un objeto Message con id (counter +1 y mensaje "Hello World"
        return new Message(counter.incrementAndGet(), "Hello World");

    }

}


