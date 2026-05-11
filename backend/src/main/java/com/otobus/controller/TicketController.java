package com.otobus.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otobus.entity.Ticket;
import com.otobus.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/buy")
    public Ticket buyTicket(@RequestBody com.otobus.dto.request.TicketBuyRequest bilet) {
        return ticketService.biletKes(bilet);
    }

    @GetMapping("/all")
    public List<Ticket> getAllTickets() {
        return ticketService.tumBiletleriGetir();
    }

    @DeleteMapping("/cancel/{id}")
    public void cancelTicket(@PathVariable Long id) {
        ticketService.biletIptalEt(id);
    }
}