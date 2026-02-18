package fr.magasin.impression.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerEtat {

    @GetMapping("/api/etat")
    public String etat() {
        return "OK - serveur impression en ligne";
    }
}
