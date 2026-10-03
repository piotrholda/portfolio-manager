package piotrholda.portfoliomanager.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Forward only UI routes so API, Swagger and missing assets keep their normal responses. */
@Controller
class UiController {
    @GetMapping({"/", "/simulations", "/strategies", "/instruments", "/quotations", "/corporate-actions"})
    String index() {
        return "forward:/index.html";
    }
}
