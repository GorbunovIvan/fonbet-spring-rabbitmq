package com.example.controller;

import com.example.model.Game;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.GameService;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    @Value("${limit.user.games}")
    private int limitOfGamesNumberForUser;

    @GetMapping
    public String getAll(Model model) {

        var currentUser = currentUser();
        var isCurrentUserAdmin = currentUser.getAuthorities().contains(Role.ADMIN);

        List<Game> games;

        if (isCurrentUserAdmin) {
            games = gameService.getAll();
        } else {
            games = gameService.getAll(limitOfGamesNumberForUser);
        }

        model.addAttribute("games", games);
        model.addAttribute("currentUser", currentUser);

        return "games/games";
    }

    private User currentUser() {
        return userService.getCurrentUser();
    }
}
