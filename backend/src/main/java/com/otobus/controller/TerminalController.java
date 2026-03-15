package com.otobus.controller;

import com.otobus.entity.Terminal;
import com.otobus.service.TerminalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminals")
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping
    public ResponseEntity<Terminal> addTerminal(@RequestBody Terminal terminal) {
        return ResponseEntity.ok(terminalService.addTerminal(terminal));
    }

    @GetMapping
    public ResponseEntity<List<Terminal>> getAllTerminals() {
        return ResponseEntity.ok(terminalService.getAllTerminals());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTerminal(@PathVariable Long id) {
        terminalService.deleteTerminal(id);
        return ResponseEntity.ok("Otogar başarıyla silindi.");
    }
}