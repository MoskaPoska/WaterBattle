package org.example.waterbattle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {
    @GetMapping("/board")
    public String index() {
        return "board-view";
    }
    @PostMapping("/start")
    public String startGame() {
        return "redirect:/board";
    }
}
