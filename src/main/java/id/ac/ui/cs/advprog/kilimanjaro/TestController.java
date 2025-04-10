package id.ac.ui.cs.advprog.kilimanjaro;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {
    @GetMapping("/health")
    public String getHealth() {
        return "Health is ok!";
    }
}
