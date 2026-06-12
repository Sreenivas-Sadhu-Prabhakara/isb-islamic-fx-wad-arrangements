package com.bank.islamic.islamicfxwadarrangements.api;

import com.bank.islamic.islamicfxwadarrangements.model.ControlRecord;
import com.bank.islamic.islamicfxwadarrangements.service.ControlRecordStore;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

/**
 * BIAN semantic API for the "Islamic FX Wad Arrangements" service domain.
 *
 * Endpoints follow the BIAN action-term style:
 *   GET  /v1/service-domain                          → who am I (SD metadata)
 *   POST /v1/wad-fx-arrangement-procedure/initiate                    → Initiate a control record
 *   GET  /v1/wad-fx-arrangement-procedure                             → Retrieve (list)
 *   GET  /v1/wad-fx-arrangement-procedure/{crId}/retrieve             → Retrieve (single)
 *   PUT  /v1/wad-fx-arrangement-procedure/{crId}/update               → Update
 *   PUT  /v1/wad-fx-arrangement-procedure/{crId}/control              → Control (suspend|resume|terminate)
 */
@RestController
@RequestMapping("/v1")
public class ServiceDomainController {

    private final ControlRecordStore store;

    public ServiceDomainController(ControlRecordStore store) {
        this.store = store;
    }

    @GetMapping("/service-domain")
    public Map<String, String> serviceDomain() {
        return Map.of(
                "serviceDomain", "Islamic FX Wad Arrangements",
                "businessArea", "Treasury Markets and Sukuk",
                "businessDomain", "Islamic Treasury",
                "functionalPattern", "Process",
                "assetType", "Wad FX Arrangement",
                "controlRecord", "Wad FX Arrangement Procedure",
                "version", "0.1.0",
                "phase", "1-shallow"
        );
    }

    @PostMapping("/wad-fx-arrangement-procedure/initiate")
    @CircuitBreaker(name = "serviceDomain")
    public ResponseEntity<ControlRecord> initiate(@RequestBody(required = false) Map<String, Object> properties) {
        return ResponseEntity.status(HttpStatus.CREATED).body(store.initiate(properties));
    }

    @GetMapping("/wad-fx-arrangement-procedure")
    public Collection<ControlRecord> list() {
        return store.list();
    }

    @GetMapping("/wad-fx-arrangement-procedure/{crId}/retrieve")
    public ResponseEntity<ControlRecord> retrieve(@PathVariable String crId) {
        return store.retrieve(crId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/wad-fx-arrangement-procedure/{crId}/update")
    public ResponseEntity<ControlRecord> update(@PathVariable String crId,
                                                @RequestBody Map<String, Object> properties) {
        return store.update(crId, properties)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/wad-fx-arrangement-procedure/{crId}/control")
    public ResponseEntity<?> control(@PathVariable String crId,
                                     @RequestBody Map<String, String> body) {
        try {
            return store.control(crId, body.get("action"))
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
