package net.querybuilder4j.controller.rules;

import net.querybuilder4j.config.QbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/rules")
public class RulesController {

    private QbConfig qbConfig;

    @Autowired
    public RulesController(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
    }

    @GetMapping
    public ResponseEntity<QbConfig.Rules> getRules() {
        QbConfig.Rules rules = qbConfig.getRules();

        return ResponseEntity.ok(rules);
    }

}
