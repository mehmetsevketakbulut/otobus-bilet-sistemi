package com.otobus.controller;

import com.otobus.entity.Terminal;
import com.otobus.service.TerminalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/{id}")
    public ResponseEntity<Terminal> updateTerminal(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Terminal terminal = terminalService.getTerminalById(id);
        if (updates.containsKey("name")) {
            terminal.setName(updates.get("name"));
        }
        return ResponseEntity.ok(terminalService.addTerminal(terminal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTerminal(@PathVariable Long id) {
        terminalService.deleteTerminal(id);
        return ResponseEntity.ok("Otogar başarıyla silindi.");
    }
}