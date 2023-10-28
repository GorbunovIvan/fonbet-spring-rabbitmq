package com.example.controller;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.GameRepository;
import com.example.service.GameService;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@Transactional
@AutoConfigureDataJpa
class GameControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @SpyBean
    private GameService gameService;
    @SpyBean
    private GameRepository gameRepository;
    @MockBean
    private UserService userService;

    @Value("${limit.user.games}")
    private int limitOfGamesNumberForUser;

    private User user;

    @BeforeEach
    void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        user = new User(1, "user", "user", Role.ADMIN, true);
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void testGetAll() throws Exception {

        // Admin
        var games = gameService.getAll();

        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/games"))
                .andExpect(model().attribute("games", games));

        for (var game : games) {
            result.andExpect(content().string(containsString(game.getLeagueName())))
                    .andExpect(content().string(containsString(game.getTeamsString())));
        }

        verify(gameService, times(2)).getAll();
        verify(userService, times(1)).getCurrentUser();
        Mockito.reset(gameService, userService);

        // Simple user
        user.setRole(Role.USER);
        when(userService.getCurrentUser()).thenReturn(user);

        games = gameService.getAll(limitOfGamesNumberForUser);

        result = mvc.perform(MockMvcRequestBuilders.get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games/games"))
                .andExpect(model().attribute("games", games));

        for (var game : games) {
            result.andExpect(content().string(containsString(game.getLeagueName())))
                    .andExpect(content().string(containsString(game.getTeamsString())));
        }

        verify(gameService, times(2)).getAll(limitOfGamesNumberForUser);
        verify(userService, times(1)).getCurrentUser();
    }
}